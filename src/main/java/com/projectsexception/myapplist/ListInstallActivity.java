package com.projectsexception.myapplist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.view.ThemeManager;

import java.util.ArrayList;

public class ListInstallActivity extends SherlockListActivity {

    public static final String ARG_APP_INFO_LIST = "appInfoList";

    private static final String ARG_INSTALLING = "installing";
    private static final int WHAT = 1;

    private boolean mInstallig;
    private ArrayList<AppInfo> mAppInfoList;
    private AppInstallAdapter mAdapter;
    private TextView mCancelButton;
    private int mTheme;

    @Override
    protected void onCreate(Bundle savedInstance) {
        mTheme = ThemeManager.getTheme(this);
        setTheme(mTheme);
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_install);
        mCancelButton = (TextView) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishProcess();
            }
        });
        mCancelButton.setVisibility(View.GONE);
        
        final ActionBar ab = getSupportActionBar();        
        ab.setTitle(R.string.ab_title_install_list);

        mInstallig = false;
        if (savedInstance == null) {
            mAppInfoList = getIntent().getParcelableArrayListExtra(ARG_APP_INFO_LIST);
        } else {
            mInstallig = savedInstance.getBoolean(ARG_INSTALLING);
            mAppInfoList = savedInstance.getParcelableArrayList(ARG_APP_INFO_LIST);
        }

        if (mAppInfoList == null || mAppInfoList.isEmpty()) {
            finishProcess();
        } else {
            mAdapter = new AppInstallAdapter(this, mAppInfoList);
            setListAdapter(mAdapter);
        }

        if (mInstallig) {
            mCancelButton.setVisibility(View.VISIBLE);
            sendMessage();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_install_title);
            builder.setMessage(R.string.dialog_install_message);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mCancelButton.setVisibility(View.VISIBLE);
                    mInstallig = true;
                    sendMessage();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finishProcess();
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTheme != ThemeManager.getTheme(this)) {
            ThemeManager.restartActivity(this);
        }
    }

    @Override
    protected void onStop() {
        handler.removeMessages(WHAT);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mInstallig) {
            mAdapter.setAppInfoList(mAppInfoList);
            sendMessage();
        }
    }

    void sendMessage() {
        Message m = handler.obtainMessage(WHAT);
        m.arg1 = 3;
        handler.sendMessage(m);
    }

    void finishProcess() {
        Toast.makeText(this, R.string.install_process_finished, Toast.LENGTH_SHORT).show();
        handler.removeMessages(WHAT);
        finish();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mAppInfoList == null || mAppInfoList.isEmpty()) {
                finishProcess();
            } else if (msg.arg1 > 0) {
                mCancelButton.setText(getString(R.string.cancel_install, msg.arg1));
                Message newMsg = obtainMessage(WHAT);
                newMsg.arg1 = msg.arg1 - 1;
                sendMessageDelayed(newMsg, 1000);
            } else {
                AppInfo appInfo = mAppInfoList.remove(0);
                AppUtil.showPlayGoogleApp(ListInstallActivity.this, appInfo.getPackageName(), true);
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_INSTALLING, mInstallig);
        outState.putParcelableArrayList(ARG_APP_INFO_LIST, mAppInfoList);
    }

    static class AppInfoView {
        TextView title;
        ImageView icon;
    }

    static class AppInstallAdapter extends BaseAdapter {

        LayoutInflater inflater;
        final PackageManager packageManager;
        ArrayList<AppInfo> appInfoList;

        AppInstallAdapter(Context context, ArrayList<AppInfo> appInfoList) {
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.packageManager = context.getPackageManager();
            this.appInfoList = appInfoList;
        }

        void setAppInfoList(ArrayList<AppInfo> appInfoList) {
            this.appInfoList = appInfoList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return appInfoList == null ? 0 : appInfoList.size();
        }

        @Override
        public Object getItem(int i) {
            return appInfoList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            AppInfoView appInfoView;
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_item, parent, false);
                appInfoView = new AppInfoView();
                appInfoView.title = (TextView) view.findViewById(android.R.id.text1);
                appInfoView.icon = (ImageView) view.findViewById(android.R.id.icon1);
                view.findViewById(android.R.id.checkbox).setVisibility(View.GONE);
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
                icon = AppUtil.loadApplicationIcon(packageManager, item.getPackageName());
            } else {
                appInfoView.title.setTypeface(Typeface.DEFAULT);
            }

            if (icon == null) {
                appInfoView.icon.setImageResource(R.drawable.ic_default_launcher);
            } else {
                appInfoView.icon.setImageDrawable(icon);
            }
            return view;
        }
    }
}
