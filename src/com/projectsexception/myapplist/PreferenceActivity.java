package com.projectsexception.myapplist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.projectsexception.myapplist.view.ThemeManager;
import com.projectsexception.myapplist.work.SaveListService;

public class PreferenceActivity extends SherlockPreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    
    private static final String KEY_EMAIL = "mail";
    public static final String KEY_THEME = "theme";
    public static final String KEY_BACKUP_CHECK = "backup_check";
    public static final String KEY_BACKUP_PERIOD = "backup_period";
    public static final String KEY_BACKUP_IGNORED_APPS = "backup_ignored";
    
    private ListPreference mBackupPeriodPreference;
    private Preference mBackupIgnoredPreference;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeManager.getTheme(this));
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        findPreference(KEY_EMAIL).setOnPreferenceClickListener(this);
        findPreference(KEY_THEME).setOnPreferenceChangeListener(this);
        
        CheckBoxPreference check = (CheckBoxPreference) findPreference(KEY_BACKUP_CHECK);
        check.setOnPreferenceChangeListener(this);
        
        mBackupPeriodPreference = (ListPreference) findPreference(KEY_BACKUP_PERIOD);
        mBackupPeriodPreference.setOnPreferenceChangeListener(this);
        mBackupPeriodPreference.setEnabled(check.isChecked());
        
        mBackupIgnoredPreference = findPreference(KEY_BACKUP_IGNORED_APPS);
        mBackupIgnoredPreference.setOnPreferenceClickListener(this);
        mBackupIgnoredPreference.setEnabled(check.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_THEME.equals(preference.getKey())) {
            ThemeManager.changeTheme((String) newValue);
            ThemeManager.restartActivity(this);
        } else if (KEY_BACKUP_CHECK.equals(preference.getKey())) {
            boolean backup = (Boolean) newValue;
            mBackupPeriodPreference.setEnabled(backup);
            mBackupIgnoredPreference.setEnabled(backup);
            SaveListService.updateService(this, backup, mBackupPeriodPreference.getValue());
        } else if (KEY_BACKUP_PERIOD.equals(preference.getKey())) {
            SaveListService.updateService(this, true, mBackupPeriodPreference.getValue());
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
