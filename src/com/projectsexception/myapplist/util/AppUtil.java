package com.projectsexception.myapplist.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.R;

public class AppUtil {
    
    private static final String SCHEME = "package";
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
    private static final String APP_PKG_NAME_22 = "pkg";
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";
    
    public static List<AppInfo> loadAppInfoList(PackageManager mPm) {
        List<ApplicationInfo> apps = mPm.getInstalledApplications(PackageManager.GET_META_DATA);
        if (apps == null) {
            apps = new ArrayList<ApplicationInfo>();
        }

        // Create corresponding array of entries and load their labels.
        List<AppInfo> entries = new ArrayList<AppInfo>();
        AppInfo entry;
        for (ApplicationInfo applicationInfo : apps) {
            if (!isSystemPackage(applicationInfo)) {
                entry = createAppInfo(mPm, applicationInfo);
                entries.add(entry);
            }
        }
        return entries;
    }
    
    public static AppInfo loadAppInfo(PackageManager mPm, String packageName) {
        ApplicationInfo applicationInfo = loadApplicationInfo(mPm, packageName);
        AppInfo appInfo = null;
        if (applicationInfo != null) {
            appInfo = createAppInfo(mPm, applicationInfo);
        }
        return appInfo;
    }
    
    public static ApplicationInfo loadApplicationInfo(PackageManager mPm, String packageName) {
        try {
            return mPm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);            
        } catch (NameNotFoundException e) {
            return null;
        }
    }
    
    public static PackageInfo loadPackageInfo(PackageManager mPm, String packageName) {
        try {
            return mPm.getPackageInfo(packageName, 
                    PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS | PackageManager.GET_ACTIVITIES);            
        } catch (NameNotFoundException e) {
            return null;
        }
    }
    
    public static boolean isFromGooglePlay(PackageManager mPm, String packageName) {
        String installPM = mPm.getInstallerPackageName(packageName);
        if ( installPM == null ) {
            // Definitely not from Google Play
            return false;
        } else if (installPM.equals("com.google.android.feedback") || installPM.equals("com.android.vending")) {
            // Installed from the Google Play
            return true;
        }
        return false;
    }
    
    private static boolean isSystemPackage(ApplicationInfo pkgInfo) {
        return ((pkgInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) ? true : false;
    }
    
    private static AppInfo createAppInfo(PackageManager mPm, ApplicationInfo applicationInfo) {
        AppInfo entry = new AppInfo();
        entry.setPackageName(applicationInfo.packageName);
        entry.setName(applicationInfo.loadLabel(mPm).toString());
        entry.setIcon(applicationInfo.loadIcon(mPm));
        entry.setInstalled(true);
        return entry;
    }
    
    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        context.startActivity(intent);
    }
    
    public static void showPlayGoogleApp(Context context, String packageName) {
        String url = context.getString(R.string.play_google, packageName);
        try {
        	context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		} catch (Exception e) {
			Toast.makeText(context, R.string.problem_no_google_play, Toast.LENGTH_SHORT).show();
		}
    }

}
