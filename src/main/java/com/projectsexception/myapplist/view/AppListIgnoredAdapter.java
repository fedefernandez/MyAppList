package com.projectsexception.myapplist.view;

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

import java.util.ArrayList;

import butterknife.InjectView;
import butterknife.Views;

public class AppListIgnoredAdapter extends BaseAdapter {

    static class ViewHolder {
        @InjectView(android.R.id.text1) TextView title;
        @InjectView(android.R.id.icon1) ImageView icon;
        ViewHolder(View view) {
            Views.inject(this, view);
        }
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
        ViewHolder viewHolder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item_ignored, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        AppInfo item = (AppInfo) getItem(position);
        viewHolder.title.setText(item.getName());
        viewHolder.title.setTypeface(Typeface.DEFAULT_BOLD);

        Drawable icon = AppUtil.loadApplicationIcon(mPm, item.getPackageName());
        if (icon == null) {
            viewHolder.icon.setImageResource(R.drawable.ic_default_launcher);
        } else {
            viewHolder.icon.setImageDrawable(icon);
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
