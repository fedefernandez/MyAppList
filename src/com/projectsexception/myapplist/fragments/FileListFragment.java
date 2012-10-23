package com.projectsexception.myapplist.fragments;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
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
import com.projectsexception.myapplist.MainActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppSaveTask;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.util.FileListLoader;
import com.projectsexception.myapplist.xml.FileUtil;

public class FileListFragment extends AbstractAppListFragment {
    
    private static final int MENU_REMOVE = 3;
    
    private File file;
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        if (getArguments() != null) {
            String fileName = getArguments().getString(ListActivity.ARG_FILE);
            if (fileName != null) {
                if (fileName.startsWith("file://")) {
                    try {
                        file = new File(new URI(fileName));
                    } catch (URISyntaxException e) {
                        CustomLog.error("FileListFragment", e);
                    }
                } else {
                    file = FileUtil.loadFile(fileName);
                }
                if (file == null || !file.exists() || !file.canRead()) {
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
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_refresh) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
        } else if (item.getItemId() == R.id.menu_save) {
            new AppSaveTask(getActivity(), file.getName(), mAdapter.getData()).execute(true);
        } else if (item.getItemId() == R.id.menu_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/xml");
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_file_text, FileUtil.APPLICATION_DIR));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_file_subject));
            Uri uri = Uri.parse("file://" + file.getAbsolutePath());
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            try {
                startActivity(Intent.createChooser(intent, "Share file..."));                
            } catch (Exception e) {
                // Something was wrong
            }
        }
        return true;
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
            showAppInfo(appInfo.getName(), appInfo.getPackageName());
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
    public boolean isInfoButtonAvailable() {
        return false;
    }

}
