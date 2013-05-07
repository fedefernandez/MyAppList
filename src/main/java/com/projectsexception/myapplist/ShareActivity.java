package com.projectsexception.myapplist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.projectsexception.myapplist.fragments.ShareTaskFragment;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.CustomLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShareActivity extends BaseActivity implements ShareTaskFragment.CallBack, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    private static final int[] OPTIONS_IDS = {
            R.id.share_xml,
            R.id.share_text,
            R.id.share_text_file,
            R.id.share_html,
            R.id.share_html_file,
    };

    private static final int[] OPTIONS = {
            R.string.share_xml,
            R.string.share_text,
            R.string.share_text_file,
            R.string.share_html,
            R.string.share_html_file,
    };

    private static final int[] OPTIONS_MSG = {
            R.string.share_xml_message,
            R.string.share_text_message,
            R.string.share_text_file_message,
            R.string.share_html_message,
            R.string.share_html_file_message,
    };
    
    public static final int SECTION_XML = 0;
    public static final int SECTION_TEXT = 1;
    public static final int SECTION_TEXT_FILE = 2;
    public static final int SECTION_HTML = 3;
    public static final int SECTION_HTML_FILE = 4;

    public static final String APP_LIST = "app_list";
    public static final String FILE_PATH = "file_path";

    /**
     * Store the selected value
     */
    int mSelection;

    /**
     * Store the start index. 0 if XML available 1 in other case
     */
    int mStartIndex;
    
    /**
     * Store the XML file if exists
     */
    File mFile;
    
    /**
     * Store the selected apps to share
     */
    ArrayList<AppInfo> mAppList;

    /**
     * Store the message textView
     */
    TextView mTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.ab_title_share);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        // If we receive the file path, we can share xml
        String filePath = getIntent().getStringExtra(FILE_PATH);
        if (filePath != null) {
            mFile = new File(filePath);
        }
        mAppList = getIntent().getParcelableArrayListExtra(APP_LIST);

        final List<String> itemList = new ArrayList<String>();
        mStartIndex = (filePath == null) ? 1 : 0;
        for (int i = mStartIndex ; i < OPTIONS.length ; i++) {
            itemList.add(getString(OPTIONS[i]));
        }

        if (savedInstanceState != null) {
            mSelection = savedInstanceState.getInt("selection", 0);
        }

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        if (spinner != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, com.actionbarsherlock.R.layout.sherlock_spinner_item, itemList);
            adapter.setDropDownViewResource(com.actionbarsherlock.R.layout.sherlock_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(mSelection - mStartIndex);
            spinner.setOnItemSelectedListener(this);
        } else {
            RadioGroup radio = (RadioGroup) findViewById(R.id.radio);
            if (radio != null) {
                if (mStartIndex > 0) {
                    radio.removeViewAt(0);
                }
                radio.check(OPTIONS_IDS[mSelection]);
                radio.setOnCheckedChangeListener(this);
            }
        }

        mTextView = (TextView) findViewById(android.R.id.text1);
        mTextView.setText(OPTIONS_MSG[mSelection]);
        
        FragmentManager fm = getSupportFragmentManager();
        ShareTaskFragment frg = (ShareTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (frg == null) {
            frg = new ShareTaskFragment();
            fm.beginTransaction().add(frg, TAG_TASK_FRAGMENT).commit();
        } else {
            frg.checkPending();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selection", mSelection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_share, menu);
        return true;
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
        } else if (item.getItemId() == R.id.menu_share) {
            if (mSelection == SECTION_XML) {
                if (mFile == null) {
                    Toast.makeText(this, R.string.share_xml_error, Toast.LENGTH_SHORT).show();
                } else {
                    saveFinished(SECTION_XML, mFile);
                }
            } else if (mSelection == SECTION_TEXT || mSelection == SECTION_HTML) {
                // Always add footer
                shareAppListText(mSelection, true);
            } else if (mSelection == SECTION_TEXT_FILE || mSelection == SECTION_HTML_FILE) {
                FragmentManager fm = getSupportFragmentManager();
                ShareTaskFragment taskFragment = (ShareTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
                if (taskFragment != null) {
                    taskFragment.startTask(mSelection, mAppList);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void saveFinished(int section, File file) {
        if (file != null && file.exists()) {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            if (section == SECTION_XML) {
                intent.setType("text/xml");                
            } else if (section == SECTION_HTML) {
                intent.setType("text/html");                
            } else {
                intent.setType("text/plain");
            }
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
                    getResources().getQuantityString(R.plurals.share_title, 2)); 
            intent.putExtra(android.content.Intent.EXTRA_TEXT, 
                    getString(R.string.share_file_text, getPackageName())); 
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ file.getAbsolutePath()));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.share_chooser)));
            } catch (Exception e) {
                CustomLog.error("ShareActivity", e);
                Toast.makeText(this, getString(R.string.share_file_send_failed, file.getAbsolutePath()), Toast.LENGTH_LONG).show();                    
            }
            finish();
        } else {
            Toast.makeText(this, getString(R.string.share_file_failed), Toast.LENGTH_LONG).show();
        }
    }
    
    private void shareAppListText(int section, boolean footer) {
        if (mAppList != null && !mAppList.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, 
                    getResources().getQuantityString(R.plurals.share_title, mAppList.size()));
            final CharSequence text;
            if (section == SECTION_HTML) {
                intent.setType("text/html");
                text = Html.fromHtml(AppUtil.appInfoToHTML(this, mAppList, footer));
            } else {
                intent.setType("text/plain");                    
                text = AppUtil.appInfoToText(this, mAppList, footer);
            }
            intent.putExtra(Intent.EXTRA_TEXT, text);
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.share_chooser)));                
            } catch (Exception e) {
                CustomLog.error("ShareActivity", e);
                Toast.makeText(this, getString(R.string.share_text_send_failed), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0 ; i < OPTIONS_IDS.length ; i++) {
            if (OPTIONS_IDS[i] == checkedId) {
                mSelection = i;
                mTextView.setText(OPTIONS_MSG[mSelection]);
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSelection = position + mStartIndex;
        mTextView.setText(OPTIONS_MSG[mSelection]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
