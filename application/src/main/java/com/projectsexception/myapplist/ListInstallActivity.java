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
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;

import java.util.ArrayList;

import butterknife.InjectView;
import butterknife.Views;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ListInstallActivity extends BaseActivity implements View.OnClickListener {

    public static final String ARG_APP_INFO_LIST = "appInfoList";

    private static final String ARG_STATUS = "status";
    private static final int STATE_INIT = 0;
    private static final int STATE_INSTALLING = 1;
    private static final int STATE_FINISHED = 2;
    private static final int WHAT = 1;
    private static final int SECONDS = 2;

    private int mStatus;
    private ArrayList<AppInfo> mAppInfoList;
    private AppInstallAdapter mAdapter;
    @InjectView(R.id.cancel_button_layout) View mCancelButtonLayout;
    @InjectView(R.id.cancel_button) TextView mCancelButton;
    private int mTheme;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_install);
        Views.inject(this);

        mCancelButton.setOnClickListener(this);

        final ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.ab_title_install_list);

        mStatus = STATE_INIT;
        if (savedInstance == null) {
            mAppInfoList = getIntent().getParcelableArrayListExtra(ARG_APP_INFO_LIST);
        } else {
            mStatus = savedInstance.getInt(ARG_STATUS);
            mAppInfoList = savedInstance.getParcelableArrayList(ARG_APP_INFO_LIST);
        }

        mAdapter = new AppInstallAdapter(this, mAppInfoList);
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);

        if (mStatus == STATE_INIT) {
            mCancelButtonLayout.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_install_title);
            builder.setMessage(R.string.dialog_install_message);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mCancelButtonLayout.setVisibility(View.VISIBLE);
                    mStatus = STATE_INSTALLING;
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
        } else if (mStatus == STATE_FINISHED) {
            finishProcess();
        } else {
            // mStatus == STATE_INSTALLING;
            sendMessage();
        }
    }

    @Override
    public void onStop() {
        handler.removeMessages(WHAT);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mStatus == STATE_INSTALLING) {
            mAdapter.setAppInfoList(mAppInfoList);
            sendMessage();
        }
    }

    void sendMessage() {
        Message m = handler.obtainMessage(WHAT);
        m.arg1 = SECONDS;
        handler.sendMessage(m);
    }

    void finishProcess() {
        handler.removeMessages(WHAT);
        mStatus = STATE_FINISHED;
        mCancelButtonLayout.setBackgroundColor(getResources().getColor(R.color.install_success_button));
        mCancelButton.setText(R.string.install_process_finished);
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
        outState.putInt(ARG_STATUS, mStatus);
        outState.putParcelableArrayList(ARG_APP_INFO_LIST, mAppInfoList);
    }

    @Override
    public void onClick(View v) {
        if (mStatus == STATE_INSTALLING) {
            finishProcess();
        } else {
            finish();
        }
    }

    static class ViewHolder {
        @InjectView(android.R.id.text1) TextView title;
        @InjectView(android.R.id.icon1) ImageView icon;
        @InjectView(android.R.id.checkbox) CheckBox checkBox;
        ViewHolder(View view) {
            Views.inject(this, view);
        }
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
            ViewHolder viewHolder;
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_item, parent, false);
                viewHolder = new ViewHolder(view);
                viewHolder.checkBox.setVisibility(View.GONE);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }

            AppInfo item = (AppInfo) getItem(position);
            viewHolder.title.setText(item.getName());
            Drawable icon = null;
            if (item.isInstalled()) {
                viewHolder.title.setTypeface(Typeface.DEFAULT_BOLD);
                icon = AppUtil.loadApplicationIcon(packageManager, item.getPackageName());
            } else {
                viewHolder.title.setTypeface(Typeface.DEFAULT);
            }

            if (icon == null) {
                viewHolder.icon.setImageResource(R.drawable.ic_default_launcher);
            } else {
                viewHolder.icon.setImageDrawable(icon);
            }
            return view;
        }
    }
}
