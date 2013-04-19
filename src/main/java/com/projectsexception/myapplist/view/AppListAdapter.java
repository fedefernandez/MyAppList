package com.projectsexception.myapplist.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.manuelpeinado.multichoiceadapter.MultiChoiceBaseAdapter;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AppListAdapter extends MultiChoiceBaseAdapter {

    static class AppInfoView {
        TextView title;
        ImageView icon;
    }

    public static interface ActionListener {
        void actionItemClicked(int id);
    }
    
    private final LayoutInflater mInflater;
    private final PackageManager mPm;
    private ArrayList<AppInfo> mAppList;
    private int mNotInstalledColor;
    private int mInstalledColor;
    private int mMenu;
    private ActionListener mListener;

    public AppListAdapter(Context context, Bundle savedInstance, int menu) {
        super(savedInstance);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPm = context.getPackageManager();
        this.mAppList = new ArrayList<AppInfo>();
        this.mNotInstalledColor = context.getResources().getColor(R.color.app_not_installed);
        this.mMenu = menu;
    }

    public void setListener(ActionListener mListener) {
        this.mListener = mListener;
    }

    public void setData(ArrayList<AppInfo> data) {
        this.mAppList = data;
        notifyDataSetChanged();
    }
    
    public ArrayList<AppInfo> getData() {
        return mAppList;
    }

    @Override
    public View getViewImpl(int position, View convertView, ViewGroup parent) {
        View view;
        AppInfoView appInfoView;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item, parent, false);
            appInfoView = new AppInfoView();
            appInfoView.title = (TextView) view.findViewById(android.R.id.text1);
            mInstalledColor = appInfoView.title.getCurrentTextColor();
            appInfoView.icon = (ImageView) view.findViewById(android.R.id.icon1);
            view.setTag(appInfoView);
        } else {
            view = convertView;
            appInfoView = (AppInfoView) view.getTag();
        }

        AppInfo item = (AppInfo) getItem(position);
        appInfoView.title.setText(item.getName());
        Drawable icon = null;
        if (item.isInstalled()) {
            appInfoView.title.setTypeface(Typeface.DEFAULT_BOLD);
            appInfoView.title.setTextColor(mInstalledColor);
            icon = AppUtil.loadApplicationIcon(mPm, item.getPackageName());
        } else {
            appInfoView.title.setTypeface(Typeface.DEFAULT);
            appInfoView.title.setTextColor(mNotInstalledColor);
        }
        
        if (icon == null) {
            appInfoView.icon.setImageResource(R.drawable.ic_default_launcher);
        } else {
            appInfoView.icon.setImageDrawable(icon);
        }
        return view;
    }

    @Override
    public int getCount() {
        if (mAppList == null) {
            return 0;
        } else {
            return mAppList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mAppList == null || position >= mAppList.size()) {
            return null;
        } else {
            return mAppList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(mMenu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (mListener != null) {
            mListener.actionItemClicked(item.getItemId());
            finishActionMode();
            return true;
        }
        return false;
    }

    public ArrayList<AppInfo> getSelectedItems() {
        ArrayList<AppInfo> selectedApps = new ArrayList<AppInfo>();
        Set<Long> selection = getSelection();
        if (selection != null) {
            List<AppInfo> allApps = getData();
            int size = getCount();
            for (int i = 0 ; i < size ; i++) {
                if (selection.contains(new Long(i))) {
                    selectedApps.add(allApps.get(i));
                }
            }
        }
        return selectedApps;
    }
}
