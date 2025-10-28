package com.mycompany.passcodedemo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

/**
 * Decorative illustration panel that renders responsive security themed icons.
 */
public class SecurityIllustrationPanel extends JPanel {

    private float scaleMultiplier = 1f;

    public SecurityIllustrationPanel() {
        setOpaque(false);
    }

    public void setScaleMultiplier(float scale) {
        this.scaleMultiplier = Math.max(0.65f, Math.min(scale, 1.6f));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        GradientPaint background = new GradientPaint(0, 0, new Color(26, 54, 96, 180),
                width, height, new Color(58, 128, 164, 150));
        g2.setPaint(background);
        g2.fillRoundRect(0, 0, width, height, 36, 36);

        g2.setColor(new Color(255, 255, 255, 90));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(2, 2, width - 4, height - 4, 32, 32);

        float baseSize = Math.min(width, height) * 0.28f * scaleMultiplier;

        drawShield(g2, width * 0.26f, height * 0.45f, baseSize);
        drawKey(g2, width * 0.68f, height * 0.6f, baseSize * 1.1f);
        drawFingerprint(g2, width * 0.45f, height * 0.28f, baseSize * 0.75f);

        drawGlow(g2, width * 0.18f, height * 0.18f, baseSize * 0.4f, new Color(140, 220, 255, 120));
        drawGlow(g2, width * 0.72f, height * 0.22f, baseSize * 0.35f, new Color(255, 210, 120, 120));
        drawGlow(g2, width * 0.55f, height * 0.78f, baseSize * 0.28f, new Color(120, 255, 200, 120));

        g2.dispose();
    }

    private void drawShield(Graphics2D g2, float centerX, float centerY, float size) {
        Path2D.Float shield = new Path2D.Float();
        shield.moveTo(centerX, centerY - size);
        shield.lineTo(centerX + size * 0.85f, centerY - size * 0.35f);
        shield.lineTo(centerX + size * 0.6f, centerY + size * 0.95f);
        shield.lineTo(centerX, centerY + size * 1.3f);
        shield.lineTo(centerX - size * 0.6f, centerY + size * 0.95f);
        shield.lineTo(centerX - size * 0.85f, centerY - size * 0.35f);
        shield.closePath();

        g2.setPaint(new GradientPaint(centerX, centerY - size, new Color(90, 200, 255, 210),
                centerX, centerY + size * 1.3f, new Color(36, 120, 205, 230)));
        g2.fill(shield);
        g2.setStroke(new BasicStroke(Math.max(2f, size * 0.08f)));
        g2.setColor(new Color(255, 255, 255, 200));
        g2.draw(shield);

        g2.setStroke(new BasicStroke(Math.max(2f, size * 0.13f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(Math.round(centerX - size * 0.32f), Math.round(centerY + size * 0.1f),
                Math.round(centerX - size * 0.06f), Math.round(centerY + size * 0.48f));
        g2.drawLine(Math.round(centerX - size * 0.06f), Math.round(centerY + size * 0.48f),
                Math.round(centerX + size * 0.42f), Math.round(centerY - size * 0.22f));
    }

    private void drawKey(Graphics2D g2, float centerX, float centerY, float size) {
        float headRadius = size * 0.35f;
        g2.setColor(new Color(255, 215, 140, 190));
        g2.setStroke(new BasicStroke(Math.max(2f, size * 0.12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Ellipse2D.Float(centerX - headRadius, centerY - headRadius,
                headRadius * 2, headRadius * 2));

        g2.drawLine(Math.round(centerX + headRadius), Math.round(centerY),
                Math.round(centerX + size * 0.95f), Math.round(centerY));
        g2.drawLine(Math.round(centerX + size * 0.6f), Math.round(centerY),
                Math.round(centerX + size * 0.6f), Math.round(centerY - size * 0.24f));
        g2.drawLine(Math.round(centerX + size * 0.78f), Math.round(centerY),
                Math.round(centerX + size * 0.78f), Math.round(centerY + size * 0.24f));

        g2.setStroke(new BasicStroke(Math.max(1.5f, size * 0.06f)));
        g2.draw(new Ellipse2D.Float(centerX - headRadius * 0.4f, centerY - headRadius * 0.4f,
                headRadius * 0.8f, headRadius * 0.8f));
    }

    private void drawFingerprint(Graphics2D g2, float centerX, float centerY, float size) {
        g2.setColor(new Color(255, 255, 255, 150));
        g2.setStroke(new BasicStroke(Math.max(1.5f, size * 0.06f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 5; i++) {
            float scale = 1f - i * 0.16f;
            RoundRectangle2D.Float ridge = new RoundRectangle2D.Float(
                    centerX - size * scale,
                    centerY - size * scale * 1.2f,
                    size * 2 * scale,
                    size * 2.4f * scale,
                    size * 0.7f * scale,
                    size * 0.7f * scale);
            g2.draw(ridge);
        }
    }

    private void drawGlow(Graphics2D g2, float centerX, float centerY, float radius, Color color) {
        int r = Math.round(radius);
        GradientPaint glow = new GradientPaint(centerX, centerY, new Color(color.getRed(), color.getGreen(), color.getBlue(), 180),
                centerX, centerY + radius, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
        g2.setPaint(glow);
        g2.fillOval(Math.round(centerX - radius), Math.round(centerY - radius), r * 2, r * 2);
    }
}
