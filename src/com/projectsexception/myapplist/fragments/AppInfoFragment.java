package com.projectsexception.myapplist.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
            final PackageInfo packageInfo = AppUtil.loadPackageInfo(pManager, mPackage);
            final boolean isFromGPlay;
            if (packageInfo == null) {
                isFromGPlay = false;
            } else {
                isFromGPlay = AppUtil.isFromGooglePlay(pManager, mPackage);
            }
            populateView(pManager, packageInfo, isFromGPlay);
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
        checkStopButton();
    }

    @TargetApi(9)
    private void populateView(PackageManager pManager, PackageInfo packageInfo, boolean isFromGPlay) {
        TextView textView = (TextView) getView().findViewById(R.id.status);        
        if (packageInfo == null) {
            // Not installed
            ((ImageView) getView().findViewById(R.id.icon)).setImageResource(R.drawable.ic_default_launcher);
            ((TextView) getView().findViewById(R.id.title)).setText(mName);
            getView().findViewById(R.id.info).setEnabled(false);
            textView.setText(R.string.app_info_not_installed);
            getView().findViewById(R.id.version).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.app_data).setVisibility(View.GONE);
            getView().findViewById(R.id.play_linked).setVisibility(View.GONE);
            getView().findViewById(R.id.stop_application).setEnabled(false);            
            getView().findViewById(R.id.start_application).setEnabled(false);            
        } else {
            final ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            
            final String packageName = packageInfo.packageName;
            final Intent launchIntent = pManager.getLaunchIntentForPackage(packageName);
            
            ((ImageView) getView().findViewById(R.id.icon)).setImageDrawable(applicationInfo.loadIcon(pManager));
            ((TextView) getView().findViewById(R.id.title)).setText(applicationInfo.loadLabel(pManager));
            ((TextView) getView().findViewById(R.id.version)).setText(getString(R.string.app_info_version, packageInfo.versionName, packageInfo.versionCode));            
            
            if ((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE) {
                textView.setText(R.string.app_info_sd_installed);
            } else {
                textView.setText(R.string.app_info_local_installed);
            }
            
            if (isFromGPlay) {
                ((TextView) getView().findViewById(R.id.play_linked)).setText(R.string.app_info_play_linked);
            } else {
                ((TextView) getView().findViewById(R.id.play_linked)).setText(R.string.app_info_play_not_linked);                
            }
            
            checkStopButton();
            
            if (launchIntent == null) {
                getView().findViewById(R.id.start_application).setEnabled(false);
            } else {
                getView().findViewById(R.id.start_application).setOnClickListener(new View.OnClickListener() { 
                    @Override
                    public void onClick(View v) {
                        startActivity(launchIntent);
                    }
                });
            }
            
            textView = (TextView) getView().findViewById(R.id.app_date);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
                textView.setText(getString(R.string.app_info_date, 
                        dateFormat.format(new Date(packageInfo.firstInstallTime)), 
                        dateFormat.format(new Date(packageInfo.lastUpdateTime))));                
            } else {
                textView.setVisibility(View.GONE);
                getView().findViewById(R.id.app_date_sep).setVisibility(View.GONE);
            }
            
            textView = (TextView) getView().findViewById(R.id.permissions);
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions == null || permissions.length == 0) {
                textView.setText(R.string.app_info_no_permissions);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < permissions.length; i++) {
                    if (i > 0) {
                        sb.append('\n');
                    }
                    sb.append(permissions[i]);
                }
                textView.setText(sb);
            }
            
            getView().findViewById(R.id.info).setOnClickListener(this);
        }
        getView().findViewById(R.id.play).setOnClickListener(this);
    }

    private void checkStopButton() {
        final boolean isRunning = AppUtil.isRunning(getActivity(), mPackage);
        final View button = getView().findViewById(R.id.stop_application);
        if (button != null) {
            button.setEnabled(isRunning);
            if (isRunning) {
                button.setOnClickListener(new View.OnClickListener() {                    
                    @SuppressLint("NewApi")
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onClick(View v) {
                        ActivityManager manager = getActivityManager();
                        if (manager != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                                manager.killBackgroundProcesses(mPackage);
                            } else {
                                manager.restartPackage(mPackage);
                            }
                            v.setEnabled(false);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.info) {
            AppUtil.showInstalledAppDetails(getActivity(), mPackage);
        } else if (v.getId() == R.id.play) {
            AppUtil.showPlayGoogleApp(getActivity(), mPackage);
        }
    }
    
    protected ActivityManager getActivityManager() {
        ActivityManager  manager = null;
        if (getActivity() != null) {
            manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        }
        return manager;
    }

}
