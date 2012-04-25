package com.projectsexception.myapplist.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.CustomLog;

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
        
        XmlSerializer serializer = Xml.newSerializer();
        try {
            FileWriter writer = new FileWriter(file);
            serializer.setOutput(writer);
            try {
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            } catch (Exception e) {
                CustomLog.error("FileUtil", "Indent not supported", e);
            }
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "app-list");
            if (appList != null) {
                for (AppInfo appInfo : appList) {
                    serializer.startTag("", "app");
                    serializer.attribute("", "name", appInfo.getName());
                    serializer.attribute("", "package", appInfo.getPackageName());
                    serializer.endTag("", "app");
                }
            }
            serializer.endTag("", "app-list");
            serializer.endDocument();
            writer.close();
            return null;
        } catch (Exception e) {
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
