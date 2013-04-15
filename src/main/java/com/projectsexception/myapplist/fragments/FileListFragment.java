package com.projectsexception.myapplist.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.ListActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.ShareActivity;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.work.AppSaveTask;
import com.projectsexception.myapplist.work.FileListLoader;
import com.projectsexception.myapplist.xml.FileUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class FileListFragment extends AbstractAppListFragment {
    
    private File mFile;

    @Override
    int getMenuAdapter() {
        return R.menu.adapter_file;
    }

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
        if (item.getItemId() == R.id.menu_save) {
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
    public void actionItemClicked(int id) {
        if (id == R.id.menu_delete) {
            Set<Long> selection = mAdapter.getSelection();
            if (selection != null) {
                Iterator<AppInfo> it = mAdapter.getData().iterator();
                long pos = 0;
                while (it.hasNext()) {
                    it.next();
                    if (selection.contains(pos)) {
                        it.remove();
                    }
                    pos++;
                }
                mAdapter.notifyDataSetChanged();
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
}
