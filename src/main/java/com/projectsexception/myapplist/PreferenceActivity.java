package com.projectsexception.myapplist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.util.BackupReceiver;
import com.projectsexception.myapplist.view.ThemeManager;
import com.projectsexception.myapplist.work.SaveListService;

public class PreferenceActivity extends SherlockPreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    
    private static final String KEY_EMAIL = "mail";
    public static final String KEY_THEME = "theme";
    public static final String KEY_BACKUP_CHECK = "backup_check";
    public static final String KEY_BACKUP_IGNORED_APPS = "backup_ignored";
    
    private Preference mBackupIgnoredPreference;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeManager.getTheme(this));
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        findPreference(KEY_EMAIL).setOnPreferenceClickListener(this);
        findPreference(KEY_THEME).setOnPreferenceChangeListener(this);
        
        CheckBoxPreference check = (CheckBoxPreference) findPreference(KEY_BACKUP_CHECK);
        check.setOnPreferenceChangeListener(this);
        
        mBackupIgnoredPreference = findPreference(KEY_BACKUP_IGNORED_APPS);
        mBackupIgnoredPreference.setOnPreferenceClickListener(this);
        mBackupIgnoredPreference.setEnabled(check.isChecked());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // This is called when the Home (Up) button is pressed
            // in the Action Bar.
            Intent parentActivityIntent = new Intent(this, ListActivity.class);
            parentActivityIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_THEME.equals(preference.getKey())) {
            ThemeManager.changeTheme((String) newValue);
            ThemeManager.restartActivity(this);
        } else if (KEY_BACKUP_CHECK.equals(preference.getKey())) {
            boolean backup = (Boolean) newValue;
            mBackupIgnoredPreference.setEnabled(backup);
            if (backup) {
                BackupReceiver.enableReceiver(this); 
                final Context ctx = this;
                AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
                dialog.setTitle(R.string.backup_dialog_title);
                dialog.setMessage(R.string.backup_dialog_message);
                dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ctx.startService(new Intent(ctx, SaveListService.class));
                    }
                });
                dialog.setNegativeButton(android.R.string.cancel, null);
                dialog.show();
            } else {
                BackupReceiver.disableReceiver(this);
                SaveListService.cancelService(this);
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final Context ctx = preference.getContext();
        if (KEY_EMAIL.equals(preference.getKey())) {
            Intent intent = new Intent(Intent.ACTION_SEND);            
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { ctx.getString(R.string.about_email) });     
            intent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.about_subject));     
            intent.putExtra(Intent.EXTRA_TEXT, "");
            ctx.startActivity(intent);
            return true;
        } else if (KEY_BACKUP_IGNORED_APPS.equals(preference.getKey())) {
            ctx.startActivity(new Intent(ctx, ListIgnoredActivity.class));
        }
        return false;
    }

}
