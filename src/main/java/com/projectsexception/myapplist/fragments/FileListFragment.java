package com.projectsexception.myapplist.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.work.FileListLoader;
import com.projectsexception.myapplist.xml.FileUtil;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FileListFragment extends AbstractAppListFragment {

    public static FileListFragment newInstance(String fileName) {
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putString("fileName", fileName);
        FileListFragment fragment = new FileListFragment();
        fragment.setArguments(fragmentArgs);
        return fragment;
    }

    public static interface CallBack {
        void updateAppList(String fileName, List<AppInfo> appList);
        void shareAppList(String filePath, ArrayList<AppInfo> appList);
        void installAppList(ArrayList<AppInfo> appList);
        void showAppInfo(String name, String packageName);
    }

    private CallBack mCallBack;
    private File mFile;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CallBack) {
            mCallBack = (CallBack) activity;
        } else {
            throw new IllegalStateException("Activity must implement fragment's callback");
        }
    }

    @Override
    public void onDetach() {
        mCallBack = null;
        super.onDetach();
    }

    @Override
    int getMenuAdapter() {
        return R.menu.adapter_file;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        if (getArguments() != null) {
            reloadFile(getArguments().getString("fileName"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getSherlockActivity() != null) {
            getSherlockActivity().getSupportActionBar().setTitle(R.string.ab_title_file_list);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_file, menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mCallBack != null) {
            if (item.getItemId() == R.id.menu_save) {
                mCallBack.updateAppList(mFile.getName(), mAdapter.getData());
                return true;
            } else if (item.getItemId() == R.id.menu_share) {
                mCallBack.shareAppList(mFile.getAbsolutePath(), mAdapter.getData());
                return true;
            } else if (item.getItemId() == R.id.menu_install) {
                ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>(mAdapter.getData());
                for (Iterator<AppInfo> it = appInfoList.iterator(); it.hasNext(); ) {
                    if (it.next().isInstalled()) {
                        it.remove();
                    }
                }
                if (appInfoList.isEmpty()) {
                    Crouton.makeText(getSherlockActivity(), R.string.empty_list_install_error, Style.ALERT).show();
                } else {
                    mCallBack.installAppList(appInfoList);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void actionItemClicked(int id) {
        if (id == R.id.menu_delete) {
            Set<Long> selection = mAdapter.getCheckedItems();
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
        return new FileListLoader(getSherlockActivity(), mFile, lst);
    }

    @Override
    void showAppInfo(String name, String packageName) {
        if (mCallBack != null) {
            mCallBack.showAppInfo(name, packageName);
        }
    }

    public void reloadFile(String fileName) {
        if (fileName != null) {
            if (fileName.startsWith("file://")) {
                try {
                    mFile = new File(new URI(fileName));
                } catch (URISyntaxException e) {
                    CustomLog.error("FileListFragment", e);
                }
            } else {
                mFile = FileUtil.loadFile(getSherlockActivity(), fileName);
            }
            if (mFile == null || !mFile.exists() || !mFile.canRead()) {
                // If file not exists or can't read
                return;
            }
        }

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }
}
