package com.projectsexception.myapplist.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.projectsexception.myapplist.PreferenceActivity;
import com.projectsexception.myapplist.R;

public class ThemeManager {
    
    private static int THEME = -1;

    private static final int[] THEMES = {
            R.style.MyAppListTheme,
            R.style.MyAppListThemeLight,
            R.style.MyAppListThemeFlavored,
            R.style.MyAppListThemeLightFlavored
    };

    private static final int[] THEME_NAMES = {
            R.string.theme_dark,
            R.string.theme_light,
            R.string.theme_dark_flavored,
            R.string.theme_light_flavored
    };
    
    public static int getTheme(Context context) {
        if (THEME < 0) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            changeTheme(prefs.getString(PreferenceActivity.KEY_THEME, "0"));
        }
        return THEME;
    }

    public static void changeTheme(String themeString) {
        THEME = THEMES[parseThemeValue(themeString)];
    }

    public static void restartActivity(Activity activity) {
        activity.finish();
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static int getThemeName(String themeValue) {
        return THEME_NAMES[parseThemeValue(themeValue)];
    }

    public static boolean isFlavoredTheme(Context context) {
        int actualTheme = getTheme(context);
        return actualTheme == R.style.MyAppListThemeFlavored || actualTheme == R.style.MyAppListThemeLightFlavored;
    }

    static int parseThemeValue(String themeValue) {
        int value;
        try {
            value = Integer.parseInt(themeValue);
        } catch (Exception e) {
            value = 0;
        }
        return value % THEMES.length;
    }

}
