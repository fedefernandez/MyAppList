/*
 * Copyright (C) ${year} The Android Open Source Project
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

import com.projectsexception.myapplist.util.AppUtil;

/**
 * This task load the icon from package manager.  When the task
 * has finished, it calls handleState to report its results.
 *
 * Objects of this class are instantiated and managed by instances of IconTask.
 * PhotoTask objects call
 * {@link #IconDownloadRunnable(TaskRunnableDownloadMethods) PhotoDownloadRunnable()} with
 * themselves as the argument. In effect, an PhotoTask object and a
 * PhotoDownloadRunnable object communicate through the fields of the PhotoTask.
 */
class IconDownloadRunnable implements Runnable {

    // Sets a tag for this class
    @SuppressWarnings("unused")
    private static final String LOG_TAG = "PhotoDownloadRunnable";

    // Constants for indicating the state of the download
    static final int STATE_FAILED = -1;
    static final int STATE_STARTED = 0;
    static final int STATE_COMPLETED = 1;

    // Defines a field that contains the calling object of type PhotoTask.
    final TaskRunnableDownloadMethods mPhotoTask;

    /**
     *
     * An interface that defines methods that PhotoTask implements. An instance of
     * PhotoTask passes itself to an IconDownloadRunnable instance through the
     * PhotoDownloadRunnable constructor, after which the two instances can access each other's
     * variables.
     */
    interface TaskRunnableDownloadMethods {

        /**
         * Sets the Thread that this instance is running on
         * @param currentThread the current Thread
         */
        void setDownloadThread(Thread currentThread);

        /**
         * Sets the drawable
         * @param drawable to set
         */
        void setDrawable(Drawable drawable);

        /**
         * Defines the actions for each state of the PhotoTask instance.
         * @param state The current state of the task
         */
        void handleDownloadState(int state);

        PackageManager getPackageManager();

        /**
         * Gets the package name for the applicacion
         * @return The package name
         */
        String getPackageName();
    }

    /**
     * This constructor creates an instance of PhotoDownloadRunnable and stores in it a reference
     * to the PhotoTask instance that instantiated it.
     *
     * @param photoTask The PhotoTask, which implements TaskRunnableDecodeMethods
     */
    IconDownloadRunnable(TaskRunnableDownloadMethods photoTask) {
        mPhotoTask = photoTask;
    }
    
    /*
     * Defines this object's task, which is a set of instructions designed to be run on a Thread.
     */
    @SuppressWarnings("resource")
    @Override
    public void run() {

        /*
         * Stores the current Thread in the the PhotoTask instance, so that the instance
         * can interrupt the Thread.
         */
        mPhotoTask.setDownloadThread(Thread.currentThread());
        
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        Drawable drawable = null;
        /*
         * A try block that load the icon. The package name value is in the field
         * PhotoTask.mPackageName
         */
        try {
            // Before continuing, checks to see that the Thread hasn't been
            // interrupted
            if (Thread.interrupted()) {
                
                throw new InterruptedException();
            }


            /*
             * Calls the PhotoTask implementation of {@link #handleDownloadState} to
             * set the state of the download
             */
            mPhotoTask.handleDownloadState(STATE_STARTED);

            drawable = AppUtil.loadApplicationIcon(mPhotoTask.getPackageManager(), mPhotoTask.getPackageName());

            /*
             * Stores the downloaded bytes in the byte buffer in the PhotoTask instance.
             */
            mPhotoTask.setDrawable(drawable);

            /*
             * Sets the status message in the PhotoTask instance. This sets the
             * ImageView background to indicate that the image is being
             * decoded.
             */
            mPhotoTask.handleDownloadState(STATE_COMPLETED);
      
        // Catches exceptions thrown in response to a queued interrupt
        } catch (InterruptedException e1) {
            
            // Does nothing
        
        // In all cases, handle the results
        } finally {
            
            // If the byteBuffer is null, reports that the download failed.
            if (drawable == null) {
                mPhotoTask.handleDownloadState(STATE_FAILED);
            }

            /*
             * The implementation of setHTTPDownloadThread() in PhotoTask calls
             * PhotoTask.setCurrentThread(), which then locks on the static ThreadPool
             * object and returns the current thread. Locking keeps all references to Thread
             * objects the same until the reference to the current Thread is deleted.
             */
            
            // Sets the reference to the current Thread to null, releasing its storage
            mPhotoTask.setDownloadThread(null);
            
            // Clears the Thread's interrupt flag
            Thread.interrupted();
        }
    }
}

