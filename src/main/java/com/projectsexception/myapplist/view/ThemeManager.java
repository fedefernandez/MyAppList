package com.projectsexception.myapplist.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.projectsexception.myapplist.PreferenceActivity;
import com.projectsexception.myapplist.R;

public class ThemeManager {
    
    private static int THEME = 0;
    
    public static int getTheme(Context context) {
        if (THEME == 0) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            changeTheme(prefs.getString(PreferenceActivity.KEY_THEME, "0"));
        }
        return THEME;
    }

    public static void changeTheme(String themeString) {
        int theme = Integer.parseInt(themeString);
        if (theme == 0) {
            THEME = R.style.MyAppListTheme;
        } else {
            THEME = R.style.MyAppListThemeLight;
        }
    }
    
    public static int chooseDrawable(Context context, int darkDrawable, int lightDrawable) {
        int theme = getTheme(context);
        if (theme == R.style.MyAppListTheme) {
            return darkDrawable;
        } else {
            return lightDrawable;
        }
    }
    
    public static void restartActivity(Activity activity) {
        activity.finish();
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

}
