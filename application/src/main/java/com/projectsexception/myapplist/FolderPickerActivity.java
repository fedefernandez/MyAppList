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

        FragmentManager fm = getSupportFragmentManager();
        Fragment frg = fm.findFragmentById(android.R.id.content);
        if (frg == null) {
            File file = FileUtil.prepareApplicationDir(this, true);
            if (file == null) {
                cancel();
            } else {
                frg = FolderPickerFragment.newInstance(file.getAbsolutePath());
                fm.beginTransaction().add(android.R.id.content, frg).commit();
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
