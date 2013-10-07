package com.projectsexception.myapplist.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.ApplicationsReceiver;
import com.projectsexception.myapplist.view.ThemeManager;
import com.projectsexception.myapplist.view.TypefaceProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.InjectView;
import butterknife.Views;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class AppInfoFragment extends Fragment implements View.OnClickListener {
    
    public static interface CallBack {
        void removeAppInfoFragment();
    }
    
    static final String KEY_LISTENER = "AppInfoFragment";
    static final String NAME_ARG = "nameArg";
    static final String PERMISSION_PREFIX = "android.permission.";

    static final String PACKAGE_ARG = "packageArg";

    public static AppInfoFragment newInstance(String name, String packageName) {
        AppInfoFragment frg = new AppInfoFragment();
        Bundle args = new Bundle();
        args.putString(NAME_ARG, name);
        args.putString(PACKAGE_ARG, packageName);
        frg.setArguments(args);
        return frg;
    }
    
    private CallBack mCallBack;
    private String mName;
    private String mPackage;
    @InjectView(R.id.icon) ImageView mIcon;
    @InjectView(R.id.title) TextView mTitle;
    @InjectView(R.id.package_name) TextView mPackageName;
    @InjectView(R.id.status) TextView mStatus;
    @InjectView(R.id.info) View mInfo;
    @InjectView(R.id.play) View mPlay;
    @InjectView(R.id.version) TextView mVersion;
    @InjectView(R.id.app_data) View mApplicationData;
    @InjectView(R.id.play_linked) TextView mPlayLinked;
    @InjectView(R.id.stop_application) TextView mStopApplication;
    @InjectView(R.id.uninstall_application) TextView mUninstallApplication;
    @InjectView(R.id.start_application) TextView mStartApplication;
    @InjectView(R.id.app_date) TextView mApplicationDate;
    @InjectView(R.id.app_date_sep) View mApplicationDateSeparator;
    @InjectView(R.id.permissions) TextView mApplicationPermissions;

    public String getShownPackage() {
        return mPackage;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallBack = (CallBack) activity;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Activity must implement AppInfoFragment.CallBack");
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ApplicationsReceiver.getInstance(getActivity()).registerListener(KEY_LISTENER);
        mName = getArguments().getString(NAME_ARG);
        mPackage = getArguments().getString(PACKAGE_ARG);
        if (mPackage != null) {
            PackageManager pManager = getActivity().getPackageManager();
            final PackageInfo packageInfo = AppUtil.loadPackageInfo(pManager, mPackage);
            final boolean isFromGPlay = packageInfo != null && AppUtil.isFromGooglePlay(pManager, mPackage);
            populateView(pManager, packageInfo, isFromGPlay);
        } else {
            getView().setVisibility(View.GONE);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_info, container, false);
        Views.inject(this, view);
        mInfo.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        final FragmentActivity activity = getActivity();
        final ApplicationsReceiver receiver = ApplicationsReceiver.getInstance(activity);
        if (receiver.isContextChanged(KEY_LISTENER)) {
            mName = null;
            mPackage = null;
            receiver.removeListener(KEY_LISTENER);
            mCallBack.removeAppInfoFragment();
        }
        checkStopButton();
    }

    @TargetApi(9)
    private void populateView(PackageManager pManager, PackageInfo packageInfo, boolean isFromGPlay) {
        if (packageInfo == null) {
            // Not installed
            mIcon.setImageResource(R.drawable.ic_default_launcher);
            mTitle.setText(mName);
            mPackageName.setText(mPackage);
            mInfo.setEnabled(false);
            mVersion.setVisibility(View.INVISIBLE);
            mStatus.setText(R.string.app_info_not_installed);
            mApplicationData.setVisibility(View.GONE);
            mPlayLinked.setVisibility(View.GONE);
            mStopApplication.setEnabled(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mUninstallApplication.setEnabled(false);
            } else {
                mUninstallApplication.setVisibility(View.GONE);
            }
            mStartApplication.setEnabled(false);
        } else {
            final ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            final String packageName = packageInfo.packageName;
            final Intent launchIntent = pManager.getLaunchIntentForPackage(packageName);
            
            mIcon.setImageDrawable(applicationInfo.loadIcon(pManager));
            mTitle.setText(applicationInfo.loadLabel(pManager));
            mPackageName.setText(packageName);
            mInfo.setEnabled(true);
            mVersion.setText(getString(R.string.app_info_version, packageInfo.versionName, packageInfo.versionCode));
            if ((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE) {
                mStatus.setText(R.string.app_info_sd_installed);
            } else {
                mStatus.setText(R.string.app_info_local_installed);
            }
            
            if (isFromGPlay) {
                mPlayLinked.setText(R.string.app_info_play_linked);
            } else {
                mPlayLinked.setText(R.string.app_info_play_not_linked);
            }
            
            checkStopButton();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mUninstallApplication.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Activity activity = getActivity();
                        if (activity != null) {
                            try {
                                Uri packageUri = Uri.parse("package:" + packageName);
                                Intent i = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                                activity.startActivity(i);
                            } catch (Exception e) {
                                Crouton.makeText(activity, R.string.error_uninstall_application, Style.ALERT).show();
                            }
                        }
                    }
                });
            } else {
                mUninstallApplication.setVisibility(View.GONE);
            }
            
            if (launchIntent == null) {
                mStartApplication.setEnabled(false);
            } else {                
                mStartApplication.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Activity activity = getActivity();
                        if (activity != null) {
                            try {
                                activity.startActivity(launchIntent);
                            } catch (Exception e) {
                                Crouton.makeText(activity, R.string.error_start_application, Style.ALERT).show();
                            }
                        }
                    }
                });
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
                mApplicationDate.setText(getString(R.string.app_info_date,
                        dateFormat.format(new Date(packageInfo.firstInstallTime)),
                        dateFormat.format(new Date(packageInfo.lastUpdateTime))));
                mApplicationDateSeparator.setVisibility(View.VISIBLE);
            } else {
                mApplicationDate.setVisibility(View.GONE);
                mApplicationDateSeparator.setVisibility(View.GONE);
            }
            
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions == null || permissions.length == 0) {
                mApplicationPermissions.setText(R.string.app_info_no_permissions);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < permissions.length; i++) {
                    if (i > 0) {
                        sb.append('\n');
                    }
                    if (permissions[i].startsWith(PERMISSION_PREFIX)) {
                        sb.append(permissions[i].substring(PERMISSION_PREFIX.length()));
                    } else {
                        sb.append(permissions[i]);
                    }
                }
                mApplicationPermissions.setText(sb);
            }
        }

        if (ThemeManager.isFlavoredTheme(getActivity())) {
            TypefaceProvider.setTypeFace(getActivity(), mTitle, TypefaceProvider.FONT_BOLD);
            TypefaceProvider.setTypeFace(getActivity(), mPackageName, TypefaceProvider.FONT_REGULAR);
            TypefaceProvider.setTypeFace(getActivity(), mStatus, TypefaceProvider.FONT_REGULAR);
            TypefaceProvider.setTypeFace(getActivity(), mVersion, TypefaceProvider.FONT_REGULAR);
            TypefaceProvider.setTypeFace(getActivity(), mPlayLinked, TypefaceProvider.FONT_REGULAR);
            TypefaceProvider.setTypeFace(getActivity(), mStopApplication, TypefaceProvider.FONT_REGULAR);
            TypefaceProvider.setTypeFace(getActivity(), mUninstallApplication, TypefaceProvider.FONT_REGULAR);
            TypefaceProvider.setTypeFace(getActivity(), mStartApplication, TypefaceProvider.FONT_REGULAR);
            TypefaceProvider.setTypeFace(getActivity(), mApplicationDate, TypefaceProvider.FONT_REGULAR);
            TypefaceProvider.setTypeFace(getActivity(), mApplicationPermissions, TypefaceProvider.FONT_REGULAR);
        }
    }

    private void checkStopButton() {
        final boolean isRunning = AppUtil.isRunning(getActivity(), mPackage);
        if (mStopApplication != null) {
            mStopApplication.setEnabled(isRunning);
            if (isRunning) {
                mStopApplication.setOnClickListener(new View.OnClickListener() {
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
        if (v == mInfo) {
            AppUtil.showInstalledAppDetails(getActivity(), mPackage);
        } else if (v == mPlay) {
            AppUtil.showPlayGoogleApp(getActivity(), mPackage, false);
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
