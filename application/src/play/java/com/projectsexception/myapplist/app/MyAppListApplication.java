package com.projectsexception.myapplist.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.projectsexception.myapplist.BuildConfig;
import com.projectsexception.myapplist.MyAppListPreferenceActivity;
import com.projectsexception.util.CustomLog;

public class MyAppListApplication extends Application {

    public static final String LOG_TAG = "MyAppList";
    public static final int LOG_LEVEL = BuildConfig.DEBUG ? Log.DEBUG : Log.INFO;

    /*
     * Google Analytics configuration values.
     */
    // Prevent hits from being sent to reports, i.e. during testing.
    private static final boolean GA_IS_DRY_RUN = BuildConfig.DEBUG;

    // GA Logger verbosity.
    private static final int GA_LOG_VERBOSITY = Logger.LogLevel.INFO;

    private Tracker mTracker;

    /*
     * Method to handle basic Google Analytics initialization. This call will not
     * block as all Google Analytics work occurs off the main thread.
     */
    private void initializeGa() {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(this);
        mTracker = ga.newTracker(BuildConfig.TRACKING_ID);

        // Set dryRun flag.
        // When dry run is set, hits will not be dispatched, but will still be logged as
        // though they were dispatched.
        ga.setDryRun(GA_IS_DRY_RUN);

        // Set Logger verbosity.
        ga.getLogger().setLogLevel(GA_LOG_VERBOSITY);

        // Set the opt out flag when user updates a tracking preference.
        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        ga.setAppOptOut(userPrefs.getBoolean(MyAppListPreferenceActivity.TRACKING_PREF_KEY, false));
        userPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener () {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                  String key) {
                if (key.equals(MyAppListPreferenceActivity.TRACKING_PREF_KEY)) {
                    GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(sharedPreferences.getBoolean(key, false));
                }
            }
        });
    }

    public void analyticsSetScreenName(String screenName) {
        mTracker.setScreenName(((Object) this).getClass().getSimpleName());
    }

    public void analyticsSend() {
        mTracker.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CustomLog.initLog(LOG_TAG, LOG_LEVEL);
        initializeGa();
    }
}
