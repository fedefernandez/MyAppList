package com.projectsexception.myapplist.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.MainActivity;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppListLoader;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.xml.FileUtil;
import com.projectsexception.myapplist.R;

public class AppListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<AppInfo>> {
    
    private static final String TAG = "AppListFragment";
    
    private static final int MENU_SELECT_ALL = 1;
    private static final int MENU_SAVE = 2;
    private static final int MENU_REFRESH = 3;
    
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
        MenuItem item = menu.add(0, MENU_SAVE, 0, R.string.menu_save);
        item.setIcon(R.drawable.ic_menu_save);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        refreshItem = menu.add(0, MENU_REFRESH, 0, R.string.menu_refresh);
        refreshItem.setIcon(R.drawable.ic_menu_refresh);
        refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        selectAllItem = menu.add(0, MENU_SELECT_ALL, 0, R.string.menu_select_all);
        selectAllItem.setIcon(R.drawable.ic_menu_selectall);
        selectAllItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {        
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == MENU_SELECT_ALL) {
            checkAllItems();
        } else if (item.getItemId() == MENU_SAVE) {
            
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
        } else if (item.getItemId() == MENU_REFRESH) {
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
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.new_file_dialog_title);
        alert.setMessage(R.string.new_file_dialog_msg);
        // By default, the name is rmb-<date>.xml
        Time time = new Time();
        time.setToNow();
        String fileName = getString(R.string.new_file_dialog_name, time.format("%Y%m%d"));
        // Set an EditText view to get user input 
        final EditText input = new EditText(context);
        input.setText(fileName);
        alert.setView(input);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if (TextUtils.isEmpty(value)) {
                    Toast.makeText(context, R.string.empty_name_error, Toast.LENGTH_SHORT).show();
                } else {
                    new AppSaveTask(context, value, appList).execute(new Void[0]);
                }
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
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
    
    static class AppSaveTask extends AsyncTask<Void, Void, String> {
        
        private Context context;
        private String fileName;
        private List<AppInfo> appList;
        
        private AppSaveTask(Context context, String fileName, List<AppInfo> appList) {
            this.context = context;
            this.fileName = fileName;
            this.appList = appList;
        }
        
        @Override
        protected void onPreExecute() {
            Toast.makeText(context, R.string.export_init, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            if (appList != null && !appList.isEmpty()) {
                return FileUtil.writeFile(context, appList, fileName);
            } else {
                return context.getString(R.string.empty_list_post_error);
            }
        }
        
        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(context, R.string.export_successfully, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.export_failed, result), Toast.LENGTH_LONG).show();
            }
        }
        
    }

}
