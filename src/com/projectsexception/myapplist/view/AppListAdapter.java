package com.projectsexception.myapplist.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;

public class AppListAdapter extends BaseAdapter {
    
    private final LayoutInflater mInflater;
    private View.OnClickListener mListener;
    private List<AppInfo> appList;

    public AppListAdapter(Context context) {
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.appList = new ArrayList<AppInfo>();
    }
    
    public void setListener(View.OnClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<AppInfo> data) {
        this.appList = data;
        notifyDataSetChanged();
    }
    
    public List<AppInfo> getData() {
        return appList;
    }

    /**
     * Populate new items in the list.
     */
    @Override 
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item, parent, false);
        } else {
            view = convertView;
        }

        AppInfo item = (AppInfo) getItem(position);
        ImageView imageView = (ImageView) view.findViewById(R.id.list_item_icon);
        if (item.getIcon() == null) {
            imageView.setImageResource(R.drawable.ic_default_launcher);
        } else {
            imageView.setImageDrawable(item.getIcon());
        }
        TextView textView = (TextView) view.findViewById(R.id.list_item_text);
        textView.setText(item.getName());
        if (item.isInstalled()) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            textView.setTypeface(Typeface.DEFAULT);
        }
        View button = view.findViewById(R.id.list_item_details);
        if (mListener == null) {
            button.setVisibility(View.GONE);
        } else {
            button.setTag(item);
            button.setOnClickListener(mListener);
        }

        return view;
    }

    @Override
    public int getCount() {
        if (appList == null) {
            return 0;
        } else {
            return appList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (appList == null || position >= appList.size()) {
            return null;
        } else {
            return appList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
