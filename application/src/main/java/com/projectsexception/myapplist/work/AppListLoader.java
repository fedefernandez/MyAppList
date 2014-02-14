package com.projectsexception.myapplist.work;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.preference.PreferenceManager;

import com.projectsexception.myapplist.MyAppListPreferenceActivity;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.xml.FileUtil;
import com.projectsexception.util.CustomLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AppListLoader extends AbstractListLoader {
    
    private final PackageManager mPm;
    private final boolean mHideSystemApps;

    public AppListLoader(Context context) {
        super(context);
        
        // Retrieve the package manager for later use; note we don't
        // use 'context' directly but instead the save global application
        // context returned by getContext().
        mPm = getContext().getPackageManager();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mHideSystemApps = prefs.getBoolean(MyAppListPreferenceActivity.KEY_HIDE_SYSTEM_APPS, true);
    }

    @Override
    public ArrayList<AppInfo> loadAppInfoList() {
        return AppUtil.loadAppInfoList(mPm, mHideSystemApps);
    }

    @Override
    public boolean isPackageIntentReceiver() {
        return true;
    }
}