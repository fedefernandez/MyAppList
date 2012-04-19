package com.projectsexception.myapplist.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Environment;

import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.R;

public class FileUtil {
    
    private static final String APPLICATION_DIR = "MyAppList";

    public static File prepareApplicationDir() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcard = Environment.getExternalStorageDirectory();
            File applicationFolder = new File(sdcard, APPLICATION_DIR);
            if (!applicationFolder.exists()) {
                if (!applicationFolder.mkdir()) {
                    return null;
                }
            }
            return applicationFolder;
        }
        return null;
    }
    
    public static String writeFile(Context context, List<AppInfo> appList, String fileName) {
        File dir = prepareApplicationDir();
        if (dir == null) {
            return context.getString(R.string.error_creating_dir, APPLICATION_DIR);
        }
        
        File file = new File(dir, fileName);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            out.append("<app-list>\n");
            if (appList != null) {
                for (AppInfo appInfo : appList) {
                    out.append("    <app name=\"");
                    out.append(appInfo.getName());
                    out.append("\" package=\"");
                    out.append(appInfo.getPackageName());
                    out.append("\"/>\n");
                }
            }
            out.append("</app-list>");
            out.close();
            return null;
        } catch (IOException e) {
            return context.getString(R.string.error_creating_file, file.getAbsolutePath());
        }
    }
    
    public static String[] loadFiles() {
        File dir = prepareApplicationDir();
        if (dir != null && dir.exists() && dir.isDirectory()) {
            XMLFilenameFilter filter = new XMLFilenameFilter();
            String[] files = dir.list(filter);
            Arrays.sort(files);
            return files;
        }
        return null;
    }
    
    public static File loadFile(String fileName) {
        File dir = prepareApplicationDir();
        if (dir != null && dir.exists() && dir.isDirectory()) {
            return new File(dir, fileName);
        }
        return null;
    }
    
    static class XMLFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(".xml");
        }
        
    }

}
