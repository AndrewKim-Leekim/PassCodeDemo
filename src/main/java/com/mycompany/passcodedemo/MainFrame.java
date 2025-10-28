package com.mycompany.passcodedemo;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentAdapter;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Main user interface frame that lets the user experiment with password
 * strength evaluation and displays animated feedback.
 */
public class MainFrame extends JFrame {

    private final PasswordStrengthChecker checker;
    private final JTextArea feedbackArea = new JTextArea();
    private final JProgressBar strengthBar = new JProgressBar(0, 100);
    private final Map<JComponent, Font> baseFonts = new HashMap<>();
    private final SecurityIllustrationPanel illustrationPanel = new SecurityIllustrationPanel();

    public MainFrame() {
        super("패스코드 데모");
        this.checker = createChecker();
        configureWindow();
        add(buildContent());
        installResponsiveBehavior();
        applyResponsiveScale();
        updateFeedback("");
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 560));
        setSize(920, 580);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private PasswordStrengthChecker createChecker() {
        try {
            Set<String> dictionary = DictionaryLoader.loadCommonPasswords();
            return new PasswordStrengthChecker(dictionary);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "공통 비밀번호 목록을 불러오지 못했습니다. 강도 분석이 제한됩니다.\n" + ex.getMessage(),
                    "사전 로드 오류", JOptionPane.ERROR_MESSAGE);
            return new PasswordStrengthChecker(Set.of());
        }
    }

    private JComponent buildContent() {
        GradientPanel background = new GradientPanel();
        background.setLayout(new BorderLayout(24, 20));
        background.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        background.add(buildHeaderPanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildInputPanel(), buildVisualPanel());
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(6);
        splitPane.setOpaque(false);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        background.add(splitPane, BorderLayout.CENTER);

        return background;
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("비밀번호 강도 플레이그라운드", SwingConstants.LEFT);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        rememberFont(title);
        header.add(title);

        header.add(Box.createVerticalStrut(6));

        JLabel tagline = new JLabel("당신이 평소에 자주 사용하는 비밀번호 얼마나 강력할까요? 지금 확인해 보세요.",
                SwingConstants.LEFT);
        tagline.setForeground(new Color(230, 240, 255));
        tagline.setFont(tagline.getFont().deriveFont(Font.PLAIN, 18f));
        rememberFont(tagline);
        header.add(tagline);

        header.add(Box.createVerticalStrut(6));

        JLabel author = new JLabel("작성자: 소담고 1학년 김 민혁", SwingConstants.LEFT);
        author.setForeground(new Color(210, 225, 255));
        author.setFont(author.getFont().deriveFont(Font.PLAIN, 16f));
        rememberFont(author);
        header.add(author);

        header.add(Box.createVerticalStrut(12));
        return header;
    }

    private JPanel buildInputPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel card = createGlassPanel();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        JLabel passwordLabel = new JLabel("🔐 비밀번호를 입력하세요", SwingConstants.LEFT);
        passwordLabel.setForeground(new Color(28, 48, 84));
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(Font.BOLD, 18f));
        rememberFont(passwordLabel);
        card.add(passwordLabel, gbc);

        gbc.gridy++;
        JPanel fieldPanel = new JPanel(new BorderLayout(8, 0));
        fieldPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("🛡️", SwingConstants.CENTER);
        iconLabel.setFont(iconLabel.getFont().deriveFont(Font.PLAIN, 22f));
        rememberFont(iconLabel);
        fieldPanel.add(iconLabel, BorderLayout.WEST);

        JPasswordField passwordField = new JPasswordField(24);
        passwordField.setEchoChar('•');
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 170, 210), 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        passwordField.setBackground(new Color(255, 255, 255, 240));
        passwordField.setFont(passwordField.getFont().deriveFont(Font.PLAIN, 16f));
        passwordField.setCaretColor(new Color(40, 70, 120));
        passwordField.setForeground(new Color(30, 45, 80));
        rememberFont(passwordField);
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFeedback(new String(passwordField.getPassword()));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFeedback(new String(passwordField.getPassword()));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFeedback(new String(passwordField.getPassword()));
            }
        });
        fieldPanel.add(passwordField, BorderLayout.CENTER);
        card.add(fieldPanel, gbc);

        gbc.gridy++;
        strengthBar.setStringPainted(true);
        strengthBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 140, 180), 1, true),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        strengthBar.setBackground(new Color(235, 240, 255, 200));
        strengthBar.setForeground(new Color(34, 180, 115));
        strengthBar.setFont(strengthBar.getFont().deriveFont(Font.BOLD, 14f));
        rememberFont(strengthBar);
        card.add(strengthBar, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14f));
        feedbackArea.setForeground(new Color(34, 45, 70));
        feedbackArea.setBackground(new Color(255, 255, 255, 230));
        feedbackArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        rememberFont(feedbackArea);
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        card.add(scrollPane, gbc);

        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel hintLabel = new JLabel("💡 다양한 문자 조합과 길이를 섞으면 안전도가 높아집니다.", SwingConstants.LEFT);
        hintLabel.setForeground(new Color(60, 80, 120));
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.PLAIN, 14f));
        rememberFont(hintLabel);
        card.add(hintLabel, gbc);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildVisualPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JPanel animationCard = createGlassPanel();
        animationCard.setLayout(new BorderLayout(0, 12));
        JLabel animationTitle = new JLabel("실시간 해커 방어 시뮬레이션", SwingConstants.LEFT);
        animationTitle.setForeground(new Color(28, 48, 84));
        animationTitle.setFont(animationTitle.getFont().deriveFont(Font.BOLD, 16f));
        rememberFont(animationTitle);
        animationCard.add(animationTitle, BorderLayout.NORTH);
        HackerAnimationPanel animationPanel = new HackerAnimationPanel();
        animationPanel.setPreferredSize(new Dimension(320, 260));
        animationCard.add(animationPanel, BorderLayout.CENTER);
        wrapper.add(animationCard);

        wrapper.add(Box.createVerticalStrut(18));

        JPanel illustrationCard = createGlassPanel();
        illustrationCard.setLayout(new BorderLayout());
        illustrationPanel.setPreferredSize(new Dimension(320, 220));
        illustrationCard.add(illustrationPanel, BorderLayout.CENTER);
        wrapper.add(illustrationCard);

        return wrapper;
    }

    private JPanel createGlassPanel() {
        return new JPanel() {
            {
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            }

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();
                g2.setColor(new Color(255, 255, 255, 185));
                g2.fillRoundRect(6, 6, width - 12, height - 12, 28, 28);
                g2.setColor(new Color(255, 255, 255, 90));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(6, 6, width - 12, height - 12, 28, 28);
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    private void updateFeedback(String password) {
        PasswordStrengthChecker.Analysis analysis = checker.analyze(password);
        strengthBar.setValue(analysis.score());
        strengthBar.setForeground(resolveStrengthColor(analysis.strength()));
        strengthBar.setString("강도: " + translateStrength(analysis.strength())
                + " (" + analysis.score() + "/100)");

        StringBuilder sb = new StringBuilder();
        sb.append("분석 요약\n");
        sb.append(" - 길이: ").append(password.length()).append("자\n");
        sb.append(" - 흔한 비밀번호 여부: ").append(analysis.isCommonPassword() ? "예" : "아니오").append('\n');
        sb.append(" - 강도 등급: ").append(translateStrength(analysis.strength())).append('\n');

        List<String> suggestions = analysis.suggestions();
        if (suggestions.isEmpty()) {
            sb.append("\n아주 좋아요! 이 비밀번호는 상당히 안전해 보입니다.\n");
        } else {
            sb.append("\n개선 제안:\n");
            for (String suggestion : suggestions) {
                sb.append(" • ").append(suggestion).append('\n');
            }
        }
        feedbackArea.setText(sb.toString());
    }

    private Color resolveStrengthColor(PasswordStrengthChecker.Strength strength) {
        return switch (strength) {
            case STRONG -> new Color(46, 198, 118);
            case MODERATE -> new Color(255, 173, 65);
            case WEAK -> new Color(220, 82, 82);
        };
    }

    private String translateStrength(PasswordStrengthChecker.Strength strength) {
        return switch (strength) {
            case STRONG -> "강함";
            case MODERATE -> "보통";
            case WEAK -> "약함";
        };
    }

    private void installResponsiveBehavior() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveScale();
            }
        });
    }

    private void applyResponsiveScale() {
        float scale = Math.max(0.85f, Math.min(1.6f, (float) getWidth() / 920f));
        for (Map.Entry<JComponent, Font> entry : baseFonts.entrySet()) {
            Font base = entry.getValue();
            Component component = entry.getKey();
            component.setFont(base.deriveFont(base.getSize2D() * scale));
        }
        illustrationPanel.setScaleMultiplier(scale);
        revalidate();
        repaint();
    }

    private void rememberFont(JComponent component) {
        baseFonts.put(component, component.getFont());
    }

    private static class GradientPanel extends JPanel {
        GradientPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            int width = getWidth();
            int height = getHeight();
            GradientPaint gradient = new GradientPaint(0, 0,
                    new Color(18, 32, 60), width, height, new Color(42, 98, 140));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, width, height);
            g2.setPaint(new Color(255, 255, 255, 40));
            g2.fillOval(width / 2, -height / 2, width, height);
            g2.dispose();
        }
    }
}
