package com.mycompany.passcodedemo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Simple animated panel that gives the application a playful "hacker" vibe.
 */
public class HackerAnimationPanel extends JPanel {

    private static final char[] GLYPHS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!$%&*@#".toCharArray();
    private static final int STREAM_COUNT = 10;
    private static final int UPDATE_INTERVAL = 120;

    private final Random random = new Random();
    private final Timer timer;
    private final StringBuilder[] streams = new StringBuilder[STREAM_COUNT];

    public HackerAnimationPanel() {
        setBackground(Color.BLACK);
        setForeground(new Color(0, 255, 128));
        setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));

        for (int i = 0; i < streams.length; i++) {
            streams[i] = new StringBuilder();
        }

        timer = new Timer(UPDATE_INTERVAL, e -> {
            advanceAnimation();
            repaint();
        });
        timer.start();
    }

    private void advanceAnimation() {
        for (StringBuilder stream : streams) {
            if (stream.length() > 12) {
                stream.setLength(random.nextInt(6));
            }
            stream.append(GLYPHS[random.nextInt(GLYPHS.length)]);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getForeground());

        int width = getWidth();
        int height = getHeight();
        int columnWidth = Math.max(1, width / STREAM_COUNT);

        for (int i = 0; i < streams.length; i++) {
            int x = i * columnWidth + 8;
            StringBuilder stream = streams[i];
            int y = 30;
            for (int j = stream.length() - 1; j >= 0; j--) {
                float alpha = Math.max(0.2f, 1f - (stream.length() - 1 - j) * 0.12f);
                g2.setColor(new Color(0f, 1f, 0.5f, Math.min(alpha, 1f)));
                g2.drawString(String.valueOf(stream.charAt(j)), x, y);
                y += 24;
                if (y > height) {
                    break;
                }
            }
        }
        g2.dispose();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }
}
