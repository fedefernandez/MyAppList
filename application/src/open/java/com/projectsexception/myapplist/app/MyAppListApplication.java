package com.projectsexception.myapplist.app;

import android.app.Application;
import android.util.Log;

import com.projectsexception.myapplist.BuildConfig;
import com.projectsexception.util.CustomLog;

public class MyAppListApplication extends Application {

    public static final String LOG_TAG = "MyAppList";
    public static final int LOG_LEVEL = BuildConfig.DEBUG ? Log.DEBUG : Log.INFO;

    public void analyticsSetScreenName(String screenName) {

    }

    public void analyticsSend() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        CustomLog.initLog(LOG_TAG, LOG_LEVEL);
    }
}
