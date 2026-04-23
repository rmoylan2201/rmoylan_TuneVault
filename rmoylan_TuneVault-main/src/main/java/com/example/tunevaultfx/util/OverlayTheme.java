package com.example.tunevaultfx.util;

/**
 * Inline styles for modal overlays so they follow light/dark mode.
 */
public final class OverlayTheme {

    private OverlayTheme() {}

    public static String backdrop() {
        return AppTheme.isLightMode()
                ? "-fx-background-color: rgba(15,23,42,0.42);"
                : "-fx-background-color: rgba(3,2,14,0.72);";
    }

    public static String card() {
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: #f8fafc;"
                    + "-fx-background-radius: 20;"
                    + "-fx-border-color: rgba(124,58,237,0.22);"
                    + "-fx-border-radius: 20; -fx-border-width: 1;"
                    + "-fx-effect: dropshadow(gaussian, rgba(76,29,149,0.12), 28, 0, 0, 6);";
        }
        return "-fx-background-color: #0f0f1c;"
                + "-fx-background-radius: 24;"
                + "-fx-border-color: rgba(139,92,246,0.16);"
                + "-fx-border-radius: 24; -fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.70), 48, 0, 0, 16);";
    }

    public static String title() {
        return "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + CellStyleKit.getTextPrimary() + ";";
    }

    public static String subtitle() {
        return "-fx-font-size: 12px; -fx-text-fill: " + CellStyleKit.getTextSecondary() + ";";
    }

    public static String primaryButton() {
        return "-fx-background-color: #8b5cf6; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.35), 12, 0, 0, 3);";
    }

    public static String primaryButtonHover() {
        return "-fx-background-color: #7c3aed; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;";
    }

    public static String secondaryButton() {
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: rgba(15,23,42,0.06); -fx-text-fill: " + CellStyleKit.getTextSecondary() + ";"
                    + "-fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;"
                    + "-fx-border-color: rgba(15,23,42,0.12); -fx-border-radius: 12; -fx-border-width: 1;";
        }
        return "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #9d9db8;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;"
                + "-fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 12; -fx-border-width: 1;";
    }

    /** Destructive action (e.g. delete confirm). */
    public static String dangerButton() {
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: rgba(220,38,38,0.14); -fx-text-fill: #b91c1c;"
                    + "-fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;"
                    + "-fx-border-color: rgba(220,38,38,0.28); -fx-border-radius: 12; -fx-border-width: 1;";
        }
        return "-fx-background-color: rgba(239,68,68,0.16); -fx-text-fill: #fca5a5;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;"
                + "-fx-border-color: rgba(239,68,68,0.30); -fx-border-radius: 12; -fx-border-width: 1;";
    }

    public static String dangerButtonHover() {
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: rgba(220,38,38,0.22); -fx-text-fill: #991b1b;"
                    + "-fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;";
        }
        return "-fx-background-color: rgba(239,68,68,0.26); -fx-text-fill: #fecaca;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;";
    }

    public static String secondaryButtonHover() {
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: rgba(124,58,237,0.10); -fx-text-fill: " + CellStyleKit.getTextPrimary() + ";"
                    + "-fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;"
                    + "-fx-border-color: rgba(124,58,237,0.25); -fx-border-radius: 12; -fx-border-width: 1;";
        }
        return "-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: #eeeef6;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-cursor: hand;"
                + "-fx-border-color: rgba(255,255,255,0.14); -fx-border-radius: 12; -fx-border-width: 1;";
    }

    /** Small anchored menu (three-dot, etc.). No drop shadow — avoids black corner artifacts on transparent popup windows. */
    public static String popupShell() {
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: #ffffff;"
                    + "-fx-background-radius: 16;"
                    + "-fx-border-color: rgba(15,23,42,0.14);"
                    + "-fx-border-radius: 16; -fx-border-width: 1;";
        }
        return "-fx-background-color: #16162a;"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: rgba(255,255,255,0.14);"
                + "-fx-border-radius: 18; -fx-border-width: 1;";
    }

    public static String popupItemBase() {
        String fill = AppTheme.isLightMode() ? "#0f172a" : "#e0e0f0";
        return "-fx-background-color: transparent; -fx-background-radius: 10;"
                + "-fx-font-size: 13px; -fx-padding: 0 12 0 12; -fx-text-fill: " + fill + ";";
    }

    public static String popupItemHover() {
        String fill = AppTheme.isLightMode() ? "#0f172a" : "#e0e0f0";
        String hoverBg = AppTheme.isLightMode() ? "rgba(124,58,237,0.08)" : "rgba(255,255,255,0.07)";
        return "-fx-background-color: " + hoverBg + "; -fx-background-radius: 10;"
                + "-fx-font-size: 13px; -fx-padding: 0 12 0 12; -fx-text-fill: " + fill + ";";
    }

    public static String popupItemDestructiveBase() {
        String fill = AppTheme.isLightMode() ? "#b91c1c" : "#fda4af";
        String hoverBg = AppTheme.isLightMode() ? "rgba(220,38,38,0.10)" : "rgba(244,63,94,0.12)";
        return "-fx-background-color: transparent; -fx-background-radius: 10;"
                + "-fx-font-size: 13px; -fx-padding: 0 12 0 12; -fx-text-fill: " + fill + ";";
    }

    public static String popupItemDestructiveHover() {
        String fill = AppTheme.isLightMode() ? "#b91c1c" : "#fda4af";
        String hoverBg = AppTheme.isLightMode() ? "rgba(220,38,38,0.10)" : "rgba(244,63,94,0.12)";
        return "-fx-background-color: " + hoverBg + "; -fx-background-radius: 10;"
                + "-fx-font-size: 13px; -fx-padding: 0 12 0 12; -fx-text-fill: " + fill + ";";
    }

    public static String createPlaylistField() {
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: #ffffff; -fx-text-fill: #0f172a;"
                    + "-fx-prompt-text-fill: #94a3b8;"
                    + "-fx-border-color: rgba(15,23,42,0.12); -fx-border-radius: 10; -fx-background-radius: 10;"
                    + "-fx-border-width: 1; -fx-padding: 10 12 10 12;";
        }
        return "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #eeeef6;"
                + "-fx-prompt-text-fill: #5c5c78;"
                + "-fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 10; -fx-background-radius: 10;"
                + "-fx-border-width: 1; -fx-padding: 10 12 10 12;";
    }

    public static String pickerRowHover() {
        return AppTheme.isLightMode()
                ? "-fx-background-color: rgba(124,58,237,0.08); -fx-background-radius: 14;"
                : "-fx-background-color: rgba(139,92,246,0.06); -fx-background-radius: 14;";
    }

    public static String pickerIconGlyph() {
        return "-fx-font-size: 12px; -fx-text-fill: " + (AppTheme.isLightMode() ? "#5b21b6" : "#a78bfa") + ";";
    }

    public static String pickerIconBox() {
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: rgba(124,58,237,0.12);"
                    + "-fx-background-radius: 10;"
                    + "-fx-border-color: rgba(124,58,237,0.22);"
                    + "-fx-border-radius: 10; -fx-border-width: 1;";
        }
        return "-fx-background-color: rgba(139,92,246,0.14);"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: rgba(139,92,246,0.22);"
                + "-fx-border-radius: 10; -fx-border-width: 1;";
    }
}
