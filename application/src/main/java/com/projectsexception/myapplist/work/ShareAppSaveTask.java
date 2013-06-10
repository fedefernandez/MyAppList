package com.projectsexception.myapplist.work;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.xml.FileUtil;

public class ShareAppSaveTask extends AsyncTask<ShareAppSaveTask.Data, Void, File> {

    private Context mContext;
    private Listener mListener;
    
    public static class Data {
        public Data(List<AppInfo> appList, int format) {
            this.appList = appList;
            this.format = format;
        }
        List<AppInfo> appList;
        int format;        
    }
    
    public static interface Listener { 
        void saveFinished(File result);
    }

    public ShareAppSaveTask(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected File doInBackground(ShareAppSaveTask.Data... params) {        
        if (params.length > 0 && params[0] != null) {
            return FileUtil.writeShareFile(mContext, params[0].appList, params[0].format);
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(File result) {
        if (mListener != null) {
            mListener.saveFinished(result);
        }
    }

}