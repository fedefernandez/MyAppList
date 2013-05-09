package com.projectsexception.myapplist.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.model.MyAppListDbHelper;
import com.projectsexception.myapplist.work.AppListLoader;

import java.util.ArrayList;
import java.util.List;

public class IgnoredListFragment extends AbstractAppListFragment {

    public static interface CallBack {
        MyAppListDbHelper getHelper();
    }
    
    private CallBack mCallBack;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CallBack) {
            mCallBack = (CallBack) activity;
        } else {
            throw new IllegalStateException("activity must implement fragment's callback");
        }
    }

    @Override
    int getMenuAdapter() {
        return R.menu.adapter_ign;
    }

    @Override
    void showAppInfo(String name, String packageName) {
        //
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.setItemChecked(position, true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_ign, menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            saveSelectedItems(mAdapter.getSelectedItems());
            getSherlockActivity().finish(); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void actionItemClicked(int id) {
        if (id == R.id.menu_save_ign) {
            saveSelectedItems(mAdapter.getSelectedItems());
            getSherlockActivity().finish();
        }
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
            final int count = mAdapter.getCount();
            for ( int i = 0 ; i < count ; i++ ) {
                if (ignored.contains(data.get(i).getPackageName())) {
                    mAdapter.setItemChecked(i, true);
                }
            }
        }
    }

    @Override
    boolean showCheckBox() {
        return false;
    }

    private void saveSelectedItems(List<AppInfo> selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            mCallBack.getHelper().deletePackages();
        } else {
            List<String> packages = new ArrayList<String>();
            for (AppInfo appInfo : selectedItems) {
                packages.add(appInfo.getPackageName());
            }
            mCallBack.getHelper().savePackages(packages);
        }
    }

}
