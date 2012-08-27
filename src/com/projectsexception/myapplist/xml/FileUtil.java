package com.projectsexception.myapplist.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.CustomLog;

public class FileUtil {
    
    private static final String PATTERN_PACKAGE_NAME = "<app\\s+package=\"([^\"]+)\"\\s+name=\"([^\"]+)\"\\s*\\/>";
    private static final String PATTERN_NAME_PACKAGE = "<app\\s+name=\"([^\"]+)\"\\s+package=\"([^\"]+)\"\\s*\\/>";
    
    public static final String APPLICATION_DIR = "MyAppList";

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
        if (dir == null && context != null) {
            return context.getString(R.string.error_creating_dir, APPLICATION_DIR);
        }
        
        File file = new File(dir, fileName);
        return writeFile(context, appList, file);
    }
    
    public static String writeFile(Context context, List<AppInfo> appList, File file) {
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
            if (context != null) {
                return context.getString(R.string.error_creating_file, file.getAbsolutePath());
            } else {
                return "";
            }
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
    
    public static List<AppInfo> fixFile(File from, File to) {
        BufferedReader br = null;
        try {
            // Read file
            br = new BufferedReader(new FileReader(from));
            String line = null;
            StringBuilder buffer = new StringBuilder();
            while ((line = br.readLine()) != null) {                
                buffer.append(line);
            }
            
            // Extract appinfo
            List<AppInfo> lst = new ArrayList<AppInfo>();
            readAppInfo(lst, PATTERN_NAME_PACKAGE, buffer);
            readAppInfo(lst, PATTERN_PACKAGE_NAME, buffer);
            
            // Write file
            writeFile(null, lst, to);
            
            return lst;
        } catch (IOException e) {
            
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // No problem
            }
        }
        return null;
    }
    
    private static void readAppInfo(List<AppInfo> lst, String patternAppInfo, CharSequence text) {
        Pattern pattern = Pattern.compile(patternAppInfo);
        Matcher matcher = pattern.matcher(text);
        AppInfo appInfo;
        while (matcher.find()) {
            appInfo = new AppInfo();
            appInfo.setName(matcher.group(1));
            appInfo.setPackageName(matcher.group(2));
            lst.add(appInfo);
        }
    }
    
    static class XMLFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(".xml");
        }
        
    }

}
