package com.example.tunevaultfx.util;

/**
 * Utility class for formatting time values.
 * Converts raw seconds into a readable minutes:seconds format.
 */
public class TimeUtil {

    private TimeUtil() {
    }

    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }
}