package com.projectsexception.myapplist.util;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ApplicationsReceiver extends BroadcastReceiver {
    
    private static ApplicationsReceiver instance;
    
    public static ApplicationsReceiver getInstance(Context context) {
        if (instance == null) {
            instance = new ApplicationsReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            intentFilter.addDataScheme("package");
            context.registerReceiver(instance, intentFilter);
        }
        return instance;
    }
    
    private Map<String, Boolean> mContextChangedMap;
    
    public ApplicationsReceiver() {
        mContextChangedMap = new HashMap<String, Boolean>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        for (String key : mContextChangedMap.keySet()) {
            mContextChangedMap.put(key, true);
        }
    }
    
    public void registerListener(String key) {
        mContextChangedMap.put(key, false);
    }
    
    public void removeListener(String key) {
        mContextChangedMap.remove(key);
    }

    public boolean isContextChanged(String key) {
        if (mContextChangedMap.containsKey(key)) {
            return mContextChangedMap.get(key);            
        }
        return false;
    }
    
}