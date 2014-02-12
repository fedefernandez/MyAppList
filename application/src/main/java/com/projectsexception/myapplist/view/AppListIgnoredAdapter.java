package com.projectsexception.myapplist.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.iconloader.IconView;
import com.projectsexception.myapplist.model.AppInfo;

import java.util.ArrayList;

import butterknife.InjectView;
import butterknife.ButterKnife;

public class AppListIgnoredAdapter extends BaseAdapter {

    static class ViewHolder {
        @InjectView(android.R.id.text1) TextView title;
        @InjectView(android.R.id.icon1) IconView icon;
        @InjectView(android.R.id.checkbox) CheckBox checkBox;
        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final PackageManager mPm;
    private ArrayList<AppInfo> mAppList;
    private boolean mAnimations;
    private int mLastAnimatedPosition;

    public AppListIgnoredAdapter(Context context, boolean animations) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPm = context.getPackageManager();
        this.mAppList = new ArrayList<AppInfo>();
        this.mAnimations = animations;
        this.mLastAnimatedPosition = -1;
    }

    public void setData(ArrayList<AppInfo> data) {
        this.mAppList = data;
        notifyDataSetChanged();
    }

    public void setAnimations(boolean animations) {
        mAnimations = animations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item, parent, false);
            viewHolder = new ViewHolder(view);
            viewHolder.checkBox.setVisibility(View.GONE);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        AppInfo item = (AppInfo) getItem(position);
        viewHolder.title.setText(item.getName());
        viewHolder.title.setTypeface(Typeface.DEFAULT_BOLD);
        viewHolder.icon.setPackageName(mPm, item.getPackageName(), R.drawable.ic_default_launcher, true);

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
}
