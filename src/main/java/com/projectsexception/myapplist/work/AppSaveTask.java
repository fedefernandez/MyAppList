package com.projectsexception.myapplist.work;

import android.content.Context;
import android.os.AsyncTask;

import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.xml.FileUtil;

import java.io.InputStream;
import java.util.List;

public class AppSaveTask extends AsyncTask<String, Void, String> {

    public static final int OP_SAVE_LIST = 0;
    public static final int OP_UPDATE_LIST = 1;
    public static final int OP_SAVE_STREAM = 2;


    public static interface Listener {
        Context getContext();
        void saveFinished(String fileName, String errorMsg, int operation);
    }

    private Listener listener;
    private String fileName;
    private InputStream fileStream;
    private List<AppInfo> appList;
    private boolean updating;

    public AppSaveTask(Listener listener, InputStream fileStream, List<AppInfo> appList) {
        this.listener = listener;
        this.fileStream = fileStream;
        this.appList = appList;
    }

    @Override
    protected String doInBackground(String... params) {
        if (listener != null && params.length > 0) {
            fileName = params[0];
            if (appList != null && !appList.isEmpty()) {
                updating = params.length > 1 && params[1] != null && Boolean.parseBoolean(params[1]);
                return FileUtil.writeFile(listener.getContext(), appList, fileName);
            } else if (fileStream != null) {
                return FileUtil.writeInputStreamFile(listener.getContext(), fileStream, fileName);
            } else {
                return listener.getContext().getString(R.string.empty_list_post_error);
            }
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        if (listener != null) {
            int operation;
            if (fileStream == null) {
                operation = updating ? OP_UPDATE_LIST : OP_SAVE_LIST;
            } else {
                operation = OP_SAVE_STREAM;
            }
            if (result != null) {
                int error;
                if (updating) {
                   error = R.string.export_failed_update;
                } else {
                   error = R.string.export_failed;
                }
                result = listener.getContext().getString(error, result);
            }
            listener.saveFinished(fileName, result, operation);
        }
    }

}