package com.projectsexception.myapplist.fragments;

import android.app.SearchManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.projectsexception.myapplist.MyAppListPreferenceActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.ApplicationsReceiver;
import com.projectsexception.myapplist.view.AppListAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class AbstractAppListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<ArrayList<AppInfo>>,
        AdapterView.OnItemClickListener,
        AppListAdapter.ActionListener,
        SearchView.OnQueryTextListener {

    private static final String KEY_LISTENER = "AbstractAppListFragment";

    protected static final String ARG_RELOAD = "reload";
    
    private MenuItem mRefreshItem;
    private AppListAdapter mAdapter;
    private String mSearchTerm;
    private boolean mListShown;
    private boolean mAnimations;
    @InjectView(android.R.id.list)
    ListView mListView;
    @InjectView(android.R.id.empty) View mEmptyView;
    @InjectView(android.R.id.progress) View mProgress;

    abstract int getMenuAdapter();
    abstract int getMenuResource();
    abstract Loader<ArrayList<AppInfo>> createLoader(int id, Bundle args);
    abstract void showAppInfo(String name, String packageName);
    abstract int getTitle();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSearchTerm = savedInstanceState.getString(SearchManager.QUERY);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAnimations = prefs.getBoolean(MyAppListPreferenceActivity.KEY_ANIMATIONS, true);
        mAdapter = new AppListAdapter(getActivity(), savedInstanceState, getMenuAdapter(), mAnimations);
        mAdapter.setSearchTerm(mSearchTerm);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setAdapterView(mListView);
        mAdapter.setListener(this);

        mListView.setFastScrollEnabled(true);
        mListView.setEmptyView(mEmptyView);

        // Start out with a progress indicator.
        setListShown(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            mAdapter.save(outState);
        }
        if (!TextUtils.isEmpty(mSearchTerm)) {
            outState.putString(SearchManager.QUERY, mSearchTerm);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ApplicationsReceiver.getInstance(getActivity()).isContextChanged(KEY_LISTENER)) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
        } else {
            setActionBarTitle();
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean animations = prefs.getBoolean(MyAppListPreferenceActivity.KEY_ANIMATIONS, true);
        if (mAnimations != animations) {
            mAnimations = animations;
            mAdapter.setAnimations(animations);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setActionBarTitle() {
        if (getActivity() != null) {
            String title = getString(getTitle());
            if (mAdapter != null) {
                title += " " + getString(R.string.ab_title_num, mAdapter.getCount());
            }
            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfo appInfo = mAdapter.getActualItems().get(position);
        if (!TextUtils.isEmpty(appInfo.getPackageName())) {
            if (appInfo.isInstalled()) {
                showAppInfo(appInfo.getName(), appInfo.getPackageName());
            } else {
                AppUtil.showPlayGoogleApp(getActivity(), appInfo.getPackageName(), false);
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        ApplicationsReceiver.unregisterListener(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(getMenuResource(), menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
        MenuItem item = menu.findItem(R.id.menu_search);
        if (item != null) {
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint(getString(R.string.find_apps));
            if (mSearchTerm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    item.expandActionView();
                }
                searchView.setQuery(mSearchTerm, false);
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_select_all) {
            for (int i = 0; i < mAdapter.getCount(); ++i) {
                mAdapter.setItemChecked(i, true);
            }
            return true;
        } else if (item.getItemId() == R.id.menu_refresh) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_RELOAD, true);
            getLoaderManager().restartLoader(0, args, this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override 
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        loading(true);
        return createLoader(id, args);
    }

    @Override 
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> data) {
        loading(false);
        
        ApplicationsReceiver.getInstance(getActivity()).registerListener(KEY_LISTENER);
        
        // Set the new data in the adapter.
        mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();

        // The list should now be shown.
        setListShown(true);

        setActionBarTitle();
    }

    @Override 
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
        loading(false);
        // Clear the data in the adapter.
        mAdapter.setData(null);
        mAdapter.notifyDataSetChanged();

        setActionBarTitle();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;

        // Don't do anything if the filter is empty
        if (mSearchTerm == null && newFilter == null) {
            return true;
        }

        // Don't do anything if the new filter is the same as the current filter
        if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
            return true;
        }

        // Updates current filter to new filter
        mSearchTerm = newFilter;
        mAdapter.setSearchTerm(mSearchTerm);
        mAdapter.notifyDataSetChanged();
        return false;
    }

    public void setListShown(boolean shown) {
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            mProgress.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            mProgress.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
        }
    }
    
    protected void loading(boolean loading) {
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
                MenuItemCompat.setActionView(mRefreshItem, R.layout.refresh_loading);
            } else {
                mRefreshItem.setEnabled(true);
                MenuItemCompat.setActionView(mRefreshItem, null);
            }           
        }
    }

    protected String getSearchTerm() {
        return mSearchTerm;
    }

    protected AppListAdapter getAdapter() {
        return mAdapter;
    }

    protected void setRefreshItem(MenuItem refreshItem) {
        mRefreshItem = refreshItem;
    }
}
