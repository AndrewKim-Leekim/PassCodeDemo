package com.mycompany.passcodedemo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Animated vignette that visualises a mischievous hacker reacting to the
 * analysis process. The scene adapts depending on the current analysis stage.
 */
public class HackerAnimationPanel extends JPanel {

    private enum Phase {
        SNOOPING,
        ATTEMPTING,
        DEFEATED
    }

    private static final int UPDATE_INTERVAL = 70;

    private final Timer timer;
    private Phase phase = Phase.SNOOPING;
    private double animationTick;
    private double progressFraction;

    public HackerAnimationPanel() {
        setOpaque(false);
        timer = new Timer(UPDATE_INTERVAL, e -> {
            animationTick += 0.12;
            repaint();
        });
        timer.start();
    }

    public void showSnooping() {
        phase = Phase.SNOOPING;
        repaint();
    }

    public void showAttempting() {
        phase = Phase.ATTEMPTING;
        repaint();
    }

    public void showDefeated() {
        phase = Phase.DEFEATED;
        repaint();
    }

    public void updateProgress(double progress) {
        progressFraction = Math.max(0.0, Math.min(1.0, progress));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        GradientPaint background = new GradientPaint(0, 0, new Color(9, 16, 32, 235),
                width, height, new Color(34, 82, 112, 220));
        g2.setPaint(background);
        g2.fillRoundRect(0, 0, width, height, 28, 28);

        g2.setColor(new Color(0, 0, 0, 90));
        g2.setStroke(new BasicStroke(3.2f));
        g2.drawRoundRect(1, 1, width - 3, height - 3, 26, 26);

        drawDesk(g2, width, height);
        drawTerminalGlow(g2, width, height);
        drawSafe(g2, width, height);
        drawHacker(g2, width, height);
        g2.dispose();
    }

    private void drawDesk(Graphics2D g2, int width, int height) {
        int deskY = (int) (height * 0.72);
        GradientPaint deskPaint = new GradientPaint(0, deskY, new Color(24, 32, 50),
                0, height, new Color(12, 18, 30));
        g2.setPaint(deskPaint);
        g2.fillRoundRect((int) (width * 0.08), deskY, (int) (width * 0.84), (int) (height * 0.18), 18, 18);
    }

    private void drawTerminalGlow(Graphics2D g2, int width, int height) {
        int monitorX = (int) (width * 0.12);
        int monitorY = (int) (height * 0.18);
        int monitorW = (int) (width * 0.34);
        int monitorH = (int) (height * 0.36);

        g2.setColor(new Color(22, 32, 52));
        g2.fillRoundRect(monitorX, monitorY, monitorW, monitorH, 18, 18);

        g2.setColor(new Color(70, 255, 190, 120));
        g2.setStroke(new BasicStroke(2.4f));
        g2.drawRoundRect(monitorX + 4, monitorY + 4, monitorW - 8, monitorH - 8, 14, 14);

        double flicker = 0.15 * Math.sin(animationTick * 1.8);
        Color glow = new Color(46, 220, 160, (int) (150 + 70 * (0.5 + flicker)));
        g2.setColor(glow);
        g2.fillRoundRect(monitorX + 10, monitorY + 10, monitorW - 20, monitorH - 20, 12, 12);
    }

    private void drawSafe(Graphics2D g2, int width, int height) {
        int safeSize = (int) (Math.min(width, height) * 0.44);
        int safeX = width - (int) (width * 0.16) - safeSize;
        int safeY = (int) (height * 0.2);

        g2.setColor(new Color(32, 54, 80));
        g2.fillRoundRect(safeX, safeY, safeSize, safeSize, 28, 28);

        g2.setColor(new Color(160, 210, 255, 120));
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRoundRect(safeX + 4, safeY + 4, safeSize - 8, safeSize - 8, 24, 24);

        int dialSize = (int) (safeSize * 0.32);
        int dialX = safeX + safeSize / 2 - dialSize / 2;
        int dialY = safeY + safeSize / 2 - dialSize / 2;

        g2.setColor(new Color(210, 235, 255));
        g2.fillOval(dialX, dialY, dialSize, dialSize);

        g2.setColor(new Color(40, 68, 110));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(dialX, dialY, dialSize, dialSize);

        double dialAngle = Math.toRadians(progressFraction * 210 - 30);
        int dialCenterX = dialX + dialSize / 2;
        int dialCenterY = dialY + dialSize / 2;
        int dialHandLength = (int) (dialSize * 0.36);
        int handX = dialCenterX + (int) (Math.cos(dialAngle) * dialHandLength);
        int handY = dialCenterY + (int) (Math.sin(dialAngle) * dialHandLength);
        g2.drawLine(dialCenterX, dialCenterY, handX, handY);
    }

