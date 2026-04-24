package com.example.tunevaultfx.profile;

import java.util.Locale;

/** Small display helpers for the profile page. */
final class ProfileTextFormat {

    private ProfileTextFormat() {}

    static String initialsFor(String username) {
        String t = username == null ? "" : username.trim();
        if (t.isEmpty()) {
            return "?";
        }
        if (t.length() == 1) {
            return t.toUpperCase(Locale.ROOT);
        }
        int cp = t.codePointAt(0);
        String first = new String(Character.toChars(cp)).toUpperCase(Locale.ROOT);
        int i = Character.charCount(cp);
        if (i >= t.length()) {
            return first;
        }
        int cp2 = t.codePointAt(i);
        String second = new String(Character.toChars(cp2)).toUpperCase(Locale.ROOT);
        return first + second;
    }

    static String formatNumber(int n) {
        return String.format(Locale.US, "%,d", n);
    }

    static String formatListeningDuration(long seconds) {
        if (seconds <= 0) {
            return "0 minutes";
        }
        long m = seconds / 60;
        if (m < 60) {
            return m + (m == 1 ? " minute" : " minutes");
        }
        long h = m / 60;
        long remM = m % 60;
        if (h < 48) {
            return h + "h " + remM + "m";
        }
        long d = h / 24;
        return d + (d == 1 ? " day" : " days") + " of play time";
    }
}
