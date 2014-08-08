package com.projectsexception.myapplist.util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.projectsexception.myapplist.app.MyAppListApplication;
import com.projectsexception.myapplist.work.SaveListService;
import com.projectsexception.util.CustomLog;

public class BackupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CustomLog.initLog(MyAppListApplication.LOG_TAG, MyAppListApplication.LOG_LEVEL);
        CustomLog.getInstance().debug("BackupReceiver", "Receiver package changes, program the service");
        SaveListService.updateService(context);
    }

    /**
     * This method enables the Broadcast receiver registered in the
     * AndroidManifest file.
     * 
     * @param ctx Context
     */
    public static void enableReceiver(Context ctx) {
        setReceiver(ctx, true);
    }

    /**
     * This method disables the Broadcast receiver registered in the
     * AndroidManifest file.
     * 
     * @param ctx Context
     */
    public static void disableReceiver(Context ctx) {
        setReceiver(ctx, false);
    }

    private static void setReceiver(Context ctx, boolean enable) {
        int flag = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        ComponentName receiver = new ComponentName(ctx, BackupReceiver.class);
        PackageManager pm = ctx.getPackageManager();
        pm.setComponentEnabledSetting(receiver, flag, PackageManager.DONT_KILL_APP);
    }

}
