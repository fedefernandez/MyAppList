package com.projectsexception.myapplist;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.projectsexception.myapplist.fragments.AppInfoFragment;

public class AppInfoActivity extends SherlockFragmentActivity implements AppInfoFragment.ActivityInterface {
    
    public static final String NAME_EXTRA = "nameExtra";
    public static final String PACKAGE_EXTRA = "packageNameExtra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    public void removeAppInfoFragment() {
        finish();
    }

}
