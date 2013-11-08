package com.projectsexception.myapplist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import android.view.MenuItem;
import com.projectsexception.myapplist.util.BackupReceiver;
import com.projectsexception.myapplist.view.ThemeManager;
import com.projectsexception.myapplist.work.SaveListService;
import com.projectsexception.myapplist.xml.FileUtil;

import java.io.File;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MyAppListPreferenceActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    static final String KEY_EMAIL = "mail";
    public static final String KEY_HIDE_SYSTEM_APPS = "hide_system_apps";
    public static final String KEY_THEME = "theme";
    private static final String KEY_SDCARD = "sdcard";
    public static final String PREF_FOLDER = "pref_folder";
    public static final String KEY_BACKUP_CHECK = "backup_check";
    public static final String KEY_BACKUP_IGNORED_APPS = "backup_ignored";
    public static final String KEY_BACKUP_UNINSTALLED_APPS = "backup_uninstalled";
    public static final String KEY_ANIMATIONS = "animations";
    public static final int REQUEST_CODE_FOLDER = 1;

    private ListPreference mThemePreference;
    private Preference mSdcardPreference;
    private Preference mBackupIgnoredPreference;
    private Preference mBackupInstalledPreference;
    private Preference mAnimationsPreference;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeManager.getPreferenceTheme(this));
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mThemePreference = (ListPreference) findPreference(KEY_THEME);
        mThemePreference.setSummary(ThemeManager.getThemeName(mThemePreference.getValue()));
        
        findPreference(KEY_EMAIL).setOnPreferenceClickListener(this);
        findPreference(KEY_THEME).setOnPreferenceChangeListener(this);
        
        CheckBoxPreference check = (CheckBoxPreference) findPreference(KEY_BACKUP_CHECK);
        check.setOnPreferenceChangeListener(this);

        mSdcardPreference = findPreference(KEY_SDCARD);
        File folder = FileUtil.prepareApplicationDir(this, true);
        if (folder == null) {
            mSdcardPreference.setSummary(R.string.configuration_sdcard_no_access);
        } else {
            mSdcardPreference.setSummary(folder.getAbsolutePath());
        }
        mSdcardPreference.setOnPreferenceClickListener(this);

        mBackupIgnoredPreference = findPreference(KEY_BACKUP_IGNORED_APPS);
        mBackupIgnoredPreference.setOnPreferenceClickListener(this);
        mBackupIgnoredPreference.setEnabled(check.isChecked());

        mBackupInstalledPreference = findPreference(KEY_BACKUP_UNINSTALLED_APPS);
        mBackupInstalledPreference.setEnabled(check.isChecked());

        mAnimationsPreference = findPreference(KEY_ANIMATIONS);
        mAnimationsPreference.setEnabled(ThemeManager.isFlavoredTheme(this));
    }

    @Override
    protected void onDestroy() {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // This is called when the Home (Up) button is pressed
            // in the Action Bar.
            Intent parentActivityIntent = new Intent(this, MainActivity.class);
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
            mAnimationsPreference.setEnabled(ThemeManager.isFlavoredTheme(this));
            ThemeManager.restartActivity(this);
            mThemePreference.setSummary(ThemeManager.getThemeName((String) newValue));
        } else if (KEY_BACKUP_CHECK.equals(preference.getKey())) {
            boolean backup = (Boolean) newValue;
            mBackupIgnoredPreference.setEnabled(backup);
            mBackupInstalledPreference.setEnabled(backup);
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
            try {
                ctx.startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                Crouton.makeText(this, R.string.mail_send_failed, Style.ALERT).show();
            }
            return true;
        } else if (KEY_BACKUP_IGNORED_APPS.equals(preference.getKey())) {
            ctx.startActivity(new Intent(ctx, ListIgnoredActivity.class));
        } else if (KEY_SDCARD.equals(preference.getKey())) {
            startActivityForResult(new Intent(this, FolderPickerActivity.class), REQUEST_CODE_FOLDER);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOLDER && resultCode == RESULT_OK) {
            String path = data.getStringExtra(FolderPickerActivity.FOLDER_PATH);
            File folder = new File(path);
            if (folder.canWrite()) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString(MyAppListPreferenceActivity.PREF_FOLDER, path);
                editor.commit();
                mSdcardPreference.setSummary(path);
            }
        }
    }
}
