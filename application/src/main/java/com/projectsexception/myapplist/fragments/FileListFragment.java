package com.projectsexception.myapplist.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.work.FileListLoader;
import com.projectsexception.myapplist.xml.FileUtil;
import com.projectsexception.util.CustomLog;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

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
    int getMenuResource() {
        return R.menu.fragment_file;
    }

    @Override
    Loader<ArrayList<AppInfo>> createLoader(int id, Bundle args) {
        ArrayList<AppInfo> lst;
        if (args == null || args.getBoolean(ARG_RELOAD, false) || getAdapter() == null) {
            lst = null;
        } else {
            lst = getAdapter().getActualItems();
        }
        return new FileListLoader(getActivity(), mFile, lst);
    }

    @Override
    void showAppInfo(String name, String packageName) {
        if (mCallBack != null) {
            mCallBack.showAppInfo(name, packageName);
        }
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
        if (getActivity() != null) {
            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.ab_title_file_list);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mCallBack != null && getAdapter() != null) {
            if (item.getItemId() == R.id.menu_save) {
                mCallBack.updateAppList(mFile.getName(), getAdapter().getActualItems());
                return true;
            } else if (item.getItemId() == R.id.menu_share) {
                mCallBack.shareAppList(mFile.getAbsolutePath(), getAdapter().getActualItems());
                return true;
            } else if (item.getItemId() == R.id.menu_install) {
                ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>(getAdapter().getActualItems());
                for (Iterator<AppInfo> it = appInfoList.iterator(); it.hasNext(); ) {
                    if (it.next().isInstalled()) {
                        it.remove();
                    }
                }
                if (appInfoList.isEmpty()) {
                    Crouton.makeText(getActivity(), R.string.empty_list_install_error, Style.ALERT).show();
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
        if (getAdapter() == null) {
            return;
        }
        if (id == R.id.menu_delete) {
            Set<Long> selection = getAdapter().getCheckedItems();
            if (selection != null) {
                Iterator<AppInfo> it = getAdapter().getActualItems().iterator();
                long pos = 0;
                while (it.hasNext()) {
                    it.next();
                    if (selection.contains(pos)) {
                        it.remove();
                    }
                    pos++;
                }
                getAdapter().notifyDataSetChanged();
            }
        } else if (id == R.id.menu_install) {
            ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>(getAdapter().getActualItems());
            Set<Long> selection = getAdapter().getCheckedItems();
            if (selection == null) {
                selection = new HashSet<Long>(0);
            }
            long pos = 0;
            Iterator<AppInfo> it = appInfoList.iterator();
            while (it.hasNext()) {
                it.next();
                if (!selection.contains(pos)) {
                    it.remove();
                }
                pos++;
            }
            if (appInfoList.isEmpty()) {
                Crouton.makeText(getActivity(), R.string.empty_list_error, Style.ALERT).show();
            } else {
                mCallBack.installAppList(appInfoList);
            }
        }
    }

    public void reloadFile(String fileName) {
        if (fileName != null) {
            if (fileName.startsWith("file://")) {
                try {
                    mFile = new File(new URI(fileName));
                } catch (URISyntaxException e) {
                    CustomLog.getInstance().error("FileListFragment", e);
                }
            } else {
                mFile = FileUtil.loadFile(getActivity(), fileName);
            }
            if (mFile == null || !mFile.exists() || !mFile.canRead()) {
                // If file not exists or can't read
                return;
            }
        }

        getLoaderManager().restartLoader(0, null, this);
    }
}
