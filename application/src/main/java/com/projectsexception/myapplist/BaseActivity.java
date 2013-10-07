package com.projectsexception.myapplist;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.projectsexception.myapplist.view.ThemeManager;

public class BaseActivity extends ActionBarActivity {
    
    private int mTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTheme = ThemeManager.getTheme(this);
        setTheme(mTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTheme != ThemeManager.getTheme(this)) {
            ThemeManager.restartActivity(this);
        }
    }

}
