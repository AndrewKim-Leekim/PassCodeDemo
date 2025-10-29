package com.mycompany.passcodedemo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URL;
import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 * Hacker animation panel backed by the {@code hacker.mp4} video clip that ships
 * with the application resources. The panel exposes the same phase-based API as
 * the previous hand-drawn animation so that the rest of the UI can continue to
 * drive the narrative flow.
 */
public class HackerAnimationPanel extends JPanel {

    private final JFXPanel fxPanel = new JFXPanel();

    private MediaPlayer mediaPlayer;
    private volatile boolean mediaReady;
    private Duration pendingSeek;
    private boolean pendingPlay;

    public HackerAnimationPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        add(fxPanel, BorderLayout.CENTER);
        Platform.runLater(this::initialiseMediaScene);
    }

    public void showSnooping() {
        restartFrom(Duration.ZERO);
    }

    public void showAttempting() {
        ensurePlaying();
    }

    public void showDefeated() {
        ensurePlaying();
    }

    public void updateProgress(double progress) {
        // The legacy drawing logic reacted to the progress value. The video-based
        // version keeps this hook to remain API compatible, even though the
        // supplied clip already encodes the visual progression.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2.setColor(new Color(9, 16, 32, 235));
        g2.fillRoundRect(0, 0, width, height, 28, 28);

        g2.setColor(new Color(0, 0, 0, 90));
        g2.drawRoundRect(1, 1, width - 3, height - 3, 26, 26);

        g2.dispose();
    }

    @Override
    public void removeNotify() {
        stopMedia();
        super.removeNotify();
    }

    private void initialiseMediaScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(9,16,32,0.9); -fx-background-radius: 24;");
        Scene scene = new Scene(root);
        fxPanel.setScene(scene);

        URL mediaUrl = getClass().getResource("/hacker.mp4");
        if (mediaUrl == null) {
            installFallbackMessage(root, "hacker.mp4 리소스를 찾을 수 없습니다.");
            return;
        }

        try {
            Media media = new Media(mediaUrl.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setOnReady(() -> {
                mediaReady = true;
                applyPendingRequests();
            });
            mediaPlayer.setOnError(() -> handleMediaFailure(root));

            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(root.widthProperty());
            mediaView.fitHeightProperty().bind(root.heightProperty());
            root.getChildren().add(mediaView);
        } catch (MediaException ex) {
            handleMediaFailure(root);
        }
    }

    private void installFallbackMessage(StackPane root, String message) {
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        root.getChildren().setAll(label);
    }

    private void ensurePlaying() {
        if (mediaPlayer == null) {
            pendingPlay = true;
            return;
        }
        if (!mediaReady) {
            pendingPlay = true;
            return;
        }
        Platform.runLater(() -> {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.STOPPED || status == MediaPlayer.Status.PAUSED
                    || status == MediaPlayer.Status.READY) {
                mediaPlayer.play();
            }
        });
    }

    private void restartFrom(Duration position) {
        if (mediaPlayer == null || !mediaReady) {
            pendingSeek = position;
            pendingPlay = true;
            return;
        }
        Platform.runLater(() -> {
            mediaPlayer.seek(position);
            mediaPlayer.play();
        });
    }

    private void applyPendingRequests() {
        Duration seekTarget = pendingSeek;
        boolean shouldPlay = pendingPlay;
        pendingSeek = null;
        pendingPlay = false;
        if (seekTarget != null && !seekTarget.isUnknown()) {
            Platform.runLater(() -> {
                mediaPlayer.seek(seekTarget);
                if (shouldPlay || mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                    mediaPlayer.play();
                }
            });
        } else if (shouldPlay) {
            Platform.runLater(() -> mediaPlayer.play());
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) {
            return;
        }
        MediaPlayer player = mediaPlayer;
        mediaPlayer = null;
        mediaReady = false;
        pendingSeek = null;
        pendingPlay = false;
        Platform.runLater(() -> {
            player.stop();
            player.dispose();
        });
    }

    private void handleMediaFailure(StackPane root) {
        MediaPlayer failedPlayer = mediaPlayer;
        mediaPlayer = null;
        mediaReady = false;
        pendingSeek = null;
        pendingPlay = false;
        if (failedPlayer != null) {
            try {
                failedPlayer.stop();
            } catch (IllegalStateException ignored) {
                // If the player cannot stop due to its state we still dispose to release resources.
            }
            failedPlayer.dispose();
        }
        installFallbackMessage(root, "해커 애니메이션을 재생할 수 없습니다.");
    }
}
