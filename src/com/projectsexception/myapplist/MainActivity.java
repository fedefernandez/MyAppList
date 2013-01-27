package com.projectsexception.myapplist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.view.ThemeManager;
import com.projectsexception.myapplist.xml.FileUtil;
import com.projectsexception.myapplist.R;

public class MainActivity extends SherlockActivity {
    
    private int mTheme;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mTheme = ThemeManager.getTheme(this);
        setTheme(mTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mTheme != ThemeManager.getTheme(this)) {
            ThemeManager.restartActivity(this);
        }
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