package com.projectsexception.myapplist;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.projectsexception.myapplist.fragments.AppInfoFragment;
import com.projectsexception.myapplist.view.ThemeManager;

public class AppInfoActivity extends SherlockFragmentActivity implements AppInfoFragment.CallBack {
    
    public static final String NAME_EXTRA = "nameExtra";
    public static final String PACKAGE_EXTRA = "packageNameExtra";
    
    private int mTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTheme = ThemeManager.getTheme(this);
        setTheme(mTheme);
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            AppInfoFragment infoFragment = AppInfoFragment.newInstance(
                    getIntent().getStringExtra(NAME_EXTRA),
                    getIntent().getStringExtra(PACKAGE_EXTRA));
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, infoFragment).commit();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mTheme != ThemeManager.getTheme(this)) {
            ThemeManager.restartActivity(this);
        }
    }

    @Override
    public void removeAppInfoFragment() {
        finish();
    }

}
