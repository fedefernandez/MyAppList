package com.projectsexception.myapplist;

import android.app.Application;
import android.util.Log;

import com.projectsexception.util.CustomLog;

public class MyAppListApplication extends Application {

    public static final String LOG_TAG = "MyAppList";
    public static final int LOG_LEVEL = BuildConfig.DEBUG ? Log.DEBUG : Log.INFO;

    @Override
    public void onCreate() {
        super.onCreate();
        CustomLog.initLog(LOG_TAG, LOG_LEVEL);
    }
}
