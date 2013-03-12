package com.projectsexception.myapplist.work;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;

import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.xml.AppXMLHandler;
import com.projectsexception.myapplist.xml.FileUtil;
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
                fileAppList = xmlHandler.getAppInfoList();
            } catch (ParserException e) {
                CustomLog.error("FileListLoader", "Error loading file", e);
                if (e.getMessage() != null && e.getMessage().contains("invalid token")) {
                    String filePath = file.getAbsolutePath();
                    File backup = new File(filePath + "_backup");
                    if (file.renameTo(backup)) {
                        // Hemos renombrado, ahora corregimos
                        File newFile = new File(filePath);
                        fileAppList = FileUtil.fixFile(backup, newFile);                        
                    }
                }
            }
        }
        
        if (fileAppList != null) {
            AppInfo installed;
            for (AppInfo appInfo : fileAppList) {
                installed = AppUtil.loadAppInfo(mPm, appInfo.getPackageName());
                if (installed == null) {
                    appInfo.setInstalled(false);
                } else {
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