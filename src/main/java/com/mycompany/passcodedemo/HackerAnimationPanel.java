package com.mycompany.passcodedemo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
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
    private static final int STREAM_COUNT = 14;
    private static final int UPDATE_INTERVAL = 110;
    private final Random random = new Random();
    private final Timer timer;
    private final StringBuilder[] streams = new StringBuilder[STREAM_COUNT];
    private float pulsePhase = 0f;

    public HackerAnimationPanel() {
        setOpaque(false);
        setBackground(new Color(8, 14, 26));
        setForeground(new Color(0, 255, 160));
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
        pulsePhase += 0.12f;
        if (pulsePhase > Math.PI * 2) {
            pulsePhase -= Math.PI * 2;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        GradientPaint gradient = new GradientPaint(0, 0, new Color(6, 12, 28, 230),
                width, height, new Color(18, 70, 88, 220));
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width, height, 28, 28);

        g2.setColor(new Color(0, 255, 170, 80));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(1, 1, width - 3, height - 3, 26, 26);

        int columnWidth = Math.max(1, width / STREAM_COUNT);
        float baseFontSize = Math.max(14f, columnWidth * 0.75f);
        Font dynamicFont = getFont().deriveFont(baseFontSize);
        g2.setFont(dynamicFont);

        for (int i = 0; i < streams.length; i++) {
            int x = i * columnWidth + columnWidth / 3;
            StringBuilder stream = streams[i];
            float y = dynamicFont.getSize2D() * 1.5f;
            for (int j = stream.length() - 1; j >= 0; j--) {
                float alpha = Math.max(0.18f, 1f - (stream.length() - 1 - j) * 0.12f);
                g2.setColor(new Color(0f, 1f, 0.6f, Math.min(alpha, 1f)));
                g2.drawString(String.valueOf(stream.charAt(j)), x, Math.round(y));
                y += dynamicFont.getSize2D() * 1.25f;
                if (y > height) {
                    break;
                }
            }
        }
        paintLockOverlay(g2, width, height);
        g2.dispose();
    }

    private void paintLockOverlay(Graphics2D g2, int width, int height) {
        int size = Math.min(width, height);
        float lockSize = size * 0.32f;
        float offsetY = (float) Math.sin(pulsePhase) * lockSize * 0.08f;
        int centerX = width - Math.round(lockSize * 0.9f);
        int centerY = Math.round(lockSize + offsetY);

        g2.setStroke(new BasicStroke(Math.max(3f, lockSize * 0.08f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(0, 255, 210, 120));
        int bodyWidth = Math.round(lockSize);
        int bodyHeight = Math.round(lockSize * 0.85f);
        int bodyX = centerX - bodyWidth / 2;
        int bodyY = centerY - bodyHeight / 2;
        g2.drawRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, Math.round(lockSize * 0.3f), Math.round(lockSize * 0.3f));

        int shackleWidth = Math.round(lockSize * 0.7f);
        int shackleHeight = Math.round(lockSize * 0.6f);
        int shackleX = centerX - shackleWidth / 2;
        int shackleY = bodyY - shackleHeight / 2;
        g2.drawArc(shackleX, shackleY, shackleWidth, shackleHeight, 200, 140);

        g2.setColor(new Color(0, 255, 210, 70));
        g2.fillOval(centerX - Math.round(lockSize * 0.08f),
                centerY - Math.round(lockSize * 0.08f),
                Math.round(lockSize * 0.16f), Math.round(lockSize * 0.16f));
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
