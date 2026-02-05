package com.example.util;

public final class PhoneUtil {

    private PhoneUtil() {}

    /**
     * Normalize phone numbers:
     * - trims
     * - keeps only digits and an optional leading '+'
     * - converts leading "00" to "+"
     * Examples:
     *  "+90-555 0000" -> "+905550000"
     *  "0044 20 7946 0958" -> "+442079460958"
     */
    public static String normalize(String raw) {
        if (raw == null) return "";

        String s = raw.trim();
        if (s.isEmpty()) return "";

        StringBuilder out = new StringBuilder();
        boolean plusUsed = false;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (Character.isDigit(ch)) {
                out.append(ch);
            } else if (ch == '+' && !plusUsed && out.length() == 0) {
                out.append(ch);
                plusUsed = true;
            }
            // everything else ignored: spaces, '-', '(', ')', etc.
        }

        String normalized = out.toString();

        // Convert leading 00... to +...
        if (normalized.startsWith("00")) {
            normalized = "+" + normalized.substring(2);
        }

        return normalized;
    }

    /** Validate normalized phones: optional leading '+', then digits, 7..16 digits. */
    public static boolean isValidNormalized(String normalized) {
        if (normalized == null) return false;
        if (normalized.isEmpty()) return false;
        if (normalized.equals("+")) return false;

        String digits = normalized.startsWith("+") ? normalized.substring(1) : normalized;

        if (digits.isEmpty()) return false;
        for (int i = 0; i < digits.length(); i++) {
            if (!Character.isDigit(digits.charAt(i))) return false;
        }

        int len = digits.length();
        return len >= 7 && len <= 16;
    }

    public static String normalizeAndValidateOrThrow(String raw) {
        String n = normalize(raw);
        if (!isValidNormalized(n)) {
            throw new IllegalArgumentException("Phone must be 7–16 digits (optionally starting with '+'). You may also start with 00 for international prefix.");
        }
        return n;
    }

    public static String trimToNull(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }
}
