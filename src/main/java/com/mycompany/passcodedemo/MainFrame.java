package com.mycompany.passcodedemo;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.im.InputContext;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.Timer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/**
 * Main user interface frame that lets the user experiment with password
 * strength evaluation and displays animated feedback.
 */
public class MainFrame extends JFrame {

    private static final DateTimeFormatter BIRTHDATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final PasswordStrengthChecker checker;
    private final JTextArea feedbackArea = new JTextArea();
    private final JProgressBar strengthBar = new JProgressBar(0, 100);
    private final JPasswordField passwordField = new JPasswordField(24);
    private final Map<JComponent, Font> baseFonts = new HashMap<>();
    private final HackerAnimationPanel hackerAnimationPanel = new HackerAnimationPanel();
    private final SecurityIllustrationPanel illustrationPanel = new SecurityIllustrationPanel();
    private Timer analysisTimer;
    private Timer resetTimer;
    private Clip suspenseClip;
    private boolean suppressLiveFeedback;
    private boolean defeatAnimationShown;
    private UserProfile userProfile;

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

        JLabel title = new JLabel("🛡️ 비밀번호 강도 플레이그라운드", SwingConstants.LEFT);
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

        passwordField.setEchoChar('•');
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 170, 210), 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        passwordField.setBackground(new Color(255, 255, 255));
        passwordField.setFont(passwordField.getFont().deriveFont(Font.PLAIN, 16f));
        passwordField.setCaretColor(new Color(40, 70, 120));
        passwordField.setForeground(new Color(30, 45, 80));
        rememberFont(passwordField);
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!suppressLiveFeedback) {
                    updateFeedback(new String(passwordField.getPassword()));
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!suppressLiveFeedback) {
                    updateFeedback(new String(passwordField.getPassword()));
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!suppressLiveFeedback) {
                    updateFeedback(new String(passwordField.getPassword()));
                }
            }
        });
        fieldPanel.add(passwordField, BorderLayout.CENTER);
        fieldPanel.add(createStartButton(), BorderLayout.EAST);
        card.add(fieldPanel, gbc);

        gbc.gridy++;
        strengthBar.setStringPainted(true);
        strengthBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 140, 180), 1, true),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        strengthBar.setBackground(new Color(235, 240, 255));
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
        feedbackArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        feedbackArea.setForeground(new Color(34, 45, 70));
        feedbackArea.setBackground(new Color(255, 255, 255));
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

    private JButton createStartButton() {
        JButton startButton = new JButton("시작");
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(new Color(82, 120, 220));
        startButton.setFont(startButton.getFont().deriveFont(Font.BOLD, 14f));
        startButton.setOpaque(true);
        startButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 160), 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> {
            clearExistingUserInputs();
            showRegistrationDialog();
        });
        return startButton;
    }

    private void clearExistingUserInputs() {
        stopResetCycle();
        userProfile = null;
        stopAnalysisAnimation();
        suppressLiveFeedback = true;
        passwordField.setText("");
        suppressLiveFeedback = false;
        updateFeedback("");
    }

    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(this, "회원 정보 입력", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GradientPanel background = new GradientPanel();
        background.setLayout(new BorderLayout(18, 18));
        background.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel card = createGlassPanel();
        card.setLayout(new BorderLayout(0, 16));
        card.setPreferredSize(new Dimension(Math.max(480, getWidth() / 2),
                Math.max(520, getHeight() / 2)));

        JLabel title = new JLabel("🧩 당신의 정보를 평소에 회원을 가입하는 방식대로 입력해 주세요", SwingConstants.LEFT);
        title.setForeground(new Color(28, 48, 84));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        card.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        JTextField nameField = new JTextField();
        configureInputField(nameField);
        addFormRow(formPanel, 0, "🙋‍♂️ 이름", nameField);
        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                requestInputLocale(nameField, Locale.KOREAN);
            }
        });

        JTextField emailField = new JTextField();
        configureInputField(emailField);
        addFormRow(formPanel, 1, "✉️ 이메일", emailField);
        emailField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                requestInputLocale(emailField, Locale.ENGLISH);
            }
        });

        JTextField birthField = new JTextField();
        configureInputField(birthField);
        birthField.setToolTipText("예: 2008-05-21");
        addFormRow(formPanel, 2, "🎂 생년월일", birthField);

        JPasswordField passwordInput = new JPasswordField();
        configureInputField(passwordInput);
        passwordInput.setEchoChar('•');
        addFormRow(formPanel, 3, "🔑 비밀번호", passwordInput);

        JPasswordField confirmInput = new JPasswordField();
        configureInputField(confirmInput);
        confirmInput.setEchoChar('•');
        addFormRow(formPanel, 4, "🧾 비밀번호 확인", confirmInput);

        GridBagConstraints fillerConstraints = new GridBagConstraints();
        fillerConstraints.gridx = 0;
        fillerConstraints.gridy = 5;
        fillerConstraints.gridwidth = 2;
        fillerConstraints.weighty = 1;
        fillerConstraints.fill = GridBagConstraints.VERTICAL;
        formPanel.add(Box.createVerticalGlue(), fillerConstraints);

        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formScrollPane.setOpaque(false);
        formScrollPane.getViewport().setOpaque(false);
        formScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(formScrollPane, BorderLayout.CENTER);

        JButton joinButton = new JButton("가입");
        joinButton.setForeground(Color.WHITE);
        joinButton.setBackground(new Color(34, 180, 115));
        joinButton.setFont(joinButton.getFont().deriveFont(Font.BOLD, 16f));
        joinButton.setOpaque(true);
        joinButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 160), 1, true),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));
        joinButton.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
        buttonPanel.add(joinButton, BorderLayout.EAST);
        card.add(buttonPanel, BorderLayout.SOUTH);

        background.add(card, BorderLayout.CENTER);
        dialog.setContentPane(background);
        dialog.pack();
        dialog.setMinimumSize(card.getPreferredSize());
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(this);

        joinButton.addActionListener(e -> {
            List<String> errors = new ArrayList<>();
            String name = nameField.getText().trim();
            if (name.length() < 2) {
                errors.add("이름을 두 글자 이상 입력해주세요.");
            }

            String email = emailField.getText().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.add("올바른 이메일 형식이 아닙니다.");
            }

            String birthText = birthField.getText().trim();
            LocalDate birthDate = null;
            if (!birthText.isEmpty()) {
                try {
                    birthDate = LocalDate.parse(birthText, BIRTHDATE_FORMAT);
                } catch (DateTimeParseException ex) {
                    errors.add("생년월일은 yyyy-MM-dd 형식으로 입력해주세요.");
                }
            } else {
                errors.add("생년월일을 입력해주세요.");
            }

            char[] passwordChars = passwordInput.getPassword();
            char[] confirmChars = confirmInput.getPassword();
            String password = new String(passwordChars);
            String confirm = new String(confirmChars);

            if (password.isEmpty()) {
                errors.add("비밀번호를 입력해주세요.");
            }

            if (!password.equals(confirm)) {
                errors.add("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            }

            if (errors.isEmpty()) {
                userProfile = new UserProfile(name, email, birthDate, password);
                suppressLiveFeedback = true;
                passwordField.setText(password);
                suppressLiveFeedback = false;
                dialog.dispose();
                startAnimatedAnalysis(password);
            } else {
                JOptionPane.showMessageDialog(dialog,
                        String.join("\n", errors),
                        "입력 오류", JOptionPane.ERROR_MESSAGE);
            }

            Arrays.fill(passwordChars, '\0');
            Arrays.fill(confirmChars, '\0');
        });

        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, int rowIndex, String labelText, JComponent field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = rowIndex;
        labelConstraints.anchor = GridBagConstraints.EAST;
        labelConstraints.insets = new Insets(6, 6, 6, 12);

        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(50, 70, 110));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        rememberFont(label);
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = rowIndex;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(6, 0, 6, 6);
        panel.add(field, fieldConstraints);
    }

    private void configureInputField(JTextField field) {
        field.setColumns(18);
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 14f));
        field.setForeground(new Color(30, 45, 80));
        field.setBackground(new Color(255, 255, 255));
        field.setCaretColor(new Color(40, 70, 120));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 170, 210), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        rememberFont(field);
    }

    private void requestInputLocale(Component component, Locale locale) {
        InputContext context = component.getInputContext();
        if (context != null) {
            context.selectInputMethod(locale);
        }
    }

    private JPanel buildVisualPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JPanel animationCard = createGlassPanel();
        animationCard.setLayout(new BorderLayout(0, 12));
        JLabel animationTitle = new JLabel("🕵️‍♂️ 실시간 해커 동향 시뮬레이션", SwingConstants.LEFT);
        animationTitle.setForeground(new Color(28, 48, 84));
        animationTitle.setFont(animationTitle.getFont().deriveFont(Font.BOLD, 16f));
        rememberFont(animationTitle);
        animationCard.add(animationTitle, BorderLayout.NORTH);
        hackerAnimationPanel.setPreferredSize(new Dimension(320, 260));
        animationCard.add(hackerAnimationPanel, BorderLayout.CENTER);
        wrapper.add(animationCard);

        wrapper.add(Box.createVerticalStrut(18));

        JPanel illustrationCard = createGlassPanel();
        illustrationCard.setLayout(new BorderLayout(0, 12));
        JLabel illustrationTitle = new JLabel("🔒 보안 요소 미리보기", SwingConstants.LEFT);
        illustrationTitle.setForeground(new Color(28, 48, 84));
        illustrationTitle.setFont(illustrationTitle.getFont().deriveFont(Font.BOLD, 16f));
        rememberFont(illustrationTitle);
        illustrationCard.add(illustrationTitle, BorderLayout.NORTH);
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
        stopAnalysisAnimation();
        if (password == null || password.isBlank()) {
            showInitialNarrative();
            return;
        }
        PasswordStrengthChecker.Analysis analysis = checker.analyze(password);
        applyAnalysisResult(analysis, password);
    }

    private void showInitialNarrative() {
        hackerAnimationPanel.showSnooping();
        hackerAnimationPanel.updateProgress(0);
        strengthBar.setValue(0);
        strengthBar.setForeground(new Color(82, 120, 220));
        strengthBar.setString("분석 대기 중");
        feedbackArea.setText("초기 감시 상태입니다. 해커가 정보를 노리고 기웃거리고 있어요!\n" +
                "\n'시작' 버튼을 누르고 회원가입을 진행하면 방어 절차가 시작됩니다.");
        feedbackArea.setCaretPosition(0);
    }

    private void applyAnalysisResult(PasswordStrengthChecker.Analysis analysis, String password) {
        String safePassword = password == null ? "" : password;
        strengthBar.setValue(analysis.score());
        strengthBar.setForeground(resolveStrengthColor(analysis.strength()));
        strengthBar.setString("강도: " + translateStrength(analysis.strength())
                + " (" + analysis.score() + "/100)");

        StringBuilder sb = new StringBuilder();
        sb.append("분석 요약\n");
        sb.append(" - 길이: ").append(safePassword.length()).append("자\n");
        sb.append(" - 흔한 비밀번호 여부: ").append(analysis.isCommonPassword() ? "예" : "아니오").append('\n');
        sb.append(" - 강도 등급: ").append(translateStrength(analysis.strength())).append('\n');

        List<String> suggestions = analysis.suggestions();
        List<String> similarityWarnings = collectSimilarityWarnings(safePassword);
        List<String> patternWarnings = collectPatternWarnings(safePassword);

        if (suggestions.isEmpty() && similarityWarnings.isEmpty() && patternWarnings.isEmpty()) {
            sb.append("\n아주 좋아요! 이 비밀번호는 상당히 안전해 보입니다.\n");
        } else {
            if (!suggestions.isEmpty()) {
                sb.append("\n개선 제안:\n");
                for (String suggestion : suggestions) {
                    sb.append(" • ").append(suggestion).append('\n');
                }
            }

            if (!similarityWarnings.isEmpty()) {
                sb.append("\n개인 정보 기반 경고:\n");
                for (String warning : similarityWarnings) {
                    sb.append(" • ").append(warning).append('\n');
                }
            }

            if (!patternWarnings.isEmpty()) {
                sb.append("\n패턴 경고:\n");
                for (String warning : patternWarnings) {
                    sb.append(" • ").append(warning).append('\n');
                }
            }
        }
        feedbackArea.setText(sb.toString());
        feedbackArea.setCaretPosition(0);
    }

    private List<String> collectSimilarityWarnings(String password) {
        List<String> warnings = new ArrayList<>();
        if (userProfile == null || password == null || password.isBlank()) {
            return warnings;
        }

        String lowerPassword = password.toLowerCase();

        if (userProfile.name != null && !userProfile.name.isBlank()) {
            String[] nameTokens = userProfile.name.toLowerCase().split("\\s+");
            for (String token : nameTokens) {
                if (token.length() >= 2 && lowerPassword.contains(token)) {
                    addUniqueWarning(warnings, "비밀번호에 이름과 유사한 문자열이 포함되어 있습니다.");
                    break;
                }
            }
            appendPersonalNumberWarning(warnings, userProfile.name, password,
                    "비밀번호에 이름과 연결된 숫자(%s)가 포함되어 있습니다.");
        }

        if (userProfile.email != null && !userProfile.email.isBlank()) {
            String emailLower = userProfile.email.toLowerCase();
            if (lowerPassword.contains(emailLower)) {
                addUniqueWarning(warnings, "비밀번호에 이메일 전체가 포함되어 있습니다.");
            }
            int atIndex = emailLower.indexOf('@');
            if (atIndex > 0) {
                String localPart = emailLower.substring(0, atIndex);
                if (localPart.length() >= 3 && lowerPassword.contains(localPart)) {
                    addUniqueWarning(warnings, "비밀번호에 이메일 아이디 부분이 포함되어 있습니다.");
                }
                String domainPart = emailLower.substring(atIndex + 1);
                if (!domainPart.isBlank() && lowerPassword.contains(domainPart)) {
                    addUniqueWarning(warnings, "비밀번호에 이메일 도메인이 포함되어 있습니다.");
                }
            }
            appendPersonalNumberWarning(warnings, userProfile.email, password,
                    "비밀번호에 이메일과 관련된 숫자(%s)가 포함되어 있습니다.");
        }

        if (userProfile.birthDate != null) {
            String digits = userProfile.birthDate.format(DateTimeFormatter.BASIC_ISO_DATE);
            if (password.contains(digits)) {
                addUniqueWarning(warnings, "비밀번호에 생년월일(" + digits + ")이 그대로 포함되어 있습니다.");
            }
            String year = String.valueOf(userProfile.birthDate.getYear());
            if (password.contains(year)) {
                addUniqueWarning(warnings, "비밀번호에 출생 연도(" + year + ")가 포함되어 있습니다.");
            }
            String yearSuffix = year.substring(Math.max(0, year.length() - 2));
            if (!yearSuffix.equals(year) && password.contains(yearSuffix)) {
                addUniqueWarning(warnings, "비밀번호에 출생 연도의 말미 숫자(" + yearSuffix + ")가 포함되어 있습니다.");
            }
            String monthDay = String.format("%02d%02d", userProfile.birthDate.getMonthValue(),
                    userProfile.birthDate.getDayOfMonth());
            if (password.contains(monthDay)) {
                addUniqueWarning(warnings, "비밀번호에 생일(月日) 조합(" + monthDay + ")이 포함되어 있습니다.");
            }
        }

        return warnings;
    }

    private void appendPersonalNumberWarning(List<String> warnings, String source, String password,
            String messageTemplate) {
        if (source == null || password == null) {
            return;
        }
        String digits = source.replaceAll("\\D+", "");
        if (digits.length() < 2) {
            return;
        }
        int maxLength = Math.min(6, digits.length());
        for (int length = maxLength; length >= 2; length--) {
            for (int start = 0; start <= digits.length() - length; start++) {
                String fragment = digits.substring(start, start + length);
                if (!fragment.isBlank() && password.contains(fragment)) {
                    addUniqueWarning(warnings, String.format(messageTemplate, fragment));
                    return;
                }
            }
        }
    }

    private List<String> collectPatternWarnings(String password) {
        List<String> warnings = new ArrayList<>();
        if (password == null || password.isBlank()) {
            return warnings;
        }

        if (containsSequentialDigits(password, 3)) {
            addUniqueWarning(warnings, "연속된 숫자 패턴이 감지되었습니다. 순차적인 숫자는 쉽게 추측될 수 있습니다.");
        }

        return warnings;
    }

    private boolean containsSequentialDigits(String password, int minLength) {
        int count = 1;
        Integer lastDigit = null;
        Integer direction = null;
        for (char ch : password.toCharArray()) {
            if (Character.isDigit(ch)) {
                int current = ch - '0';
                if (lastDigit != null) {
                    int diff = current - lastDigit;
                    if (diff == 1 || diff == -1) {
                        if (direction == null || diff == direction) {
                            direction = diff;
                            count++;
                        } else {
                            direction = diff;
                            count = 2;
                        }
                    } else {
                        direction = null;
                        count = 1;
                    }
                } else {
                    count = 1;
                }
                lastDigit = current;
                if (count >= minLength) {
                    return true;
                }
            } else {
                lastDigit = null;
                direction = null;
                count = 0;
            }
        }
        return false;
    }

    private void startAnimatedAnalysis(String password) {
        stopAnalysisAnimation();
        stopResetCycle();
        defeatAnimationShown = false;
        PasswordStrengthChecker.Analysis analysis = checker.analyze(password);
        final long duration = 10_000L;
        final long startTime = System.currentTimeMillis();
        strengthBar.setForeground(new Color(82, 120, 220));
        strengthBar.setValue(0);
        strengthBar.setString("방어 강화 중... 0%");
        feedbackArea.setText("해커가 열쇠를 돌려 잠금을 해제하려고 합니다!\n" +
                "진행률이 오르는 동안 방어 분석이 강화되고 있어요.");
        feedbackArea.setCaretPosition(0);

        playSuspenseSound(duration);
        hackerAnimationPanel.showAttempting();
        hackerAnimationPanel.updateProgress(0);

        analysisTimer = new Timer(40, event -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = Math.min(1.0, (double) elapsed / duration);
            int percent = (int) Math.round(progress * 100);
            strengthBar.setValue(percent);
            strengthBar.setString(String.format("방어 강화 중... %d%%", percent));
            hackerAnimationPanel.updateProgress(progress);
            if (!defeatAnimationShown && progress >= 0.3) {
                defeatAnimationShown = true;
                hackerAnimationPanel.showDefeated();
                feedbackArea.append("\n\n해커가 결국 포기하고 항복했습니다!\n분석 결과를 정리하는 중이에요.");
                feedbackArea.setCaretPosition(0);
            }
            if (progress >= 1.0) {
                Timer source = (Timer) event.getSource();
                source.stop();
                analysisTimer = null;
                stopSuspenseSound();
                applyAnalysisResult(analysis, password);
                scheduleResetCycle();
            }
        });
        analysisTimer.setCoalesce(true);
        analysisTimer.start();
    }

    private void stopAnalysisAnimation() {
        if (analysisTimer != null) {
            analysisTimer.stop();
            analysisTimer = null;
        }
        stopSuspenseSound();
        stopResetCycle();
    }

    private void stopSuspenseSound() {
        if (suspenseClip != null) {
            suspenseClip.stop();
            suspenseClip.close();
            suspenseClip = null;
        }
    }

    private void stopResetCycle() {
        if (resetTimer != null) {
            resetTimer.stop();
            resetTimer = null;
        }
    }

    private void scheduleResetCycle() {
        stopResetCycle();
        resetTimer = new Timer(10_000, e -> {
            Timer source = (Timer) e.getSource();
            source.stop();
            resetTimer = null;
            resetToInitialState();
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }

    private void resetToInitialState() {
        suppressLiveFeedback = true;
        passwordField.setText("");
        suppressLiveFeedback = false;
        userProfile = null;
        defeatAnimationShown = false;
        showInitialNarrative();
    }

    private void playSuspenseSound(long durationMillis) {
        stopSuspenseSound();
        try {
            AudioFormat format = new AudioFormat(44100f, 16, 1, true, false);
            int totalSamples = (int) (format.getSampleRate() * durationMillis / 1000.0);
            byte[] data = new byte[totalSamples * 2];
            double sampleRate = format.getSampleRate();
            for (int i = 0; i < totalSamples; i++) {
                double sweep = 180 + 140 * Math.sin(2 * Math.PI * i / (sampleRate / 1.7));
                double tremolo = 0.4 + 0.6 * Math.sin(2 * Math.PI * i / (sampleRate / 6.0));
                double envelope = Math.pow(1 - (double) i / totalSamples, 0.4);
                double sample = Math.sin(2 * Math.PI * sweep * i / sampleRate) * tremolo * envelope;
                short value = (short) (sample * Short.MAX_VALUE);
                data[2 * i] = (byte) (value & 0xFF);
                data[2 * i + 1] = (byte) ((value >> 8) & 0xFF);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            try (AudioInputStream ais = new AudioInputStream(bais, format, totalSamples)) {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
                suspenseClip = clip;
            }
        } catch (LineUnavailableException | IOException ex) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void addUniqueWarning(List<String> warnings, String message) {
        if (!warnings.contains(message)) {
            warnings.add(message);
        }
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

    private static class UserProfile {
        final String name;
        final String email;
        final LocalDate birthDate;
        final String password;

        UserProfile(String name, String email, LocalDate birthDate, String password) {
            this.name = name;
            this.email = email;
            this.birthDate = birthDate;
            this.password = password;
        }
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
