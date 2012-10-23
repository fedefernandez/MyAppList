package com.projectsexception.myapplist.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.ApplicationsReceiver;

public class AppInfoFragment extends SherlockFragment implements View.OnClickListener {
    
    public static interface ActivityInterface {
        void removeAppInfoFragment();
    }
    
    private static final String NAME_ARG = "nameArg";
    private static final String PACKAGE_ARG = "packageArg";
    
    public static AppInfoFragment newInstance(String name, String packageName) {
        AppInfoFragment frg = new AppInfoFragment();
        Bundle args = new Bundle();
        args.putString(NAME_ARG, name);
        args.putString(PACKAGE_ARG, packageName);
        frg.setArguments(args);
        return frg;
    }
    
    private ActivityInterface mActivity;
    private String mName;
    private String mPackage;    
    
    public String getShownPackage() {
        return mPackage;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = (ActivityInterface) activity;            
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Activity must implement AppInfoFragment.ActivityInterface");
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ApplicationsReceiver.getInstance(getSherlockActivity()).registerListener(getClass().getName());
        mName = getArguments().getString(NAME_ARG);
        mPackage = getArguments().getString(PACKAGE_ARG);
        if (mPackage != null) {
            PackageManager pManager = getActivity().getPackageManager();
            PackageInfo packageInfo = AppUtil.loadPackageInfo(pManager, mPackage);
            populateView(pManager, packageInfo);
        } else {
            getView().setVisibility(View.GONE);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.app_info, container, false);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        final SherlockFragmentActivity activity = getSherlockActivity();
        final ApplicationsReceiver receiver = ApplicationsReceiver.getInstance(activity);
        final String key = getClass().getName();
        if (receiver.isContextChanged(key)) {
            mName = null;
            mPackage = null;
            receiver.removeListener(key);
            mActivity.removeAppInfoFragment();
        }
    }

    private void populateView(PackageManager pManager, PackageInfo packageInfo) {        
        TextView textView = (TextView) getView().findViewById(R.id.status);        
        if (packageInfo == null) {
            // Not installed
            ((ImageView) getView().findViewById(R.id.icon)).setImageResource(R.drawable.ic_default_launcher);
            ((TextView) getView().findViewById(R.id.title)).setText(mName);
            getView().findViewById(R.id.info).setEnabled(false);
            textView.setText(R.string.app_info_not_installed);
            getView().findViewById(R.id.version).setVisibility(View.GONE);
            getView().findViewById(R.id.app_data).setVisibility(View.GONE);
        } else {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            ((ImageView) getView().findViewById(R.id.icon)).setImageDrawable(applicationInfo.loadIcon(pManager));
            ((TextView) getView().findViewById(R.id.title)).setText(applicationInfo.loadLabel(pManager));
            ((TextView) getView().findViewById(R.id.version)).setText(
                    getString(R.string.app_info_version, packageInfo.versionName, packageInfo.versionCode));            
            
            if ((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE) {
                textView.setText(R.string.app_info_sd_installed);                
            } else {
                textView.setText(R.string.app_info_local_installed);
            }
            
            textView = (TextView) getView().findViewById(R.id.app_date);
            final int apiLevel = Build.VERSION.SDK_INT;
            if (apiLevel >= 9) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                textView.setText(getString(R.string.app_info_date, 
                        dateFormat.format(new Date(packageInfo.firstInstallTime)), 
                        dateFormat.format(new Date(packageInfo.lastUpdateTime))));                
            } else {
                textView.setVisibility(View.GONE);
                getView().findViewById(R.id.app_date_sep).setVisibility(View.GONE);
            }
            
            textView = (TextView) getView().findViewById(R.id.permissions);
            PermissionInfo[] permissions = packageInfo.permissions;
            if (permissions == null || permissions.length == 0) {
                textView.setText(R.string.app_info_no_permissions);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < permissions.length; i++) {
                    if (i > 0) {
                        sb.append('\n');
                    }
                    sb.append(permissions[i].name);
                }
                textView.setText(sb);
            }
            
            textView = (TextView) getView().findViewById(R.id.activities);
            ActivityInfo[] activities = packageInfo.activities;
            if (activities == null || activities.length == 0) {
                textView.setText(R.string.app_info_no_activities);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < activities.length; i++) {
                    if (i > 0) {
                        sb.append('\n');
                    }
                    sb.append(activities[i].name);
                }
                textView.setText(sb);
            }
            
            getView().findViewById(R.id.info).setOnClickListener(this);
        }
        getView().findViewById(R.id.play).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.info) {
            AppUtil.showInstalledAppDetails(getActivity(), mPackage);
        } else if (v.getId() == R.id.play) {
            AppUtil.showPlayGoogleApp(getActivity(), mPackage);
        }
    }

}
