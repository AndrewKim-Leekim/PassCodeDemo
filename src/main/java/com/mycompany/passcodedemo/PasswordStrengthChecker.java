package com.mycompany.passcodedemo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Provides password strength calculations and human-friendly suggestions.
 */
public class PasswordStrengthChecker {

    private static final Pattern REPEATED_CHARS = Pattern.compile("(.)\\1{2,}");

    private final Set<String> commonPasswords;

    public PasswordStrengthChecker(Set<String> commonPasswords) {
        this.commonPasswords = new HashSet<>();
        for (String entry : commonPasswords) {
            if (entry != null) {
                this.commonPasswords.add(entry.trim().toLowerCase());
            }
        }
    }

    /**
     * Analyses the provided password returning a score, strength category and
     * suggestions for improvements.
     */
    public Analysis analyze(String password) {
        if (password == null) {
            password = "";
        }

        String normalized = password.toLowerCase();
        boolean common = commonPasswords.contains(normalized);

        int score = 0;
        List<String> suggestions = new ArrayList<>();

        int length = password.length();
        if (length >= 12) {
            score += 40;
        } else if (length >= 8) {
            score += 20;
            suggestions.add("Use 12 or more characters for better strength.");
        } else if (length > 0) {
            score += 10;
            suggestions.add("Increase the length to at least 8 characters.");
        } else {
            suggestions.add("Enter a password to get started.");
        }

        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSymbol = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        if (hasLower) {
            score += 15;
        } else {
            suggestions.add("Add lowercase letters.");
        }
        if (hasUpper) {
            score += 15;
        } else {
            suggestions.add("Add uppercase letters.");
        }
        if (hasDigit) {
            score += 15;
        } else {
            suggestions.add("Include at least one number.");
        }
        if (hasSymbol) {
            score += 15;
        } else {
            suggestions.add("Include punctuation or symbols.");
        }

        if (REPEATED_CHARS.matcher(password).find()) {
            score -= 10;
            suggestions.add("Avoid repeating the same character several times.");
        }
        if (common && !password.isEmpty()) {
            score = Math.min(score, 20);
            suggestions.add("This password appears in common password lists.");
        }

        score = Math.max(0, Math.min(100, score));

        Strength strength;
        if (score >= 80) {
            strength = Strength.STRONG;
        } else if (score >= 50) {
            strength = Strength.MODERATE;
        } else {
            strength = Strength.WEAK;
        }

        return new Analysis(strength, score, common, List.copyOf(suggestions));
    }

    /**
     * Strength categories used by the UI.
     */
    public enum Strength {
        WEAK,
        MODERATE,
        STRONG
    }

    /**
     * Immutable view of a password analysis result.
     */
    public record Analysis(Strength strength, int score, boolean isCommonPassword,
            List<String> suggestions) {
    }
}
