package com.projectsexception.myapplist.fragments;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.AppInfoActivity;
import com.projectsexception.myapplist.MainActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
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
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        ApplicationsReceiver.unregisterListener(getSherlockActivity());
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consume = false;
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            consume = true;
        } else if (item.getItemId() == R.id.menu_share_text 
                || item.getItemId() == R.id.menu_share_html) {
            SparseBooleanArray sp = getListView().getCheckedItemPositions();
            if (sp == null || sp.size() == 0) {
                Toast.makeText(getActivity(), R.string.empty_list_error, Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);            
                List<AppInfo> lst = mAdapter.getData();
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_text_subject));
                if (item.getItemId() == R.id.menu_share_text) {
                    intent.setType("text/plain");                    
                    intent.putExtra(Intent.EXTRA_TEXT, AppUtil.appInfoToSpanned(getActivity(), lst, false));
                } else {
                    intent.setType("text/html");
                    intent.putExtra(Intent.EXTRA_TEXT, AppUtil.appInfoToSpanned(getActivity(), lst, true));
                }
                try {
                    startActivity(Intent.createChooser(intent, getString(R.string.share_chooser)));                
                } catch (Exception e) {
                    // Something was wrong
                }
            }
            consume = true;
        }
        return consume;
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
