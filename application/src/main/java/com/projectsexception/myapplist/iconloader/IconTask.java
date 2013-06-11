/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projectsexception.myapplist.iconloader;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.lang.ref.WeakReference;

/**
 * This class manages PhotoDownloadRunnable and PhotoDownloadRunnable objects.  It does't perform
 * the download or decode; instead, it manages persistent storage for the tasks that do the work.
 * It does this by implementing the interfaces that the download and decode classes define, and
 * then passing itself as an argument to the constructor of a download or decode object. In effect,
 * this allows PhotoTask to start on a Thread, run a download in a delegate object, then
 * run a decode, and then start over again. This class can be pooled and reused as necessary.
 */
public class IconTask implements IconDownloadRunnable.TaskRunnableDownloadMethods {

    /*
     * Creates a weak reference to the ImageView that this Task will populate.
     * The weak reference prevents memory leaks and crashes, because it
     * automatically tracks the "state" of the variable it backs. If the
     * reference becomes invalid, the weak reference is garbage- collected. This
     * technique is important for referring to objects that are part of a
     * component lifecycle. Using a hard reference may cause memory leaks as the
     * value continues to change; even worse, it can cause crashes if the
     * underlying component is destroyed. Using a weak reference to a View
     * ensures that the reference is more transitory in nature.
     */
    private WeakReference<IconView> mImageWeakRef;

    // The package manager
    private PackageManager mPackageManager;

    // The package name
    private String mPackageName;

    // Is the cache enabled for this transaction?
    private boolean mCacheEnabled;

    /*
     * Field containing the Thread this task is running on.
     */
    Thread mThreadThis;

    /*
     * Fields containing references to the two runnable objects that handle downloading and
     * decoding of the image.
     */
    private Runnable mDownloadRunnable;

    // The loaded drawable
    private Drawable mDrawable;

    // The Thread on which this task is currently running.
    private Thread mCurrentThread;

    /*
     * An object that contains the ThreadPool singleton.
     */
    private static IconManager sIconManager;

    /**
     * Creates an PhotoTask containing a download object and a decoder object.
     */
    IconTask() {
        // Create the runnables
        mDownloadRunnable = new IconDownloadRunnable(this);
        sIconManager = IconManager.getInstance();
    }
    
    /**
     * Initializes the Task
     *
     * @param iconManager A ThreadPool object
     * @param imageView An ImageView instance that shows the downloaded image
     */
    void initializeDownloaderTask(IconManager iconManager, IconView imageView, boolean cacheEnabled) {
        // Sets this object's ThreadPool field to be the input argument
        sIconManager = iconManager;

        mPackageManager = imageView.getPackageManager();
        
        // Gets the URL for the View
        mPackageName = imageView.getPackageName();

        mCacheEnabled = cacheEnabled;

        // Instantiates the weak reference to the incoming view
        mImageWeakRef = new WeakReference<IconView>(imageView);
    }
    
    /**
     * Recycles an PhotoTask object before it's put back into the pool. One reason to do
     * this is to avoid memory leaks.
     */
    void recycle() {
        
        // Deletes the weak reference to the imageView
        if ( null != mImageWeakRef ) {
            mImageWeakRef.clear();
            mImageWeakRef = null;
        }
    }

    // Detects the state of caching
    boolean isCacheEnabled() {
        return mCacheEnabled;
    }
    
    // Delegates handling the current state of the task to the PhotoManager object
    void handleState(int state) {
        sIconManager.handleState(this, state);
    }

    // Returns the ImageView that's being constructed.
    public IconView getIconView() {
        if ( null != mImageWeakRef ) {
            return mImageWeakRef.get();
        }
        return null;
    }

    /*
     * Returns the Thread that this Task is running on. The method must first get a lock on a
     * static field, in this case the ThreadPool singleton. The lock is needed because the
     * Thread object reference is stored in the Thread object itself, and that object can be
     * changed by processes outside of this app.
     */
    public Thread getCurrentThread() {
        synchronized(sIconManager) {
            return mCurrentThread;
        }
    }

    /*
     * Sets the identifier for the current Thread. This must be a synchronized operation; see the
     * notes for getCurrentThread()
     */
    public void setCurrentThread(Thread thread) {
        synchronized(sIconManager) {
            mCurrentThread = thread;
        }
    }

    // Implements PhotoDownloadRunnable.setHTTPDownloadThread(). Calls setCurrentThread().
    @Override
    public void setDownloadThread(Thread currentThread) {
        setCurrentThread(currentThread);
    }

    // Returns the instance that downloaded the image
    Runnable getDownloadRunnable() {
        return mDownloadRunnable;
    }

    @Override
    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    /*
     * Implements PhotoDownloadRunnable.handleHTTPState(). Passes the download state to the
     * ThreadPool object.
     */
    
    @Override
    public void handleDownloadState(int state) {
        int outState;
        
        // Converts the download state to the overall state
        switch(state) {
            case IconDownloadRunnable.STATE_COMPLETED:
                outState = IconManager.TASK_COMPLETE;
                break;
            case IconDownloadRunnable.STATE_FAILED:
                outState = IconManager.LOAD_FAILED;
                break;
            default:
                outState = IconManager.LOAD_STARTED;
                break;
        }
        // Passes the state to the ThreadPool object.
        handleState(outState);
    }

    @Override
    public PackageManager getPackageManager() {
        return mPackageManager;
    }

    @Override
    public String getPackageName() {
        return mPackageName;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }
}
