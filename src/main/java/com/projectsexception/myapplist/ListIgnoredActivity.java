package com.projectsexception.myapplist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.fragments.IgnoredListFragment;
import com.projectsexception.myapplist.model.MyAppListDbHelper;

public class ListIgnoredActivity extends BaseActivity implements IgnoredListFragment.CallBack {
    
    private MyAppListDbHelper mDbHelper;
    
    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);
        
        final ActionBar ab = getSupportActionBar();        
        FragmentManager fm = getSupportFragmentManager();
        ab.setTitle(R.string.ab_title_ignored_list);
        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, new IgnoredListFragment()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // This is called when the Home (Up) button is pressed
            // in the Action Bar.
            Intent parentActivityIntent = new Intent(this, MainActivity.class);
            parentActivityIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
