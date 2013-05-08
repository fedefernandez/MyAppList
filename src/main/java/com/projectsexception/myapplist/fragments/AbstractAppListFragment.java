package com.projectsexception.myapplist.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.MainActivity;
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

    protected static final String ARG_RELOAD = "reload";
    
    protected MenuItem mRefreshItem;
    protected AppListAdapter mAdapter;

    abstract int getMenuAdapter();
    abstract void showAppInfo(String name, String packageName);
    
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            mAdapter.save(outState);
        }
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
                AppUtil.showPlayGoogleApp(getActivity(), appInfo.getPackageName(), false);
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
        if (item.getItemId() == R.id.menu_select_all) {
            for (int i = 0; i < mAdapter.getCount(); ++i) {
                mAdapter.setItemChecked(i, true);
            }
            return true;
        } else if (item.getItemId() == R.id.menu_refresh) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
