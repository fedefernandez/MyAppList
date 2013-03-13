package com.projectsexception.myapplist.work;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.xml.FileUtil;

public class AppSaveTask extends AsyncTask<Boolean, Void, String> {

    private Context context;
    private String fileName;
    private List<AppInfo> appList;
    private boolean updating;

    public AppSaveTask(Context context, String fileName, List<AppInfo> appList) {
        this.context = context;
        this.fileName = fileName;
        this.appList = appList;
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(context, R.string.export_init, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(Boolean... params) {
        updating = params.length > 0 ? params[0] : false;
        
        if (appList != null && !appList.isEmpty()) {
            return FileUtil.writeFile(context, appList, fileName);
        } else {
            return context.getString(R.string.empty_list_post_error);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        int success;
        int error;
        if (updating) {
            success = R.string.export_successfully_update;                
            error = R.string.export_failed_update;                
        } else {
            success = R.string.export_successfully;
            error = R.string.export_failed;                
        }
        if (result == null) {
            Toast.makeText(context, success, Toast.LENGTH_SHORT).show();
        } else {            
            Toast.makeText(context, context.getString(error, result), Toast.LENGTH_LONG).show();
        }
    }

}