    private void drawHacker(Graphics2D g2, int width, int height) {
        double bob = Math.sin(animationTick) * 4;
        int baseX = (int) (width * 0.32);
        int baseY = (int) (height * 0.68 + bob);

        // Cloak
        Path2D cloak = new Path2D.Double();
        cloak.moveTo(baseX - 58, baseY + 12);
        cloak.curveTo(baseX - 64, baseY - 62, baseX + 28, baseY - 74, baseX + 42, baseY + 18);
        cloak.lineTo(baseX + 26, baseY + 56);
        cloak.lineTo(baseX - 70, baseY + 52);
        cloak.closePath();
        g2.setColor(new Color(12, 20, 38, 240));
        g2.fill(cloak);

        // Head
        int headRadius = 32;
        g2.setColor(new Color(34, 48, 70));
        g2.fillOval(baseX - headRadius, baseY - headRadius - 66, headRadius * 2, headRadius * 2);
        g2.setColor(new Color(8, 12, 24));
        g2.fillOval(baseX - headRadius, baseY - headRadius - 50, headRadius * 2, (int) (headRadius * 1.2));

        // Eyes
        g2.setColor(new Color(180, 255, 200));
        int eyeY = baseY - headRadius - 32;
        int eyeOffset = 14;
        g2.fillRoundRect(baseX - eyeOffset - 12, eyeY, 24, 9, 6, 6);
        g2.fillRoundRect(baseX + 4, eyeY, 24, 9, 6, 6);

        switch (phase) {
            case SNOOPING -> drawSnoopingPose(g2, baseX, baseY);
            case ATTEMPTING -> drawAttemptPose(g2, baseX, baseY);
            case DEFEATED -> drawDefeatedPose(g2, baseX, baseY);
        }
    }

    private void drawSnoopingPose(Graphics2D g2, int baseX, int baseY) {
        g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(28, 44, 68));
        int armY = baseY - 8;
        int handX = baseX + 54;
        g2.drawLine(baseX - 36, baseY + 24, handX, armY);

        g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(baseX - 42, baseY + 40, baseX - 20, baseY + 68);
        g2.drawLine(baseX - 2, baseY + 44, baseX + 12, baseY + 72);

        g2.setColor(new Color(110, 200, 255, 180));
        int flashlightX = handX + 10;
        int flashlightY = armY - 4;
        g2.fillOval(flashlightX, flashlightY, 20, 20);
        g2.setColor(new Color(110, 200, 255, 80));
        g2.fillOval(flashlightX + 14, flashlightY - 8, 46, 36);
    }

    private void drawAttemptPose(Graphics2D g2, int baseX, int baseY) {
        g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(32, 52, 80));
        int pivotX = baseX + 40;
        int pivotY = baseY - 12;
        g2.drawLine(baseX - 34, baseY + 16, pivotX, pivotY);

        AffineTransform old = g2.getTransform();
        double angle = Math.toRadians(-20 + progressFraction * 65 + Math.sin(animationTick * 1.3) * 6);
        g2.rotate(angle, pivotX, pivotY);
        g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(244, 196, 86));
        g2.drawLine(pivotX, pivotY, pivotX + 70, pivotY);
        g2.setStroke(new BasicStroke(6f));
        g2.drawLine(pivotX + 52, pivotY, pivotX + 52, pivotY + 18);
        g2.drawLine(pivotX + 62, pivotY, pivotX + 62, pivotY - 18);
        g2.setTransform(old);

        g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(32, 52, 80));
        g2.drawLine(baseX - 42, baseY + 40, baseX - 20, baseY + 70);
        g2.drawLine(baseX - 4, baseY + 44, baseX + 4, baseY + 74);
    }

    private void drawDefeatedPose(Graphics2D g2, int baseX, int baseY) {
        double wobble = Math.sin(animationTick * 2.2) * 6;
        g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(24, 42, 66));
        g2.drawLine(baseX - 48, baseY + 10, baseX - 16, baseY - 40);
        g2.drawLine(baseX - 16, baseY - 40, baseX + 26, baseY - 30);

        g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(baseX - 40, baseY + 44, baseX - 22, baseY + 80);
        g2.drawLine(baseX, baseY + 46, baseX + 12, baseY + 82);

        int flagPoleX = baseX + 18;
        int flagPoleY = baseY - 54;
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(210, 220, 255));
        g2.drawLine(flagPoleX, flagPoleY, flagPoleX, flagPoleY + 70);

        Path2D flag = new Path2D.Double();
        flag.moveTo(flagPoleX, flagPoleY + 8);
        flag.curveTo(flagPoleX + 32, flagPoleY + 2 + wobble,
                flagPoleX + 44, flagPoleY + 46 + wobble,
                flagPoleX + 14, flagPoleY + 52);
        flag.closePath();
        g2.setColor(new Color(255, 255, 255, 210));
        g2.fill(flag);
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
