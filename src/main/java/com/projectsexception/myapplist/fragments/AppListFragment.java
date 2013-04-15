package com.projectsexception.myapplist.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.ShareActivity;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.NewFileDialog;
import com.projectsexception.myapplist.work.AppListLoader;
import com.projectsexception.myapplist.work.AppSaveTask;

import java.util.ArrayList;
import java.util.List;

public class AppListFragment extends AbstractAppListFragment {

    @Override
    int getMenuAdapter() {
        return R.menu.adapter_app;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        setHasOptionsMenu(true);
//        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.applist, menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
    }

    @Override
    public void actionItemClicked(int id) {
        if (id == R.id.menu_save) {
            createNewFileDialog(mAdapter.getSelectedItems());
        } else if (id == R.id.menu_share) {
            ArrayList<AppInfo> appInfoList = mAdapter.getSelectedItems();
            if (appInfoList.isEmpty()) {
                Toast.makeText(getSherlockActivity(), R.string.empty_list_error, Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getSherlockActivity(), ShareActivity.class);
                intent.putParcelableArrayListExtra(ShareActivity.APP_LIST, appInfoList);
                startActivity(intent);
            }
        }
    }

    @Override 
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        loading(true);
        return new AppListLoader(getActivity());
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
}
