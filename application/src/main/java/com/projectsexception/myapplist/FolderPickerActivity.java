package com.projectsexception.myapplist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.projectsexception.myapplist.fragments.FolderPickerFragment;
import com.projectsexception.myapplist.xml.FileUtil;

import java.io.File;

public class FolderPickerActivity extends BaseActivity implements FolderPickerFragment.CallBack {

    public static final String FOLDER_PATH = "folder_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_picker_folder);

        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            File file = FileUtil.prepareApplicationDir(this, true);
            if (file == null) {
                cancel();
            } else {
                fm.beginTransaction().replace(R.id.content, FolderPickerFragment.newInstance(file.getAbsolutePath())).commit();
            }
        }
    }

    @Override
    public void selectedFolder(File folder) {
        if (folder == null) {
            cancel();
        } else {
            Intent result = new Intent();
            result.putExtra(FOLDER_PATH, folder.getAbsolutePath());
            setResult(RESULT_OK, result);
            finish();
        }
    }

    @Override
    public void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
