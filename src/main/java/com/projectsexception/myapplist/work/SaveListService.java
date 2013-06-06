package com.projectsexception.myapplist.work;

import java.io.File;
import java.util.*;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import com.projectsexception.myapplist.PreferenceActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.model.MyAppListDbHelper;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.xml.AppXMLHandler;
import com.projectsexception.myapplist.xml.FileUtil;
import com.projectsexception.myapplist.xml.ParserException;
import com.projectsexception.myapplist.xml.ParserUtil;

public class SaveListService extends IntentService {

    private static final String SERVICE_NAME = "SaveListService";

    public SaveListService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CustomLog.info("SaveListService", "Running service");
        // Filename
        String fileName = getString(R.string.backup_filename);
        // Get app list from system
        List<AppInfo> list = AppUtil.loadAppInfoList(getPackageManager(), true);
        File file = FileUtil.loadFile(this, fileName);
        if (file != null && file.exists()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean uninstalledApps = prefs.getBoolean(PreferenceActivity.KEY_BACKUP_UNINSTALLED_APPS, false);
            if (uninstalledApps) {
                // Get app list from backup file
                AppXMLHandler xmlHandler = new AppXMLHandler();
                try {
                    ParserUtil.launchParser(file, xmlHandler);
                    ArrayList<AppInfo> appInfoList = xmlHandler.getAppInfoList();
                    if (appInfoList != null && !appInfoList.isEmpty()) {
                        Set<String> packages = new HashSet<String>();
                        for (AppInfo appInfo : list) {
                            packages.add(appInfo.getPackageName());
                        }
                        for (AppInfo appInfo : appInfoList) {
                            if (!packages.contains(appInfo.getPackageName())) {
                                list.add(appInfo);
                            }
                        }
                    }
                } catch (ParserException e) {
                    CustomLog.error("SaveListService", e);
                }
            }
        }
        // Remove ignored apps
        MyAppListDbHelper dbHelper = new MyAppListDbHelper(this);
        List<String> packages = dbHelper.getPackages();
        for (Iterator<AppInfo> it = list.iterator(); it.hasNext();) {
            if (packages.contains(it.next().getPackageName())) {
                it.remove();
            }
        }
        // Create file
        FileUtil.writeFile(this, list, fileName);
        // Exit
        dbHelper.close();
    }
    
    public static void updateService(Context ctx) {
        setService(ctx, true);
    }
    
    public static void cancelService(Context ctx) {
        setService(ctx, false);
    }
    
    private static void setService(Context ctx, boolean enable) {
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, SaveListService.class);
        PendingIntent pintent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (enable) {
            long time = System.currentTimeMillis() + (1000 * 60 * 15);
            alarm.set(AlarmManager.RTC, time, pintent);
        } else {
            alarm.cancel(pintent);
        }
    }

}
