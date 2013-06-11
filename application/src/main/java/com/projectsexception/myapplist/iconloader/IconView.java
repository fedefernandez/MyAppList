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

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * This class extends the standard Android ImageView View class with some features
 * that are useful for displaying icon images.
 *
 */
public class IconView extends ImageView {

    // Status flag that indicates if onDraw has completed
    private boolean mIsDrawn;

    // Indicates if caching should be used
    private boolean mCacheFlag;

    /*
     * Creates a weak reference to the ImageView in this object. The weak
     * reference prevents memory leaks and crashes, because it automatically tracks the "state" of
     * the variable it backs. If the reference becomes invalid, the weak reference is garbage-
     * collected.
     * This technique is important for referring to objects that are part of a component lifecycle.
     * Using a hard reference may cause memory leaks as the value continues to change; even worse,
     * it can cause crashes if the underlying component is destroyed. Using a weak reference to
     * a View ensures that the reference is more transitory in nature.
     */
    private WeakReference<View> mThisView;

    private PackageManager mPackageManager;

    // The package name of the application for this ImageView
    private String mPackageName;

    // The Thread that will be used to download the image for this ImageView
    private IconTask mDownloadThread;

    /**
     * Creates an ImageDownloadView with no settings
     * @param context A context for the View
     */
    public IconView(Context context) {
        super(context);
    }

    /**
     * Creates an ImageDownloadView and gets attribute values
     * @param context A Context to use with the View
     * @param attributeSet The entire set of attributes for the View
     */
    public IconView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /**
     * Creates an ImageDownloadView, gets attribute values, and applies a default style
     * @param context A context for the View
     * @param attributeSet The entire set of attributes for the View
     * @param defaultStyle The default style to use with the View
     */
    public IconView(Context context, AttributeSet attributeSet, int defaultStyle) {
        super(context, attributeSet, defaultStyle);
    }

    /**
     * Sets the visibility of the PhotoView
     * @param visState The visibility state (see View.setVisibility)
     */
    private void showView(int visState) {
        // If the View contains something
        if (mThisView != null) {
            
            // Gets a local hard reference to the View
            View localView = mThisView.get();
            
            // If the weak reference actually contains something, set the visibility
            if (localView != null)
                localView.setVisibility(visState);
        }
    }
    
    /**
     * Sets the image in this ImageView to null, and makes the View visible
     */
    public void clearImage() {
        setImageDrawable(null);
        showView(View.VISIBLE);
    }

    /**
     * Returns the package name of the icon associated with this ImageView
     * @return a URL
     */
    final String getPackageName() {
        return mPackageName;
    }

    final PackageManager getPackageManager() {
        return mPackageManager;
    }

    /*
     * This callback is invoked when the system attaches the ImageView to a Window. The callback
     * is invoked before onDraw(), but may be invoked after onMeasure()
     */
    @Override
    protected void onAttachedToWindow() {
        // Always call the supermethod first
        super.onAttachedToWindow();
    }

    /*
     * This callback is invoked when the ImageView is removed from a Window. It "unsets" variables
     * to prevent memory leaks.
     */
    @Override
    protected void onDetachedFromWindow() {
        
        // Clears out the image drawable, turns off the cache, disconnects the view from a URL
        setPackageName(null, null, 0, false);
        
        // Gets the current Drawable, or null if no Drawable is attached
        Drawable localDrawable = getDrawable();
        
        // if the Drawable is null, unbind it from this VIew
        if (localDrawable != null)
            localDrawable.setCallback(null);
        
        // If this View still exists, clears the weak reference, then sets the reference to null
        if (mThisView != null) {
            mThisView.clear();
            mThisView = null;
        }
        
        // Sets the downloader thread to null
        this.mDownloadThread = null;
        
        // Always call the super method last
        super.onDetachedFromWindow();
    }

    /*
     * This callback is invoked when the system tells the View to draw itself. If the View isn't
     * already drawn, and its URL isn't null, it invokes a Thread to download the image. Otherwise,
     * it simply passes the existing Canvas to the super method
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // If the image isn't already drawn, and the URL is set
        if ((!mIsDrawn) && (mPackageName != null)) {
            
            // Starts downloading this View
            mDownloadThread = IconManager.startDownload(this, mCacheFlag);
            
            // After successfully downloading the image, this marks that it's available.
            mIsDrawn = true;
        }
        // Always call the super method last
        super.onDraw(canvas);
    }

    /**
     * Sets the current View weak reference to be the incoming View. See the definition of
     * mThisView
     * @param view the View to use as the new WeakReference
     */
    public void setHideView(View view) {
        this.mThisView = new WeakReference<View>(view);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        // The visibility of the View
        int viewState;
        
        /*
         * Sets the View state to visible if the method is called with a null argument (the
         * image is being cleared). Otherwise, sets the View state to invisible before refreshing
         * it.
         */
        if (drawable == null) {
            
            viewState = View.VISIBLE;
        } else {
            
            viewState = View.INVISIBLE;
        }
        // Either hides or shows the View, depending on the view state
        showView(viewState);

        // Invokes the supermethod with the provided drawable
        super.setImageDrawable(drawable);
    }

    /**
     * Attempts to set the picture URL for this ImageView and then download the picture.
     * <p>
     * If the picture URL for this view is already set, and the input URL is not the same as the
     * stored URL, then the picture has moved and any existing downloads are stopped.
     * <p>
     * If the input URL is the same as the stored URL, then nothing needs to be done.
     * <p>
     * If the stored URL is null, then this method starts a download and decode of the picture
     * @param packageName Package name
     * @param resourceDrawable The resource drawable to use for this ImageView
     */
    public void setPackageName(PackageManager packageManager, String packageName, int resourceDrawable, boolean cacheFlag) {
        // If the picture URL for this ImageView is already set
        if (mPackageName != null) {
            
            // If the stored URL doesn't match the incoming URL, then the picture has changed.
            if (!mPackageName.equals(packageName)) {
                
                // Stops any ongoing downloads for this ImageView
                IconManager.removeDownload(mDownloadThread, mPackageName);
            } else {
                
                // The stored URL matches the incoming URL. Returns without doing any work.
                return;
            }
        }
        
        // Sets the Drawable for this ImageView
        setImageResource(resourceDrawable);

        mPackageManager = packageManager;
        
        // Stores the picture URL for this ImageView
        mPackageName = packageName;
        
        // If the draw operation for this ImageVIew has completed, and the picture URL isn't empty
        if ((mIsDrawn) && (packageName != null)) {

            // Sets the cache flag
            mCacheFlag = cacheFlag;
            
            /*
             * Starts a download of the picture file. Notice that if caching is on, the picture
             * file's contents may be taken from the cache.
             */
            mDownloadThread = IconManager.startDownload(this, mCacheFlag);
        }
    }

    /**
     * Sets the content of this ImageView to be a Drawable resource
     * @param resId drawable resource
     */
    public void setStatusResource(int resId) {
        
        // If the View is empty, provides it with a Drawable resource as its content
        if (mThisView == null) {
            setImageResource(resId);
        }
    }
}
