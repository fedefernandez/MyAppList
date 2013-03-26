package com.projectsexception.myapplist.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.widget.AbsListView;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.model.MyAppListDbHelper;
import com.projectsexception.myapplist.view.ThemeManager;
import com.projectsexception.myapplist.work.AppListLoader;

public class IgnoredListFragment extends AbstractAppListFragment {
    
    public static interface CallBack {
        MyAppListDbHelper getHelper();
    }
    
    private CallBack mCallBack;
    private MenuItem mAcceptItem;
    private MenuItem mSelectAllItem;
    private boolean mCheckAll;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CallBack) {
            mCallBack = (CallBack) activity;
        } else {
            throw new IllegalStateException(activity.getClass().getName() + " must implement " + CallBack.class.getName());
        }
    }
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        setHasOptionsMenu(true);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ignoredlist, menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
        mAcceptItem = menu.findItem(R.id.menu_accept);
        mSelectAllItem = menu.findItem(R.id.menu_select_all);
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mAcceptItem.setIcon(ThemeManager.chooseDrawable(
                getSherlockActivity(), 
                R.drawable.ic_action_accept_dark, 
                R.drawable.ic_action_accept_light));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { 
        if (item.getItemId() == R.id.menu_refresh) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
            return true;
        } else if (item.getItemId() == R.id.menu_select_all) {
            checkAllItems();
            return true;
        } else if (item.getItemId() == R.id.menu_accept) {
            saveSelectedItems(getSelectedItems());
            getSherlockActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override 
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        loading(true);
        return new AppListLoader(getActivity());
    }
    
    @Override 
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> data) {
        super.onLoadFinished(loader, data);
        if (data != null) {
            // Check ignored items
            final List<String> ignored = mCallBack.getHelper().getPackages();
            final ListView list = getListView();
            final int count = getListAdapter().getCount();
            for ( int i = 0 ; i < count ; i++ ) {
                if (ignored.contains(data.get(i).getPackageName())) {
                    list.setItemChecked(i, true);
                }
            }
        }
    }
    
    private void checkAllItems() {
        mCheckAll = !mCheckAll;
        final ListView list = getListView();
        final int count = getListAdapter().getCount();
        for ( int i = 0 ; i < count ; i++ ) {
            list.setItemChecked(i, mCheckAll);
        }
        if (mCheckAll) {
        	mSelectAllItem.setTitle(R.string.menu_unselect_all);
        } else {
        	mSelectAllItem.setTitle(R.string.menu_select_all);
        }
    }
    
    private List<AppInfo> getSelectedItems() {
        List<AppInfo> selectedApps = new ArrayList<AppInfo>();
        SparseBooleanArray sp = getListView().getCheckedItemPositions();
        if (sp != null) {
            List<AppInfo> allApps = mAdapter.getData();
            int size = sp.size();
            int index;
            for (int i = 0 ; i < size ; i++) {
                index = sp.keyAt(i);
                if (index < allApps.size() && sp.valueAt(i)) {
                    selectedApps.add(allApps.get(index));
                }
            }                
        }
        return selectedApps;
    }
    
    private void saveSelectedItems(List<AppInfo> selectedItems) {
        if (selectedItems != null && !selectedItems.isEmpty()) {
            List<String> packages = new ArrayList<String>();
            for (AppInfo appInfo : selectedItems) {
                packages.add(appInfo.getPackageName());
            }
            mCallBack.getHelper().savePackages(packages);
        }
    }

    @Override
    public boolean isInfoButtonAvailable() {
        return false;
    }

}
