package com.projectsexception.myapplist;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.projectsexception.myapplist.fragments.IgnoredListFragment;
import com.projectsexception.myapplist.model.MyAppListDbHelper;
import com.projectsexception.myapplist.view.ThemeManager;

public class ListIgnoredActivity extends SherlockFragmentActivity implements IgnoredListFragment.CallBack {

    public static final String ARG_FILE = "fileName";
    
    private int mTheme;
    private MyAppListDbHelper mDbHelper;
    
    @Override
    protected void onCreate(Bundle args) {
        mTheme = ThemeManager.getTheme(this);
        setTheme(mTheme);
        super.onCreate(args);
        
        final ActionBar ab = getSupportActionBar();        
        FragmentManager fm = getSupportFragmentManager();
        ab.setTitle(R.string.ab_title_ignored_list);
        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, new IgnoredListFragment()).commit();
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
    protected void onDestroy() {
        super.onDestroy();
        if (mDbHelper != null) {            
            mDbHelper.close();
            mDbHelper = null;
        }
    }
    
    @Override
    public MyAppListDbHelper getHelper() {
        if (mDbHelper == null) {
            mDbHelper = new MyAppListDbHelper(this);
        }
        return mDbHelper;
    }

}
