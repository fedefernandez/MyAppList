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

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;

import com.projectsexception.myapplist.R;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class creates pools of background threads for load application icons based on package names
 * The class is implemented as a singleton; the only way to get an IconManager instance is to
 * call {@link #getInstance}.
 * <p>
 * The class sets the pool size and cache size based on the particular operation it's performing.
 * The algorithm doesn't apply to all situations, so if you re-use the code to implement a pool
 * of threads for your own app, you will have to come up with your choices for pool size, cache
 * size, and so forth. In many cases, you'll have to set some numbers arbitrarily and then
 * measure the impact on performance.
 * <p>
 * Finally, this class defines a handler that communicates back to the UI
 * thread to change the drawable to reflect the state.
 */
@SuppressWarnings("unused")
public class IconManager {
    /*
     * Status indicators
     */
    static final int LOAD_FAILED = -1;
    static final int LOAD_STARTED = 1;
    static final int TASK_COMPLETE = 4;

    // Sets the size of the storage that's used to cache images
    private static final int IMAGE_CACHE_SIZE = 1024 * 1024 * 4;

    // Sets the amount of time an idle thread will wait for a task before terminating
    private static final int KEEP_ALIVE_TIME = 1;

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    // Sets the initial threadpool size to 8
    private static final int CORE_POOL_SIZE = 8;

    // Sets the maximum threadpool size to 8
    private static final int MAXIMUM_POOL_SIZE = 8;

    /*
     * Creates a cache of byte arrays indexed by image URLs. As new items are added to the
     * cache, the oldest items are ejected and subject to garbage collection.
     */
    private final LruCache<String, Drawable> mIconCache;

    /**
     * NOTE: This is the number of total available cores. On current versions of
     * Android, with devices that use plug-and-play cores, this will return less
     * than the total number of cores. The total number of cores is not
     * available in current Android implementations.
     */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    // A queue of Runnables for the image download pool
    private final BlockingQueue<Runnable> mDownloadWorkQueue;

    // A queue of IconManager tasks. Tasks are handed to a ThreadPool.
    private final Queue<IconTask> mIconTaskWorkQueue;

    // A managed pool of background download threads
    private final ThreadPoolExecutor mDownloadThreadPool;

    // An object that manages Messages in a Thread
    private Handler mHandler;

    // A single instance of IconManager, used to implement the singleton pattern
    private static final IconManager sInstance;

    // A static block that sets class fields
    static {

        // The time unit for "keep alive" is in seconds
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        // Creates a single static instance of IconManager
        sInstance = new IconManager();
    }
    /**
     * Constructs the work queues and thread pools used to download and decode images.
     */
    private IconManager() {

        /*
         * Creates a work queue for the pool of Thread objects used for downloading, using a linked
         * list queue that blocks when the queue is empty.
         */
        mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();

        /*
         * Creates a work queue for the set of of task objects that control downloading and
         * decoding, using a linked list queue that blocks when the queue is empty.
         */
        mIconTaskWorkQueue = new LinkedBlockingQueue<IconTask>();

        /*
         * Creates a new pool of Thread objects for the download work queue
         */
        mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);

        // Instantiates a new cache based on the cache size estimate
        mIconCache = new LruCache<String, Drawable>(IMAGE_CACHE_SIZE);

        /*
         * Instantiates a new anonymous Handler object and defines its
         * handleMessage() method. The Handler *must* run on the UI thread, because it moves
         * Drawables from the IconTask object to the View object.
         * To force the Handler to run on the UI thread, it's defined as part of the IconManager
         * constructor. The constructor is invoked when the class is first referenced, and that
         * happens when the View invokes startDownload. Since the View runs on the UI Thread, so
         * does the constructor and the Handler.
         */
        mHandler = new Handler(Looper.getMainLooper()) {

            /*
             * handleMessage() defines the operations to perform when the
             * Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {

                // Gets the image task from the incoming Message object.
                IconTask iconTask = (IconTask) inputMessage.obj;

                // Sets an IconView that's a weak reference to the
                // input ImageView
                IconView localView = iconTask.getIconView();

                // If this input view isn't null
                if (localView != null) {

                    /*
                     * Gets the package name of the *weak reference* to the input
                     * ImageView. The weak reference won't have changed, even if
                     * the input ImageView has.
                     */
                    String packageName = localView.getPackageName();

                    /*
                     * Compares the URL of the input ImageView to the URL of the
                     * weak reference. Only updates the drawable in the ImageView
                     * if this particular Thread is supposed to be serving the
                     * ImageView.
                     */
                    if (iconTask.getPackageName() != null && iconTask.getPackageName().equals(packageName)) {

                        /*
                         * Chooses the action to take, based on the incoming message
                         */
                        switch (inputMessage.what) {

                            // If the download has started, sets background color to dark green
                            case LOAD_STARTED:
                                localView.setStatusResource(R.drawable.ic_default_launcher);
                                break;
                            /*
                             * The decoding is done, so this sets the
                             * ImageView's bitmap to the bitmap in the
                             * incoming message
                             */
                            case TASK_COMPLETE:
                                localView.setImageDrawable(iconTask.getDrawable());
                                recycleTask(iconTask);
                                break;
                            // The download failed, sets the background color to dark red
                            case LOAD_FAILED:
                                localView.setStatusResource(R.drawable.ic_default_launcher);
                                
                                // Attempts to re-use the Task object
                                recycleTask(iconTask);
                                break;
                            default:
                                // Otherwise, calls the super method
                                super.handleMessage(inputMessage);
                        }
                    }
                }
            }
        };
    }

    /**
     * Returns the IconManager object
     * @return The global IconManager object
     */
    public static IconManager getInstance() {

        return sInstance;
    }
    
    /**
     * Handles state messages for a particular task object
     * @param iconTask A task object
     * @param state The state of the task
     */
    @SuppressLint("HandlerLeak")
    public void handleState(IconTask iconTask, int state) {
        switch (state) {
            
            // The task finished downloading and decoding the image
            case TASK_COMPLETE:

                // Puts the image into cache
                if (iconTask.isCacheEnabled() && iconTask.getPackageName() != null && iconTask.getDrawable() != null) {
                    // If the task is set to cache the results, put the buffer
                    // that was
                    // successfully decoded into the cache
                    mIconCache.put(iconTask.getPackageName(), iconTask.getDrawable());
                }

                // Gets a Message object, stores the state in it, and sends it to the Handler
                Message completeMessage = mHandler.obtainMessage(state, iconTask);
                completeMessage.sendToTarget();
                break;
            // In all other cases, pass along the message without any other action.
            default:
                mHandler.obtainMessage(state, iconTask).sendToTarget();
                break;
        }

    }

    /**
     * Cancels all Threads in the ThreadPool
     */
    public static void cancelAll() {

        /*
         * Creates an array of tasks that's the same size as the task work queue
         */
        IconTask[] taskArray = new IconTask[sInstance.mDownloadWorkQueue.size()];

        // Populates the array with the task objects in the queue
        sInstance.mDownloadWorkQueue.toArray(taskArray);

        // Stores the array length in order to iterate over the array
        int taskArraylen = taskArray.length;

        /*
         * Locks on the singleton to ensure that other processes aren't mutating Threads, then
         * iterates over the array of tasks and interrupts the task's current Thread.
         */
        synchronized (sInstance) {
            
            // Iterates over the array of tasks
            for (IconTask aTaskArray : taskArray) {

                // Gets the task's current thread
                Thread thread = aTaskArray.mThreadThis;

                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }

    /**
     * Stops a download Thread and removes it from the threadpool
     *
     * @param downloaderTask The download task associated with the Thread
     * @param packageName The URL being downloaded
     */
    static public void removeDownload(IconTask downloaderTask, String packageName) {

        // If the Thread object still exists and the download matches the specified URL
        if (downloaderTask != null && downloaderTask.getPackageName().equals(packageName)) {

            /*
             * Locks on this class to ensure that other processes aren't mutating Threads.
             */
            synchronized (sInstance) {
                
                // Gets the Thread that the downloader task is running on
                Thread thread = downloaderTask.getCurrentThread();

                // If the Thread exists, posts an interrupt to it
                if (null != thread)
                    thread.interrupt();
            }
            /*
             * Removes the download Runnable from the ThreadPool. This opens a Thread in the
             * ThreadPool's work queue, allowing a task in the queue to start.
             */
            sInstance.mDownloadThreadPool.remove(downloaderTask.getDownloadRunnable());
        }
    }

    /**
     * Starts an image download and decode
     *
     * @param imageView The ImageView that will get the resulting Bitmap
     * @param cacheEnabled indicates if cache is enabled
     * @return The task instance that will handle the work
     */
    static public IconTask startDownload(IconView imageView, boolean cacheEnabled) {

        /*
         * Gets a task from the pool of tasks, returning null if the pool is empty
         */
        IconTask loadTask = sInstance.mIconTaskWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == loadTask) {
            loadTask = new IconTask();
        }

        // Initializes the task
        loadTask.initializeDownloaderTask(IconManager.sInstance, imageView, cacheEnabled);

        loadTask.setDrawable(sInstance.mIconCache.get(loadTask.getPackageName()));

        if (loadTask.getDrawable() == null) {
            /*
             * "Executes" the tasks' download Runnable in order to download the image. If no
             * Threads are available in the thread pool, the Runnable waits in the queue.
             */
            sInstance.mDownloadThreadPool.execute(loadTask.getDownloadRunnable());

            // Sets the display to show that the image is queued for downloading and decoding.
            imageView.setStatusResource(R.drawable.ic_default_launcher);
        } else {
             /*
             * Signals that the download is "complete", because the byte array already contains the
             * undecoded image. The decoding starts.
             */

            sInstance.handleState(loadTask, TASK_COMPLETE);
        }

        // Returns a task object, either newly-created or one from the task pool
        return loadTask;
    }

    /**
     * Recycles tasks by calling their internal recycle() method and then putting them back into
     * the task queue.
     * @param downloadTask The task to recycle
     */
    void recycleTask(IconTask downloadTask) {
        
        // Frees up memory in the task
        downloadTask.recycle();
        
        // Puts the task object back into the queue for re-use.
        mIconTaskWorkQueue.offer(downloadTask);
    }
}
