package com.projectsexception.myapplist.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;

public class AppListAdapter extends BaseAdapter {
    
    static class AppInfoView {
        TextView title;
        ImageView icon;
        View button;
    }
    
    private final LayoutInflater mInflater;
    private final PackageManager mPm;
    private View.OnClickListener mListener;
    private List<AppInfo> mAppList;
    private int mNotInstalledColor;
    private int mInstalledColor;

    public AppListAdapter(Context context) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPm = context.getPackageManager();
        this.mAppList = new ArrayList<AppInfo>();
        this.mNotInstalledColor = context.getResources().getColor(R.color.app_not_installed);
    }
    
    public void setListener(View.OnClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<AppInfo> data) {
        this.mAppList = data;
        notifyDataSetChanged();
    }
    
    public List<AppInfo> getData() {
        return mAppList;
    }

    @Override 
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        AppInfoView appInfoView;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item, parent, false);
            appInfoView = new AppInfoView();
            appInfoView.title = (TextView) view.findViewById(R.id.list_item_text);
            mInstalledColor = appInfoView.title.getCurrentTextColor();
            appInfoView.icon = (ImageView) view.findViewById(R.id.list_item_icon);
            appInfoView.button = view.findViewById(R.id.list_item_details);
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
        
        if (mListener == null) {
            appInfoView.button.setVisibility(View.GONE);
        } else {
            appInfoView.button.setVisibility(View.VISIBLE);
            appInfoView.button.setTag(item);
            appInfoView.button.setOnClickListener(mListener);
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
}
