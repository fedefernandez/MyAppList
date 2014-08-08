package com.projectsexception.myapplist;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.projectsexception.myapplist.app.MyAppListApplication;
import com.projectsexception.myapplist.view.ThemeManager;

public class BaseActivity extends ActionBarActivity {
    
    private int mTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTheme = ThemeManager.getTheme(this);
        setTheme(mTheme);
        super.onCreate(savedInstanceState);
        // Set screen name.
        // Where path is a String representing the screen name.
        ((MyAppListApplication)getApplication()).analyticsSetScreenName(((Object) this).getClass().getSimpleName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Send a screen view.
        ((MyAppListApplication)getApplication()).analyticsSend();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTheme != ThemeManager.getTheme(this)) {
            ThemeManager.restartActivity(this);
        }
    }

}
