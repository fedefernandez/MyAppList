package com.projectsexception.myapplist;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.projectsexception.myapplist.view.ThemeManager;

public class BaseActivity extends ActionBarActivity {
    
    private int mTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTheme = ThemeManager.getTheme(this);
        setTheme(mTheme);
        super.onCreate(savedInstanceState);
        // Fields set on a tracker persist for all hits, until they are
        // overridden or cleared by assignment to null.
        MyAppListApplication.getGaTracker().set(Fields.SCREEN_NAME, ((Object) this).getClass().getSimpleName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyAppListApplication.getGaTracker().send(MapBuilder.createAppView().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTheme != ThemeManager.getTheme(this)) {
            ThemeManager.restartActivity(this);
        }
    }

}
