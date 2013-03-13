package com.projectsexception.myapplist.work;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.xml.FileUtil;

public class ShareAppSaveTask extends AsyncTask<Void, Void, File> {

    private Context context;
    private List<AppInfo> appList;
    private int format;

    public ShareAppSaveTask(Context context, List<AppInfo> appList, int format) {
        this.context = context;
        this.appList = appList;
        this.format = format;
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(context, R.string.export_init, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected File doInBackground(Void... params) {        
        if (appList != null && !appList.isEmpty()) {
            return FileUtil.writeShareFile(context, appList, format);
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(File result) {
        if (result == null) {
            Toast.makeText(context, R.string.share_file_failed, Toast.LENGTH_LONG).show();
        } else {            
            // Share fileName
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            if (format == FileUtil.FILE_HTML) {
                intent.setType("text/html");                
            } else {
                intent.setType("text/plain");
            }
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
                    context.getString(R.string.share_text_subject)); 
            intent.putExtra(android.content.Intent.EXTRA_TEXT, 
                    context.getString(R.string.share_file_text)); 
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ result.getAbsolutePath()));
            try {
                context.startActivity(intent);                    
            } catch (Exception e) {
                CustomLog.error("ShareAppSaveTask", e);
                Toast.makeText(context, context.getString(R.string.share_file_send_failed, result.getAbsolutePath()), Toast.LENGTH_SHORT).show();                    
            }
        }
    }

}