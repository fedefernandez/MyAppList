package com.projectsexception.myapplist.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.PreferenceActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.model.MyAppListDbHelper;
import com.projectsexception.myapplist.view.AppListIgnoredAdapter;
import com.projectsexception.myapplist.work.AppListLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.Views;

public class IgnoredListFragment extends SherlockListFragment implements
        LoaderManager.LoaderCallbacks<ArrayList<AppInfo>>,
        AdapterView.OnItemClickListener {

    public static interface CallBack {
        MyAppListDbHelper getHelper();
    }
    
    private CallBack mCallBack;
    private MenuItem mRefreshItem;
    private AppListIgnoredAdapter mAdapter;
    private SparseBooleanArray mCheckItems;
    private boolean mListShown;
    private boolean mAnimations;
    @InjectView(android.R.id.list) ListView mListView;
    @InjectView(android.R.id.empty) View mEmptyView;
    @InjectView(android.R.id.progress) View mProgress;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CallBack) {
            mCallBack = (CallBack) activity;
        } else {
            throw new IllegalStateException("activity must implement fragment's callback");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        Views.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCheckItems = new SparseBooleanArray();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
        mAnimations = prefs.getBoolean(PreferenceActivity.KEY_ANIMATIONS, true);
        mAdapter = new AppListIgnoredAdapter(getSherlockActivity(), mAnimations);
        mListView = getListView();
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setOnItemClickListener(this);

        // Start out with a progress indicator.
        setListShown(false);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
        boolean animations = prefs.getBoolean(PreferenceActivity.KEY_ANIMATIONS, true);
        if (mAnimations != animations) {
            mAnimations = animations;
            mAdapter.setAnimations(animations);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_ign, menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_select_all) {
            boolean select = false;
            // By default we are going to uncheck all
            for (int i = 0; i < mListView.getCount(); ++i) {
                if (!mCheckItems.get(i, false)) {
                    // If there are one element not checked
                    select = true;
                    break;
                }
            }
            for (int i = 0; i < mListView.getCount(); ++i) {
                mListView.setItemChecked(i, select);
                mCheckItems.put(i, select);
            }
            return true;
        } else if (item.getItemId() == R.id.menu_refresh) {
            getLoaderManager().restartLoader(0, null, this);
            return true;
        } else if (item.getItemId() == R.id.menu_save) {
            saveSelectedItems(getSelectedItems());
            getSherlockActivity().finish(); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override 
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        loading(true);
        return new AppListLoader(getActivity());
    }
    
    @Override 
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> data) {
        loading(false);

        // Set the new data in the adapter.
        mAdapter.setData(data);

        // The list should now be shown.
        setListShown(true);

        if (data != null) {
            // Check ignored items
            final List<String> ignored = mCallBack.getHelper().getPackages();
            final int count = mAdapter.getCount();
            for ( int i = 0 ; i < count ; i++ ) {
                boolean checked = ignored.contains(data.get(i).getPackageName());
                mListView.setItemChecked(i, checked);
                mCheckItems.put(i, checked);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
        loading(false);
        // Clear the data in the adapter.
        mAdapter.setData(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean actual = !mCheckItems.get(position);
        mListView.setItemChecked(position, actual);
        mCheckItems.put(position, actual);
    }

    public void setListShown(boolean shown) {
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            mProgress.setVisibility(View.INVISIBLE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            mProgress.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
        }
    }

    private void loading(boolean loading) {
        if (mEmptyView != null) {
            if (loading) {
                mEmptyView.setVisibility(View.INVISIBLE);
            } else {
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }

        if (mRefreshItem != null) {
            if(loading) {
                mRefreshItem.setEnabled(false);
                mRefreshItem.setActionView(R.layout.refresh_loading);
            } else {
                mRefreshItem.setEnabled(true);
                mRefreshItem.setActionView(null);
            }
        }
    }

    private void saveSelectedItems(List<AppInfo> selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            mCallBack.getHelper().deletePackages();
        } else {
            List<String> packages = new ArrayList<String>();
            for (AppInfo appInfo : selectedItems) {
                packages.add(appInfo.getPackageName());
            }
            mCallBack.getHelper().savePackages(packages);
        }
    }

    private List<AppInfo> getSelectedItems() {
        ArrayList<AppInfo> selectedApps = new ArrayList<AppInfo>();
        for (int i = 0 ; i < mCheckItems.size() ; i++) {
            if (mCheckItems.valueAt(i)) {
                selectedApps.add((AppInfo) mAdapter.getItem(mCheckItems.keyAt(i)));
            }
        }
        return selectedApps;
    }

}
