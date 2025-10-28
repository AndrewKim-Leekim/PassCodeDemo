package com.mycompany.passcodedemo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
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

    public MainFrame() {
        super("PassCode Demo");
        this.checker = createChecker();
        configureWindow();
        add(buildContent());
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private PasswordStrengthChecker createChecker() {
        try {
            Set<String> dictionary = DictionaryLoader.loadCommonPasswords();
            return new PasswordStrengthChecker(dictionary);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load common password list. Strength checks will be limited.\n" + ex.getMessage(),
                    "Dictionary Error", JOptionPane.ERROR_MESSAGE);
            return new PasswordStrengthChecker(Set.of());
        }
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(buildInputPanel(), BorderLayout.CENTER);

        HackerAnimationPanel animationPanel = new HackerAnimationPanel();
        animationPanel.setPreferredSize(new Dimension(260, 0));
        content.add(animationPanel, BorderLayout.EAST);

        return content;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Password Strength Playground", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(title, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Enter a password:"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(24);
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
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        strengthBar.setStringPainted(true);
        strengthBar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(strengthBar, gbc);

        gbc.gridy++;
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        feedbackArea.setBackground(new Color(250, 250, 250));
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        scrollPane.setPreferredSize(new Dimension(320, 200));
        panel.add(scrollPane, gbc);

        updateFeedback("");

        return panel;
    }

    private void updateFeedback(String password) {
        PasswordStrengthChecker.Analysis analysis = checker.analyze(password);
        strengthBar.setValue(analysis.score());
        strengthBar.setForeground(switch (analysis.strength()) {
            case STRONG -> new Color(0, 153, 51);
            case MODERATE -> new Color(255, 153, 0);
            case WEAK -> new Color(204, 0, 0);
        });
        strengthBar.setString(analysis.strength().name() + " (" + analysis.score() + "/100)");

        StringBuilder sb = new StringBuilder();
        sb.append("Analysis:\n");
        sb.append(" - Length: ").append(password.length()).append(" characters\n");
        sb.append(" - Common password: ").append(analysis.isCommonPassword() ? "yes" : "no").append('\n');

        List<String> suggestions = analysis.suggestions();
        if (suggestions.isEmpty()) {
            sb.append("\nGreat job! This password looks solid.\n");
        } else {
            sb.append("\nSuggestions:\n");
            for (String suggestion : suggestions) {
                sb.append(" • ").append(suggestion).append('\n');
            }
        }
        feedbackArea.setText(sb.toString());
    }
}
