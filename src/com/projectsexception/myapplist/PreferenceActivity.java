package com.projectsexception.myapplist;

import net.saik0.android.unifiedpreference.UnifiedPreferenceFragment;
import net.saik0.android.unifiedpreference.UnifiedSherlockPreferenceActivity;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;

import com.projectsexception.myapplist.view.ThemeManager;

public class PreferenceActivity extends UnifiedSherlockPreferenceActivity implements Preference.OnPreferenceChangeListener {
    
    private static final String KEY_EMAIL = "mail";
    public static final String KEY_THEME = "theme";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeManager.getTheme(this));
        setHeaderRes(R.xml.preferences);
        super.onCreate(savedInstanceState);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (isSinglePane()) {
            findPreference(KEY_EMAIL).setOnPreferenceClickListener(clickListener);
            findPreference(KEY_THEME).setOnPreferenceChangeListener(this);
        }
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ConfigurationFragment extends UnifiedPreferenceFragment {
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            findPreference(KEY_THEME).setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) getActivity());
        }
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutFragment extends UnifiedPreferenceFragment {
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            findPreference(KEY_EMAIL).setOnPreferenceClickListener(clickListener);
        }
    }
    
    private static Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {        
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (KEY_EMAIL.equals(preference.getKey())) {
                final Context ctx = preference.getContext();
                Intent intent = new Intent(Intent.ACTION_SEND);            
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { ctx.getString(R.string.about_email) });     
                intent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.about_subject));     
                intent.putExtra(Intent.EXTRA_TEXT, "");
                ctx.startActivity(intent);
                return true;
            }
            return false;
        }
    };

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_THEME.equals(preference.getKey())) {
            ThemeManager.changeTheme((String) newValue);
            ThemeManager.restartActivity(this);
        }
        return true;
    }

}
