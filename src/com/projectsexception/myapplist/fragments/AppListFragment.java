package com.projectsexception.myapplist.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.MainActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppListLoader;
import com.projectsexception.myapplist.util.AppSaveTask;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.util.NewFileDialog;

public class AppListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<AppInfo>> {
    
    private static final String TAG = "AppListFragment";
    
    private AppListAdapter mAdapter;
    private MenuItem refreshItem;
    private MenuItem selectAllItem;
    private boolean checkAll;
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText("No applications");
        
        // We have a menu item to show in action bar if we have no file
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new AppListAdapter(getActivity());
        setListAdapter(mAdapter);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.applist, menu);
        refreshItem = menu.findItem(R.id.menu_refresh);
        selectAllItem = menu.findItem(R.id.menu_select_all);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {        
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_select_all) {
            checkAllItems();
        } else if (item.getItemId() == R.id.menu_save) {
            
            SparseBooleanArray sp = getListView().getCheckedItemPositions();
            if (sp == null || sp.size() == 0) {
                Toast.makeText(getActivity(), R.string.empty_list_error, Toast.LENGTH_SHORT).show();
            } else {
                List<AppInfo> allApps = mAdapter.getData();
                List<AppInfo> selectedApps = new ArrayList<AppInfo>();
                int size = sp.size();
                int index;
                for (int i = 0 ; i < size ; i++) {
                    index = sp.keyAt(i);
                    if (index < allApps.size()) {
                        selectedApps.add(allApps.get(index));
                    }
                }
                createNewFileDialog(selectedApps);
            }
        } else if (item.getItemId() == R.id.menu_refresh) {
            getLoaderManager().restartLoader(0, null, this);
        }
        return true;
    }

    @Override 
    public Loader<List<AppInfo>> onCreateLoader(int id, Bundle args) {
        CustomLog.debug(TAG, "onCreateLoader");
        loading(true);
        return new AppListLoader(getActivity());
    }

    @Override 
    public void onLoadFinished(Loader<List<AppInfo>> loader, List<AppInfo> data) {
        CustomLog.debug(TAG, "onLoadFinished");
        loading(false);
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
        CustomLog.debug(TAG, "onLoaderReset");
        loading(false);
        // Clear the data in the adapter.
        mAdapter.setData(null);
    }
    
    private void loading(boolean loading) {
        if (refreshItem != null) {
            if(loading) {
                refreshItem.setEnabled(false);
                refreshItem.setActionView(R.layout.refresh_loading);
            } else {
                refreshItem.setEnabled(true);
                refreshItem.setActionView(null);
            }           
        }
    }
    
    private void checkAllItems() {
        checkAll = !checkAll;
        final ListView list = getListView();
        final int count = getListAdapter().getCount();
        for ( int i = 0 ; i < count ; i++ ) {
            list.setItemChecked(i, checkAll);
        }
        if (checkAll) {
        	selectAllItem.setTitle(R.string.menu_unselect_all);
        } else {
        	selectAllItem.setTitle(R.string.menu_select_all);
        }
    }
    
    private void createNewFileDialog(final List<AppInfo> appList) {
        final Context context = getActivity();
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
    
    static class AppListAdapter extends BaseAdapter {
        
        private final LayoutInflater mInflater;
        private List<AppInfo> appList;

        public AppListAdapter(Context context) {
            this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<AppInfo> data) {
            this.appList = data;
            notifyDataSetChanged();
        }
        
        public List<AppInfo> getData() {
            return appList;
        }

        /**
         * Populate new items in the list.
         */
        @Override 
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_installed, parent, false);
            } else {
                view = convertView;
            }

            AppInfo item = (AppInfo) getItem(position);
            ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());
            ((TextView) view.findViewById(R.id.text)).setText(item.getName());

            return view;
        }

        @Override
        public int getCount() {
            if (appList == null) {
                return 0;
            } else {
                return appList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            if (appList == null || position >= appList.size()) {
                return null;
            } else {
                return appList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

}
