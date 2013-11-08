package com.projectsexception.myapplist.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.manuelpeinado.multichoiceadapter.extras.actionbarcompat.MultiChoiceBaseAdapterFix;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.iconloader.IconView;
import com.projectsexception.myapplist.model.AppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.InjectView;
import butterknife.Views;

public class AppListAdapter extends MultiChoiceBaseAdapterFix implements View.OnClickListener {

    static class ViewHolder {
        @InjectView(android.R.id.text1) TextView title;
        @InjectView(android.R.id.icon1) IconView icon;
        @InjectView(android.R.id.checkbox) CheckBox checkBox;
        ViewHolder(View view) {
            Views.inject(this, view);
        }
    }

    public static interface ActionListener {
        void actionItemClicked(int id);
    }

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final PackageManager mPm;
    private ArrayList<AppInfo> mAppList;
    private int mNotInstalledColor;
    private int mInstalledColor;
    private int mMenu;
    private ActionListener mListener;
    private boolean mAnimations;
    private int mLastAnimatedPosition;

    public AppListAdapter(Context context, Bundle savedInstance, int menu, boolean animations) {
        super(savedInstance);
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPm = context.getPackageManager();
        this.mAppList = new ArrayList<AppInfo>();
        this.mNotInstalledColor = context.getResources().getColor(R.color.app_not_installed);
        this.mMenu = menu;
        this.mAnimations = animations;
        this.mLastAnimatedPosition = -1;
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

    public void setAnimations(boolean animations) {
        mAnimations = animations;
    }

    @Override
    public View getViewImpl(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item, parent, false);
            viewHolder = new ViewHolder(view);
            mInstalledColor = viewHolder.title.getCurrentTextColor();
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        AppInfo item = (AppInfo) getItem(position);
        viewHolder.title.setText(item.getName());
        if (item.isInstalled()) {
            viewHolder.title.setTypeface(Typeface.DEFAULT_BOLD);
            viewHolder.title.setTextColor(mInstalledColor);
        } else {
            viewHolder.title.setTypeface(Typeface.DEFAULT);
            viewHolder.title.setTextColor(mNotInstalledColor);
        }

        viewHolder.icon.setPackageName(mPm, item.getPackageName(), R.drawable.ic_default_launcher, true);

        if (viewHolder.checkBox.getVisibility() == View.GONE) {
            viewHolder.icon.setTag(position);
            viewHolder.icon.setOnClickListener(this);
        }

        if (ThemeManager.isFlavoredTheme(mContext)) {
            TypefaceProvider.setTypeFace(mContext, viewHolder.title, TypefaceProvider.FONT_BOLD);
            if (mAnimations && position > mLastAnimatedPosition) {
                AnimationUtil.animateIn(view);
                mLastAnimatedPosition = position;
            }
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

    @Override
    public void onClick(View v) {
        Integer position = (Integer) v.getTag();
        if (v.getTag() != null) {
            setItemChecked(position, !isChecked(position));
        }
    }

    public ArrayList<AppInfo> getSelectedItems() {
        ArrayList<AppInfo> selectedApps = new ArrayList<AppInfo>();
        Set<Long> selection = getCheckedItems();
        if (selection != null) {
            List<AppInfo> allApps = getData();
            int size = getCount();
            for (int i = 0 ; i < size ; i++) {
                if (selection.contains(Long.valueOf(i))) {
                    selectedApps.add(allApps.get(i));
                }
            }
        }
        return selectedApps;
    }
}
