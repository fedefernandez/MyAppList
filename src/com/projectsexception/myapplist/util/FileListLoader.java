package com.projectsexception.myapplist.util;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;

import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.xml.AppXMLHandler;
import com.projectsexception.myapplist.xml.ParserException;
import com.projectsexception.myapplist.xml.ParserUtil;

public class FileListLoader extends AbstractListLoader {
    
    private final File file;
    private List<AppInfo> fileAppList;
    private final PackageManager mPm;

    public FileListLoader(Context context, File file, List<AppInfo> fileAppList) {
        super(context);
        this.file = file;
        this.fileAppList = fileAppList;
        this.mPm = getContext().getPackageManager();
    }

    @Override
    public List<AppInfo> loadAppInfoList() {
        if (fileAppList == null || fileAppList.isEmpty()) {
            AppXMLHandler xmlHandler = new AppXMLHandler();
            try {
                ParserUtil.launchParser(file, xmlHandler);
            } catch (ParserException e) {
                CustomLog.error("FileListLoader", "Error loading file", e);
            }
            fileAppList = xmlHandler.getAppInfoList();
        }
        
        if (fileAppList != null) {
            AppInfo installed;
            for (AppInfo appInfo : fileAppList) {
                installed = AppUtil.loadAppInfo(mPm, appInfo.getPackageName());
                if (installed == null) {
                    appInfo.setIcon(null);
                    appInfo.setInstalled(false);
                } else {
                    appInfo.setIcon(installed.getIcon());
                    appInfo.setInstalled(true);
                }
            }
        }
        
        return fileAppList;
    }

    @Override
    public boolean isPackageIntentReceiver() {
        return true;
    }
}