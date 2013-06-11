package com.projectsexception.myapplist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import android.text.TextUtils;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.util.BackupReceiver;
import com.projectsexception.myapplist.view.ThemeManager;
import com.projectsexception.myapplist.work.SaveListService;
import com.projectsexception.myapplist.xml.FileUtil;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.io.File;
import java.util.List;

public class PreferenceActivity extends SherlockPreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    
    private static final String KEY_EMAIL = "mail";
    public static final String KEY_HIDE_SYSTEM_APPS = "hide_system_apps";
    public static final String KEY_THEME = "theme";
    public static final String KEY_SDCARD = "sdcard";
    public static final String KEY_BACKUP_CHECK = "backup_check";
    public static final String KEY_BACKUP_IGNORED_APPS = "backup_ignored";
    public static final String KEY_BACKUP_UNINSTALLED_APPS = "backup_uninstalled";

    private ListPreference mThemePreference;
    private ListPreference mSdcardPreference;
    private Preference mBackupIgnoredPreference;
    private Preference mBackupInstalledPreference;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeManager.getTheme(this));
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mThemePreference = (ListPreference) findPreference(KEY_THEME);
        setThemePreferenceSummary(mThemePreference.getValue());
        
        findPreference(KEY_EMAIL).setOnPreferenceClickListener(this);
        findPreference(KEY_THEME).setOnPreferenceChangeListener(this);
        
        CheckBoxPreference check = (CheckBoxPreference) findPreference(KEY_BACKUP_CHECK);
        check.setOnPreferenceChangeListener(this);

        mSdcardPreference = (ListPreference) findPreference(KEY_SDCARD);
        String value = mSdcardPreference.getValue();
        if (TextUtils.isEmpty(value)) {
            File folder = FileUtil.prepareApplicationDir(this, false);
            if (folder == null) {
                mSdcardPreference.setSummary(R.string.configuration_sdcard_no_access);
            } else {
                mSdcardPreference.setSummary(folder.getAbsolutePath());
                mSdcardPreference.setValue(folder.getAbsolutePath());
            }
        } else {
            File file = new File(value);
            if (file.canWrite()) {
                mSdcardPreference.setSummary(value);
            } else {
                mSdcardPreference.setSummary(R.string.configuration_sdcard_no_access);
            }
        }
        List<CharSequence> folderList = FileUtil.getSdcardFolders();
        CharSequence[] array = folderList.toArray(new CharSequence[folderList.size()]);
        mSdcardPreference.setEntries(array);
        mSdcardPreference.setEntryValues(array);
        mSdcardPreference.setOnPreferenceChangeListener(this);

        mBackupIgnoredPreference = findPreference(KEY_BACKUP_IGNORED_APPS);
        mBackupIgnoredPreference.setOnPreferenceClickListener(this);
        mBackupIgnoredPreference.setEnabled(check.isChecked());

        mBackupInstalledPreference = findPreference(KEY_BACKUP_UNINSTALLED_APPS);
        mBackupInstalledPreference.setEnabled(check.isChecked());
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
            ThemeManager.restartActivity(this);
            setThemePreferenceSummary((String) newValue);
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
        } else if (KEY_SDCARD.equals(preference.getKey())) {
            mSdcardPreference.setSummary((String) newValue);
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
        }
        return false;
    }

    void setThemePreferenceSummary(String themeValue) {
        int value;
        try {
            value = Integer.parseInt(themeValue);
        } catch (Exception e) {
            value = 0;
        }
        if (value == 0) {
            mThemePreference.setSummary(R.string.theme_dark);
        } else {
            mThemePreference.setSummary(R.string.theme_light);
        }
    }

}
