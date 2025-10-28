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
            suggestions.add("더 강한 비밀번호를 위해 12자 이상으로 늘려 보세요.");
        } else if (length > 0) {
            score += 10;
            suggestions.add("최소 8자 이상으로 길이를 늘려 주세요.");
        } else {
            suggestions.add("분석을 시작하려면 비밀번호를 입력해 주세요.");
        }

        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSymbol = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        if (hasLower) {
            score += 15;
        } else {
            suggestions.add("소문자를 포함해 보세요.");
        }
        if (hasUpper) {
            score += 15;
        } else {
            suggestions.add("대문자를 추가해 주세요.");
        }
        if (hasDigit) {
            score += 15;
        } else {
            suggestions.add("숫자를 하나 이상 포함해 주세요.");
        }
        if (hasSymbol) {
            score += 15;
        } else {
            suggestions.add("특수문자나 기호를 넣어 주세요.");
        }

        if (REPEATED_CHARS.matcher(password).find()) {
            score -= 10;
            suggestions.add("같은 문자를 여러 번 반복하지 않는 것이 좋아요.");
        }
        if (common && !password.isEmpty()) {
            score = Math.min(score, 20);
            suggestions.add("이 비밀번호는 흔히 사용되는 목록에 포함되어 있습니다.");
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
