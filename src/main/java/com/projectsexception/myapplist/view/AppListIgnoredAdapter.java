package com.projectsexception.myapplist.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

public class AppListIgnoredAdapter extends BaseAdapter {

    static class AppInfoView {
        TextView title;
        ImageView icon;
    }

    private final LayoutInflater mInflater;
    private final PackageManager mPm;
    private ArrayList<AppInfo> mAppList;

    public AppListIgnoredAdapter(Context context) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPm = context.getPackageManager();
        this.mAppList = new ArrayList<AppInfo>();
    }

    public void setData(ArrayList<AppInfo> data) {
        this.mAppList = data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        AppInfoView appInfoView;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item_ignored, parent, false);
            appInfoView = new AppInfoView();
            appInfoView.title = (TextView) view.findViewById(android.R.id.text1);
            appInfoView.icon = (ImageView) view.findViewById(android.R.id.icon1);
            view.setTag(appInfoView);
        } else {
            view = convertView;
            appInfoView = (AppInfoView) view.getTag();
        }

        AppInfo item = (AppInfo) getItem(position);
        appInfoView.title.setText(item.getName());
        appInfoView.title.setTypeface(Typeface.DEFAULT_BOLD);

        Drawable icon = AppUtil.loadApplicationIcon(mPm, item.getPackageName());
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
}
