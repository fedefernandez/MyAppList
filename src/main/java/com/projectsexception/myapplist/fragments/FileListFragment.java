package com.projectsexception.myapplist.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.ListActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.ShareActivity;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.work.AppSaveTask;
import com.projectsexception.myapplist.work.FileListLoader;
import com.projectsexception.myapplist.xml.FileUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class FileListFragment extends AbstractAppListFragment {
    
    private static final int MENU_REMOVE = 3;
    
    private File mFile;
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        if (getArguments() != null) {
            String fileName = getArguments().getString(ListActivity.ARG_FILE);
            if (fileName != null) {
                if (fileName.startsWith("file://")) {
                    try {
                        mFile = new File(new URI(fileName));
                    } catch (URISyntaxException e) {
                        CustomLog.error("FileListFragment", e);
                    }
                } else {
                    mFile = FileUtil.loadFile(fileName);
                }
                if (mFile == null || !mFile.exists() || !mFile.canRead()) {
                    // If file not exists or can't read
                    return;
                }
            }
        }

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.filelist, menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
            return true;
        } else if (item.getItemId() == R.id.menu_save) {
            new AppSaveTask(getActivity(), mFile.getName(), mAdapter.getData()).execute(true);
            return true;
        } else if (item.getItemId() == R.id.menu_share) {
            Intent intent = new Intent(getSherlockActivity(), ShareActivity.class);
            intent.putExtra(ShareActivity.FILE_PATH, mFile.getAbsolutePath());
            ArrayList<AppInfo> appInfoList = mAdapter.getData();
            intent.putParcelableArrayListExtra(ShareActivity.APP_LIST, appInfoList);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.add(info.position, MENU_REMOVE, 0, R.string.context_menu_remove_from_list);
    }
    
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        int position = item.getGroupId();
        if (position < mAdapter.getData().size()) {
            if (item.getItemId() == MENU_REMOVE) {
                mAdapter.getData().remove(position);
                mAdapter.notifyDataSetChanged();                
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }
    
    @Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
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
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        loading(true);
        ArrayList<AppInfo> lst;
        if (args == null || args.getBoolean(ARG_RELOAD, false)) {
            lst = null;
        } else {
            lst = mAdapter.getData();
        }
        return new FileListLoader(getActivity(), mFile, lst);
    }

    @Override
    public boolean isInfoButtonAvailable() {
        return false;
    }

}
