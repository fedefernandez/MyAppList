package com.projectsexception.myapplist.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.ListActivity;
import com.projectsexception.myapplist.MainActivity;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.FileListLoader;
import com.projectsexception.myapplist.xml.FileUtil;
import com.projectsexception.myapplist.R;

public class FileListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<AppInfo>> {

    private static final String ARG_RELOAD = "reload";

    private static final int MENU_REFRESH = 1;
    
    private AppListAdapter mAdapter;
    private MenuItem refreshItem;
    private File file;
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText(getActivity().getString(R.string.fragment_list_empty));
        
        if (getArguments() != null) {
            String fileName = getArguments().getString(ListActivity.ARG_FILE);
            if (fileName != null) {
                // We are going to check
                file = FileUtil.loadFile(fileName);
                if (file == null || !file.exists() || !file.canRead()) {
                    // If file not exists or can't red
                    return;
                }
            }
        }

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new AppListAdapter(getActivity());
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        refreshItem = menu.add(0, MENU_REFRESH, 0, R.string.menu_refresh);
        refreshItem.setIcon(R.drawable.ic_menu_refresh);
        refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {        
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == MENU_REFRESH) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
        }
        return true;
    }
    
    @Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
        AppInfo appInfo = mAdapter.getData().get(position);
        if (!TextUtils.isEmpty(appInfo.getPackageName())) {
            if (appInfo.isInstalled()) {
                AppUtil.showInstalledAppDetails(getActivity(), appInfo.getPackageName());
            } else {
                AppUtil.showPlayGoogleApp(getActivity(), appInfo.getPackageName());
            }
        }
    }
    
    @Override 
    public Loader<List<AppInfo>> onCreateLoader(int id, Bundle args) {
        loading(true);
        List<AppInfo> lst;
        if (args == null || args.getBoolean(ARG_RELOAD, false)) {
            lst = null;
        } else {
            lst = mAdapter.getData();
        }
        return new FileListLoader(getActivity(), file, lst);
    }

    @Override 
    public void onLoadFinished(Loader<List<AppInfo>> loader, List<AppInfo> data) {
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
    
    static class AppListAdapter extends BaseAdapter {
        
        private final LayoutInflater mInflater;
        private List<AppInfo> appList;

        public AppListAdapter(Context context) {
            this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.appList = new ArrayList<AppInfo>();
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
                view = mInflater.inflate(R.layout.list_item, parent, false);
            } else {
                view = convertView;
            }

            AppInfo item = (AppInfo) getItem(position);
            ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            if (item.getIcon() == null) {
                imageView.setImageResource(R.drawable.ic_default_launcher);
            } else {
                imageView.setImageDrawable(item.getIcon());
            }
            ((TextView) view.findViewById(R.id.text)).setText(item.getName());
            if (item.isInstalled()) {
                view.findViewById(R.id.installed).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.installed).setVisibility(View.INVISIBLE);
            }

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
