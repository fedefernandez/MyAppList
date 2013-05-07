package com.projectsexception.myapplist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.fragments.*;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.work.AppSaveTask;
import com.projectsexception.myapplist.xml.FileUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements
        AppListFragment.CallBack,
        FileListFragment.CallBack,
        AppInfoFragment.CallBack,
        FileDialogFragment.CallBack,
        AppSaveTask.Listener, FragmentManager.OnBackStackChangedListener {

    public static final String ARG_FILE = "fileName";
    private static final String ARG_DISPLAY_OPT = "display_options";

    private static final int MAX_EXECUTIONS = 50;

    private List<AppInfo> mAppList;
    private String mFileStream;
    private MenuItem mMeunLoad;
    private MenuItem mMeunSettings;
    private boolean mDualPane;

    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);
        
        setContentView(R.layout.activity_list);
        checkRateApp();
        
        String fileName = getIntent().getStringExtra(ARG_FILE);

        View detailsFrame = findViewById(R.id.app_info);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        
        if (fileName == null) {
            if (getIntent().getData() != null) {
                fileName = getIntent().getDataString();
            }
            if (fileName != null && fileName.startsWith("content://")) {
                // We have a stream file, the user must save the file before continue
                mFileStream = fileName;
                new FileDialogFragment().show(getSupportFragmentManager(), "file_dialog");
            } else {
                // Want installed applications
                loadAppListFragment();
            }
        } else {
            // Load file
            loadFileListFragment(fileName, false);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_DISPLAY_OPT, getSupportActionBar().getDisplayOptions());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int savedDisplayOpt = savedInstanceState.getInt(ARG_DISPLAY_OPT);
        if(savedDisplayOpt != 0){
            getSupportActionBar().setDisplayOptions(savedDisplayOpt);
        }
    }

    private void checkRateApp() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int numExecutions = sp.getInt(RateAppDialogFragment.NUM_EXECUTIONS, 0);
        if (numExecutions >= MAX_EXECUTIONS) {
            new RateAppDialogFragment().show(getSupportFragmentManager(), "rate_app_dialog");
        } else if (numExecutions >= 0) {
            sp.edit().putInt(RateAppDialogFragment.NUM_EXECUTIONS, numExecutions + 1).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_list, menu);
        mMeunLoad = menu.findItem(R.id.menu_load_file);
        mMeunSettings = menu.findItem(R.id.menu_settings);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.app_list);
        boolean visible = (fragment instanceof AppListFragment);
        mMeunLoad.setVisible(visible);
        mMeunSettings.setVisible(visible);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDualPane) {
                loadAppListFragment();
            } else {
                FragmentManager fm = getSupportFragmentManager();
                int count = fm.getBackStackEntryCount();
                for (int i = 0 ; i < count ; i++) {
                    fm.popBackStack();
                }
            }
            return true;
        } else if (item.getItemId() == R.id.menu_load_file) {
            new FileListTask(this).execute();
            return true;
        } else if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, PreferenceActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * ----------------------------
     * AppListFragment.CallBack and FileListFragment.CallBack method
     * ----------------------------
     */

    @Override
    public void showAppInfo(String name, String packageName) {
        FragmentManager fm = getSupportFragmentManager();

        AppInfoFragment infoFragment = null;
        int frameId;
        if (mDualPane) {
            frameId = R.id.app_info;
            // Check what fragment is shown, replace if needed.
            infoFragment = (AppInfoFragment) fm.findFragmentById(R.id.app_info);
        } else {
            frameId = R.id.app_list;
            Fragment frg = fm.findFragmentById(R.id.app_list);
            if (frg instanceof AppInfoFragment) {
                infoFragment = (AppInfoFragment) frg;
            }
        }

        if (infoFragment == null
                || infoFragment.getShownPackage() == null
                || !infoFragment.getShownPackage().equals(packageName)) {
            // Make new fragment to show this selection.
            infoFragment = AppInfoFragment.newInstance(name, packageName);

            // Execute a transaction, replacing any existing
            // fragment with this one inside the frame.
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(frameId, infoFragment);
            if (frameId == R.id.app_list) {
                ft.addToBackStack("app_info");
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }

    /*
     * ----------------------------
     * AppListFragment.CallBack methods
     * ----------------------------
     */

    @Override
    public void saveAppList(List<AppInfo> appList) {
        mAppList = appList;
        new FileDialogFragment().show(getSupportFragmentManager(), "file_dialog");
    }

    @Override
    public void shareAppList(ArrayList<AppInfo> appList) {
        shareAppList(null, appList);
    }

    /*
     * ----------------------------
     * FileListFragment.CallBack methods
     * ----------------------------
     */

    @Override
    public void updateAppList(String fileName, List<AppInfo> appList) {
        mAppList = appList;
        new AppSaveTask(MainActivity.this, null, mAppList).execute(fileName, Boolean.toString(true));
    }

    @Override
    public void shareAppList(String filePath, ArrayList<AppInfo> appList) {
        Intent intent = new Intent(this, ShareActivity.class);
        intent.putExtra(ShareActivity.FILE_PATH, filePath);
        intent.putParcelableArrayListExtra(ShareActivity.APP_LIST, appList);
        startActivity(intent);
    }

    @Override
    public void installAppList(ArrayList<AppInfo> appList) {
        Intent intent = new Intent(this, ListInstallActivity.class);
        intent.putParcelableArrayListExtra(ListInstallActivity.ARG_APP_INFO_LIST, appList);
        startActivity(intent);
    }

    /*
     * ----------------------------
     * AppInfoFragment.CallBack method
     * ----------------------------
     */

    @Override
    public void removeAppInfoFragment() {
        FragmentManager fm = getSupportFragmentManager();
        if (mDualPane) {
            AppInfoFragment infoFragment = (AppInfoFragment) fm.findFragmentById(R.id.app_info);
            if (infoFragment != null) {
                fm.beginTransaction().remove(infoFragment).commit();
            }
        } else {
            fm.popBackStack();
        }
    }

    /*
     * ----------------------------
     * FileDialogFragment.CallBack method
     * ----------------------------
     */

    @Override
    public void nameAccepted(String name) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.empty_name_error, Toast.LENGTH_SHORT).show();
        } else if (mAppList != null) {
            new AppSaveTask(MainActivity.this, null, mAppList).execute(name);
        } else if (mFileStream != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(Uri.parse(mFileStream));
                new AppSaveTask(MainActivity.this, inputStream, null).execute(name);
            } catch (FileNotFoundException e) {
                CustomLog.error("MainActivity", e);
            }
        }

        mAppList = null;
        mFileStream = null;
    }

    /*
     * ----------------------------
     * AppSaveTask.Listener methods
     * ----------------------------
     */

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void saveFinished(String fileName, String errorMsg, int operation) {
         if (errorMsg == null) {
             if (operation == AppSaveTask.OP_SAVE_STREAM) {
                 loadFileListFragment(fileName, false);
             } else if (operation == AppSaveTask.OP_SAVE_LIST) {
                 Toast.makeText(this, R.string.export_successfully_update, Toast.LENGTH_SHORT).show();
             } else {
                 Toast.makeText(this, R.string.export_successfully, Toast.LENGTH_SHORT).show();
             }
         } else {
             Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
         }
    }

    void loadAppListFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.app_list);
        if (fragment instanceof AppListFragment) {
            ((AppListFragment) fragment).reloadApplications();
        } else {
            fm.beginTransaction().replace(R.id.app_list, new AppListFragment()).commit();
        }
    }

    void loadFileListFragment(String fileName, boolean addToBack) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.app_list);
        if (fragment instanceof FileListFragment) {
            ((FileListFragment) fragment).reloadFile(fileName);
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.app_list, FileListFragment.newInstance(fileName));
            if (addToBack) {
                ft.addToBackStack("file_list");
            }
            ft.commit();
        }
    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
    }

    static class FileListTask extends AsyncTask<Void, Void, String[]> {

        private MainActivity listActivity;

        public FileListTask(MainActivity listActivity) {
            this.listActivity = listActivity;
        }

        @Override
        protected String[] doInBackground(Void... params) {
            return FileUtil.loadFiles();
        }

        @Override
        protected void onPostExecute(final String[] result) {
            if (result == null || result.length == 0) {
                Toast.makeText(listActivity, R.string.main_no_files, Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(listActivity);
                builder.setTitle(R.string.main_select_files);
                builder.setItems(result, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = result[which];
                        listActivity.loadFileListFragment(fileName, true);
                    }
                });
                builder.create().show();
            }
        }

    }

}
