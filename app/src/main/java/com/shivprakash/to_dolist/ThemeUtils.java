package com.shivprakash.to_dolist;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class ThemeUtils {
    public static void applySavedTheme(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String theme = prefs.getString("pref_theme", "blue");
        int style = R.style.ThemeOverlay_Material3_ActionBar; // fallback overlay
        ctx.getTheme().applyStyle(style, true);
    }
    public static boolean isSoundEnabled(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("pref_sound", true);
    }
}
