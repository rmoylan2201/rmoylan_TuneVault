package com.example.tunevaultfx.util;

import java.util.prefs.Preferences;

/** Desktop UI preferences (theme, playback defaults). */
public final class UiPrefs {

    public static final String KEY_THEME_LIGHT = "themeLight";
    public static final String KEY_DEFAULT_SHUFFLE_ON_LOGIN = "defaultShuffleOnLogin";

    private static final String NODE = "com/example/tunevaultfx/ui";

    private UiPrefs() {}

    public static Preferences prefs() {
        return Preferences.userRoot().node(NODE);
    }
}
