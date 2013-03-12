package com.projectsexception.myapplist.work;

import java.util.Iterator;
import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.projectsexception.myapplist.PreferenceActivity;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.model.MyAppListDbHelper;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.xml.FileUtil;

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
        // Get app list
        List<AppInfo> list = AppUtil.loadAppInfoList(getPackageManager(), true);
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
        // Set next alarm
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean(PreferenceActivity.KEY_BACKUP_CHECK, false)) {
            String periodString = sp.getString(PreferenceActivity.KEY_BACKUP_PERIOD, "6");
            updateService(this, true, periodString);
        }
        // Exit
        dbHelper.close();
    }
    
    public static void updateService(Context ctx, boolean active, String periodString) {
        int period;
        try {
            period = Integer.parseInt(periodString);
        } catch (Exception e) {
            period = 6;
        }
        
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, SaveListService.class);
        PendingIntent pintent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (active) {
            long time = System.currentTimeMillis() + (1000 * 60 * 60 * period);
            alarm.set(AlarmManager.RTC, time, pintent);
        } else {
            alarm.cancel(pintent);
        } 
    }

}
