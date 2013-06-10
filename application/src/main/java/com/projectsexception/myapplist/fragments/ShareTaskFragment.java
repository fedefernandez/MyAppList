package com.projectsexception.myapplist.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.projectsexception.myapplist.ShareActivity;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.work.ShareAppSaveTask;
import com.projectsexception.myapplist.xml.FileUtil;

import java.io.File;
import java.util.List;

public class ShareTaskFragment extends SherlockFragment implements ShareAppSaveTask.Listener {
    
    public static interface CallBack {
        void saveFinished(int section, File result);
    }

    private CallBack mCallBack;
    private ShareAppSaveTask mTask;
    private File mFileResult;
    private int mSection;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CallBack) {
            mCallBack = (CallBack) activity;
        } else {
            throw new IllegalStateException(activity.getClass().getName() + " must implement "
                    + CallBack.class.getName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBack = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void saveFinished(File result) {
        mFileResult = result;
        if (mCallBack != null) {
            mCallBack.saveFinished(mSection, mFileResult);
            mFileResult = null;
        }
    }
    
    public boolean startTask(int section, List<AppInfo> appInfoList) {
        boolean started = false;
        if (mTask == null || mTask.getStatus() != AsyncTask.Status.RUNNING || mTask.isCancelled()) {
            if (appInfoList != null) {
                mSection = section;
                mTask = new ShareAppSaveTask(getSherlockActivity(), this);
                int format = (section == ShareActivity.SECTION_HTML_FILE) ? FileUtil.FILE_HTML : FileUtil.FILE_TEXT;
                ShareAppSaveTask.Data data = new ShareAppSaveTask.Data(appInfoList, format);
                mTask.execute(data);
            }
            started = true;
        }
        return started;        
    }

    public void checkPending() {
        if (mFileResult != null) {
            saveFinished(mFileResult);
        }
    }

}
