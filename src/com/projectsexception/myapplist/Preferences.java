package com.projectsexception.myapplist;

import com.projectsexception.myapplist.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements Preference.OnPreferenceClickListener {
    
    private static final String KEY_EMAIL = "mail";
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);
        getPreferenceManager().findPreference(KEY_EMAIL).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (KEY_EMAIL.equals(preference.getKey())) {
            Intent intent = new Intent(Intent.ACTION_SEND);            
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {getString(R.string.about_email)});     
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_subject));     
            intent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(intent);
        }
        return true;
    }

}
