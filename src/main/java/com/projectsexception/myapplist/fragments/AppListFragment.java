package com.projectsexception.myapplist.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.ShareActivity;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.NewFileDialog;
import com.projectsexception.myapplist.work.AppListLoader;
import com.projectsexception.myapplist.work.AppSaveTask;

import java.util.ArrayList;
import java.util.List;

public class AppListFragment extends AbstractAppListFragment {
    
    private MenuItem mSelectAllItem;
    private boolean mCheckAll;
    
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
        inflater.inflate(R.menu.applist, menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
        mSelectAllItem = menu.findItem(R.id.menu_select_all);
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
        } else if (item.getItemId() == R.id.menu_save) {
            createNewFileDialog(getSelectedItems());
            return true;
        } else if (item.getItemId() == R.id.menu_share) {
            ArrayList<AppInfo> appInfoList = getSelectedItems();
            if (appInfoList.isEmpty()) {
                Toast.makeText(getSherlockActivity(), R.string.empty_list_error, Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getSherlockActivity(), ShareActivity.class);
                intent.putParcelableArrayListExtra(ShareActivity.APP_LIST, appInfoList);
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override 
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        loading(true);
        return new AppListLoader(getActivity());
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
    
    private void createNewFileDialog(final List<AppInfo> appList) {
        final Context context = getActivity();
        if (appList == null || appList.isEmpty()) {
            Toast.makeText(getActivity(), R.string.empty_list_error, Toast.LENGTH_SHORT).show();
        } else {
            NewFileDialog.showDialog(context, new NewFileDialog.Listener() {            
                @Override
                public void nameAccepted(String name) {
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(context, R.string.empty_name_error, Toast.LENGTH_SHORT).show();
                    } else {
                        new AppSaveTask(context, name, appList).execute(false);
                    }
                }
            });
        }
    }
    
    private ArrayList<AppInfo> getSelectedItems() {
        ArrayList<AppInfo> selectedApps = new ArrayList<AppInfo>();
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

    @Override
    public boolean isInfoButtonAvailable() {
        return true;
    }

}
