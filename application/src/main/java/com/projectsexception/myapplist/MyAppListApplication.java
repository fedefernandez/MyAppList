package com.projectsexception.myapplist;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger;
import com.google.analytics.tracking.android.Tracker;
import com.projectsexception.util.CustomLog;

import static com.google.analytics.tracking.android.Logger.LogLevel;

public class MyAppListApplication extends Application {

    public static final String LOG_TAG = "MyAppList";
    public static final int LOG_LEVEL = BuildConfig.DEBUG ? Log.DEBUG : Log.INFO;

    private static GoogleAnalytics mGa;
    private static Tracker mTracker;

    /*
     * Google Analytics configuration values.
     */
    // Prevent hits from being sent to reports, i.e. during testing.
    private static final boolean GA_IS_DRY_RUN = BuildConfig.DEBUG;

    // GA Logger verbosity.
    private static final LogLevel GA_LOG_VERBOSITY = LogLevel.INFO;

    /*
     * Method to handle basic Google Analytics initialization. This call will not
     * block as all Google Analytics work occurs off the main thread.
     */
    private void initializeGa() {
        mGa = GoogleAnalytics.getInstance(this);
        mTracker = mGa.getTracker(BuildConfig.TRACKING_ID);

        // Set dryRun flag.
        // When dry run is set, hits will not be dispatched, but will still be logged as
        // though they were dispatched.
        mGa.setDryRun(GA_IS_DRY_RUN);

        // Set Logger verbosity.
        mGa.getLogger().setLogLevel(GA_LOG_VERBOSITY);

        // Set the opt out flag when user updates a tracking preference.
        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mGa.setAppOptOut(userPrefs.getBoolean(MyAppListPreferenceActivity.TRACKING_PREF_KEY, false));
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

    /*
     * Returns the Google Analytics tracker.
     */
    public static Tracker getGaTracker() {
        return mTracker;
    }

    /*
     * Returns the Google Analytics instance.
     */
    public static GoogleAnalytics getGaInstance() {
        return mGa;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CustomLog.initLog(LOG_TAG, LOG_LEVEL);
        initializeGa();
    }
}
