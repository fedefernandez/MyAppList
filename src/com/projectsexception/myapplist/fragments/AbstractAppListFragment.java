package com.projectsexception.myapplist.fragments;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.AppInfoActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.ApplicationsReceiver;
import com.projectsexception.myapplist.view.AppListAdapter;

public abstract class AbstractAppListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<AppInfo>>, View.OnClickListener {

    private static final String CUR_NAME = "curName";
    private static final String CUR_PACKAGE = "curPackage";

    protected static final String ARG_RELOAD = "reload";
    
    protected MenuItem mRefreshItem;
    protected AppListAdapter mAdapter;
    
    private boolean mDualPane;
    private String mCurrentName;
    private String mCurrentPackage;
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getSherlockActivity().getString(R.string.fragment_list_empty));

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new AppListAdapter(getSherlockActivity());
        if (isInfoButtonAvailable()) {
            mAdapter.setListener(this);
        }
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);
        
        registerForContextMenu(getListView());
        
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurrentName = savedInstanceState.getString(CUR_NAME);
            mCurrentPackage = savedInstanceState.getString(CUR_PACKAGE);
        }
        
        View detailsFrame = getSherlockActivity().findViewById(R.id.app_info);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        
        if (mDualPane) {
            // Make sure our UI is in the correct state.
            showAppInfo(mCurrentName, mCurrentPackage);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CUR_PACKAGE, mCurrentPackage);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (ApplicationsReceiver.getInstance(getSherlockActivity()).isContextChanged(getClass().getName())) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
        }
    }
    
    @Override
    public void onClick(View v) {
        AppInfo appInfo = (AppInfo) v.getTag();
        showAppInfo(appInfo.getName(), appInfo.getPackageName());
    }
    
    public abstract boolean isInfoButtonAvailable();

    @Override 
    public abstract Loader<List<AppInfo>> onCreateLoader(int id, Bundle args);

    @Override 
    public void onLoadFinished(Loader<List<AppInfo>> loader, List<AppInfo> data) {
        loading(false);
        
        ApplicationsReceiver.getInstance(getSherlockActivity()).registerListener(getClass().getName());
        
        // Set the new data in the adapter.
        mAdapter.setData(data);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override 
    public void onLoaderReset(Loader<List<AppInfo>> loader) {
        loading(false);
        // Clear the data in the adapter.
        mAdapter.setData(null);
    }
    
    protected void showAppInfo(String name, String packageName) {
        mCurrentPackage = packageName;
        if (mDualPane) {

            // Check what fragment is shown, replace if needed.
            AppInfoFragment infoFragment = (AppInfoFragment) 
                    getFragmentManager().findFragmentById(R.id.app_info);
            if (infoFragment == null 
                    || infoFragment.getShownPackage() == null 
                    || !infoFragment.getShownPackage().equals(packageName)) {
                // Make new fragment to show this selection.
                infoFragment = AppInfoFragment.newInstance(name, packageName);

                // Execute a transaction, replacing any existing
                // fragment with this one inside the frame.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.app_info, infoFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }

        } else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.
            Intent intent = new Intent();
            intent.setClass(getActivity(), AppInfoActivity.class);
            intent.putExtra(AppInfoActivity.NAME_EXTRA, name);
            intent.putExtra(AppInfoActivity.PACKAGE_EXTRA, packageName);
            startActivity(intent);
        }
    }
    
    protected void loading(boolean loading) {
        if (mRefreshItem != null) {
            if(loading) {
                mRefreshItem.setEnabled(false);
                mRefreshItem.setActionView(R.layout.refresh_loading);
            } else {
                mRefreshItem.setEnabled(true);
                mRefreshItem.setActionView(null);
            }           
        }
    }

}
