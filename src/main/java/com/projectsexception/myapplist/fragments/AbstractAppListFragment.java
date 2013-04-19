package com.projectsexception.myapplist.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.AppInfoActivity;
import com.projectsexception.myapplist.ListActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.ApplicationsReceiver;
import com.projectsexception.myapplist.view.AppListAdapter;

import java.util.ArrayList;

public abstract class AbstractAppListFragment extends SherlockListFragment implements
        LoaderManager.LoaderCallbacks<ArrayList<AppInfo>>,
        AdapterView.OnItemClickListener,
        AppListAdapter.ActionListener {

    private static final String KEY_LISTENER = "AbstractAppListFragment";
    private static final String CUR_NAME = "curName";
    private static final String CUR_PACKAGE = "curPackage";

    protected static final String ARG_RELOAD = "reload";
    
    protected MenuItem mRefreshItem;
    protected AppListAdapter mAdapter;
    
    private boolean mDualPane;
    private String mCurrentName;
    private String mCurrentPackage;

    abstract int getMenuAdapter();
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getSherlockActivity().getString(R.string.fragment_list_empty));

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new AppListAdapter(getSherlockActivity(), savedInstanceState, getMenuAdapter());
        mAdapter.setOnItemClickListener(this);
        mAdapter.setAdapterView(getListView());
        mAdapter.setListener(this);

        // Start out with a progress indicator.
        setListShown(false);

        getListView().setFastScrollEnabled(true);
        
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
        outState.putString(CUR_NAME, mCurrentName);
        outState.putString(CUR_PACKAGE, mCurrentPackage);
        mAdapter.save(outState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (ApplicationsReceiver.getInstance(getSherlockActivity()).isContextChanged(KEY_LISTENER)) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfo appInfo = mAdapter.getData().get(position);
        if (!TextUtils.isEmpty(appInfo.getPackageName())) {
            if (appInfo.isInstalled()) {
                showAppInfo(appInfo.getName(), appInfo.getPackageName());
            } else {
                AppUtil.showPlayGoogleApp(getActivity(), appInfo.getPackageName());
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        ApplicationsReceiver.unregisterListener(getSherlockActivity());
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getActivity(), ListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }  else if (item.getItemId() == R.id.menu_select_all) {
            for (int i = 0; i < mAdapter.getCount(); ++i) {
                mAdapter.select(i);
            }
            return true;
        } else if (item.getItemId() == R.id.menu_refresh) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
            return true;
        }
        return false;
    }

    @Override 
    public abstract Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args);

    @Override 
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> data) {
        loading(false);
        
        ApplicationsReceiver.getInstance(getSherlockActivity()).registerListener(KEY_LISTENER);
        
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
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
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
