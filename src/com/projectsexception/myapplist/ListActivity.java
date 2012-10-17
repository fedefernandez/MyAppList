package com.projectsexception.myapplist;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.projectsexception.myapplist.fragments.AppListFragment;
import com.projectsexception.myapplist.fragments.FileListFragment;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.util.NewFileDialog;
import com.projectsexception.myapplist.xml.FileUtil;

public class ListActivity extends SherlockFragmentActivity {

    public static final String ARG_FILE = "fileName";
    
    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);
        
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE|ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_HOME_AS_UP);
        
        String fileName = getIntent().getStringExtra(ARG_FILE);
        boolean dialog = false;
        if (fileName == null && getIntent().getData() != null) {
            fileName = getIntent().getDataString();
            if (fileName.startsWith("content://")) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(Uri.parse(fileName));
                    dialog = true;
                    createNewFileDialog(inputStream);
                } catch (FileNotFoundException e) {
                    CustomLog.error("ListActivity", e);
                }
            }
        }
        
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment;
        if (fileName == null) {
            // Want installed applications
            ab.setTitle(R.string.ab_title_app_list);
            fragment = new AppListFragment();
            if (fm.findFragmentById(android.R.id.content) == null) {
                fm.beginTransaction().add(android.R.id.content, fragment).commit();
            }
        } else if (!dialog) {
            ab.setTitle(R.string.ab_title_file_list);
            Bundle fragmentArgs = new Bundle();
            fragmentArgs.putString(ARG_FILE, fileName);
            fragment = new FileListFragment();
            fragment.setArguments(fragmentArgs);
            if (fm.findFragmentById(android.R.id.content) == null) {
                fm.beginTransaction().add(android.R.id.content, fragment).commit();
            }
        }
    }
    
    private void createNewFileDialog(final InputStream stream) {
        final Context context = this;
        NewFileDialog.showDialog(context, new NewFileDialog.Listener() {            
            @Override
            public void nameAccepted(String name) {
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(context, R.string.empty_name_error, Toast.LENGTH_SHORT).show();
                } else {
                    new SaveFileTask(name).execute(stream);
                }
            }
        });
    }
    
    class SaveFileTask extends AsyncTask<InputStream, Void, String> {
        
        private String fileName;
        
        public SaveFileTask(String name) {
            this.fileName = name;
        }

        @Override
        protected String doInBackground(InputStream... params) {
            return FileUtil.writeInputStreamFile(ListActivity.this, params[0], fileName);
        }
        
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            final Context context = ListActivity.this;
            if (context != null) {
                if (result == null) {
                    Bundle fragmentArgs = new Bundle();
                    fragmentArgs.putString(ARG_FILE, fileName);
                    FileListFragment fragment = new FileListFragment();
                    fragment.setArguments(fragmentArgs);
                    FragmentManager fm = getSupportFragmentManager();
                    // Create the list fragment and add it as our sole content.
                    if (fm.findFragmentById(android.R.id.content) == null) {
                        fm.beginTransaction().add(android.R.id.content, fragment).commit();
                    }
                } else {
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                }
            }
        }
        
    }

}
