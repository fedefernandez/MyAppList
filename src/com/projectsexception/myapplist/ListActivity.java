package com.projectsexception.myapplist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.projectsexception.myapplist.fragments.AppListFragment;
import com.projectsexception.myapplist.fragments.FileListFragment;
import com.projectsexception.myapplist.R;

public class ListActivity extends SherlockFragmentActivity {

    public static final String ARG_FILE = "fileName";
    
    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);
        
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE|ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_HOME_AS_UP);
        
        String fileName = getIntent().getStringExtra(ARG_FILE);
        Fragment fragment;
        if (fileName == null) {
            // Want installed applications
            ab.setTitle(R.string.ab_title_app_list);
            fragment = new AppListFragment();
        } else {
            ab.setTitle(R.string.ab_title_file_list);
            Bundle fragmentArgs = new Bundle();
            fragmentArgs.putString(ARG_FILE, fileName);
            fragment = new FileListFragment();
            fragment.setArguments(fragmentArgs);
        }
        
        
        FragmentManager fm = getSupportFragmentManager();
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }

}
