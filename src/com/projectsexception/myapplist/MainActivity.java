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
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.xml.FileUtil;

public class MainActivity extends BaseActivity implements DialogInterface.OnClickListener {
    
    private static final String NUM_EXECUTIONS = "num_executions";
    private static final int MAX_EXECUTIONS = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkRateApp();
    }
    
    private void checkRateApp() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int numExecutions = sp.getInt(NUM_EXECUTIONS, 0);
        if (numExecutions >= MAX_EXECUTIONS) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.rate_dialog_title);
            dialog.setMessage(R.string.rate_dialog_msg);
            dialog.setCancelable(false);
            dialog.setNegativeButton(R.string.rate_dialog_negative, this);
            dialog.setNeutralButton(R.string.rate_dialog_neutral, this);
            dialog.setPositiveButton(R.string.rate_dialog_positive, this);
            dialog.show();
        } else if (numExecutions >= 0) {
            sp.edit().putInt(NUM_EXECUTIONS, numExecutions + 1).commit();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        int newValue = -1;
        if (DialogInterface.BUTTON_NEUTRAL == which) {
            newValue = 0;
        } else if (DialogInterface.BUTTON_POSITIVE == which) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } catch (Exception e) {
                CustomLog.error("MainActivity.Dialog", e);
            }
        }        
        editor.putInt(NUM_EXECUTIONS, newValue).commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(R.string.menu_settings);
        item.setIcon(R.drawable.ic_menu_manage);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, PreferenceActivity.class));
        return true;
    }
    
    public void loadList(View view) {
        new FileListTask(this).execute(new Void[0]);
    }
    
    public void newList(View view) {
        startActivity(new Intent(this, ListActivity.class));
    }
    
    static class FileListTask extends AsyncTask<Void, Void, String[]> {
        
        private Context context;
        
        public FileListTask(Context context) {
            this.context = context;
        }

        @Override
        protected String[] doInBackground(Void... params) {
            return FileUtil.loadFiles();
        }
        
        @Override
        protected void onPostExecute(final String[] result) {
            if (result == null || result.length == 0) {
                Toast.makeText(context, R.string.main_no_files, Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.main_select_files);
                builder.setItems(result, new DialogInterface.OnClickListener() {                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = result[which];
                        Intent intent = new Intent(context, ListActivity.class);
                        intent.putExtra(ListActivity.ARG_FILE, fileName);
                        context.startActivity(intent);
                    }
                });
                builder.create().show();
            }
        }
        
    }
    
}