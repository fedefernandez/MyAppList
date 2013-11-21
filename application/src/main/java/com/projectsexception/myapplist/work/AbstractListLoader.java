package com.projectsexception.myapplist.work;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;

import com.projectsexception.myapplist.model.AppInfo;

public abstract class AbstractListLoader extends AsyncTaskLoader<ArrayList<AppInfo>> {

    private static final Object KEY = new Object();
    
    private ArrayList<AppInfo> mApps;
    private PackageIntentReceiver mPackageObserver;

    public AbstractListLoader(Context context) {
        super(context);
    }
    
    public abstract ArrayList<AppInfo> loadAppInfoList();
    public abstract boolean isPackageIntentReceiver();

    /**
     * This is where the bulk of our work is done. This function is called in a
     * background thread and should generate a new set of data to be published
     * by the loader.
     */
    @Override
    public ArrayList<AppInfo> loadInBackground() {
        // Retrieve all known applications.
        ArrayList<AppInfo> entries;
        synchronized (KEY) {
            entries = loadAppInfoList();
        }
        
        if (entries != null) {
            // Sort the list.
            Collections.sort(entries);
        }

        // Done!
        return entries;
    }

    /**
     * Called when there is new data to deliver to the client. The super class
     * will take care of delivering it; the implementation here just adds a
     * little more logic.
     */
    @Override
    public void deliverResult(ArrayList<AppInfo> apps) {
        mApps = apps;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(apps);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mApps != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mApps);
        }

        // Start watching for changes in the app data.
        if (isPackageIntentReceiver() && mPackageObserver == null) {
            mPackageObserver = new PackageIntentReceiver(this);
        }

        if (takeContentChanged() || mApps == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(ArrayList<AppInfo> apps) {
        super.onCanceled(apps);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mApps != null) {
            mApps = null;
        }

        // Stop monitoring for changes.
        if (mPackageObserver != null) {
            getContext().unregisterReceiver(mPackageObserver);
            mPackageObserver = null;
        }
    }
    
    /**
     * Helper class to look for interesting changes to the installed apps
     * so that the loader can be updated.
     */
    public static class PackageIntentReceiver extends BroadcastReceiver {
        final AbstractListLoader mLoader;

        public PackageIntentReceiver(AbstractListLoader loader) {
            mLoader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mLoader.getContext().registerReceiver(this, filter);
            // Register for events related to sdcard installation.
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mLoader.getContext().registerReceiver(this, sdFilter);
        }

        @Override 
        public void onReceive(Context context, Intent intent) {
            // Tell the loader about the change.
            mLoader.onContentChanged();            
        }
    }
    
}