package com.projectsexception.myapplist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.view.MenuItem;
import com.projectsexception.myapplist.fragments.ShareTaskFragment;
import com.projectsexception.myapplist.model.AppInfo;
import com.projectsexception.myapplist.util.AppUtil;
import com.projectsexception.myapplist.util.CustomLog;
import com.projectsexception.myapplist.view.ThemeManager;
import com.projectsexception.myapplist.view.TypefaceProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.Optional;
import butterknife.Views;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ShareActivity extends BaseActivity implements ShareTaskFragment.CallBack, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
    
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    private static final int[] OPTIONS = {
            R.string.share_xml,
            R.string.share_text,
            R.string.share_text_file,
            R.string.share_html,
            R.string.share_html_file,
            R.string.share_forum,
    };

    private static final int[] OPTIONS_MSG = {
            R.string.share_xml_message,
            R.string.share_text_message,
            R.string.share_text_file_message,
            R.string.share_html_message,
            R.string.share_html_file_message,
            R.string.share_forum_message,
    };
    
    public static final int SECTION_XML = 0;
    public static final int SECTION_TEXT = 1;
    public static final int SECTION_TEXT_FILE = 2;
    public static final int SECTION_HTML = 3;
    public static final int SECTION_HTML_FILE = 4;
    public static final int SECTION_FORUM = 5;

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
     * Store the title textView
     */
    @InjectView(android.R.id.title) TextView mTitle;

    /**
     * Store the message textView
     */
    @InjectView(android.R.id.text1) TextView mMessage;

    /**
     * Store the share type spinner
     */
    @InjectView(R.id.spinner) Spinner mSpinner;

    /**
     * Store the list with the share types (only in big screens)
     */
    @InjectView(R.id.list)
    @Optional
    ListView mListView;

    /**
     * Store the share button
     */
    @InjectView(android.R.id.button1) ImageButton mShare;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share);
        Views.inject(this);

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

        if (mSelection - mStartIndex < 0) {
            mSelection = 1;
        }

        if (mSpinner != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            mSpinner.setSelection(mSelection - mStartIndex);
            mSpinner.setOnItemSelectedListener(this);
        } else if (mListView != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_option, itemList);
            mListView.setAdapter(adapter);
            mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            mListView.setItemChecked(mSelection - mStartIndex, true);
            mListView.setOnItemClickListener(this);
        }

        mMessage.setText(OPTIONS_MSG[mSelection]);

        if (ThemeManager.isFlavoredTheme(this)) {
            TypefaceProvider.setTypeFace(this, mTitle, TypefaceProvider.FONT_BOLD);
            TypefaceProvider.setTypeFace(this, mMessage, TypefaceProvider.FONT_REGULAR);
        }

        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelection == SECTION_XML) {
                    if (mFile == null) {
                        Crouton.makeText(ShareActivity.this, R.string.share_xml_error, Style.ALERT).show();
                    } else {
                        saveFinished(SECTION_XML, mFile);
                    }
                } else if (mSelection == SECTION_TEXT || mSelection == SECTION_HTML || mSelection == SECTION_FORUM) {
                    shareAppListText(mSelection, true);
                } else if (mSelection == SECTION_TEXT_FILE || mSelection == SECTION_HTML_FILE) {
                    FragmentManager fm = getSupportFragmentManager();
                    ShareTaskFragment taskFragment = (ShareTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
                    if (taskFragment != null) {
                        taskFragment.startTask(mSelection, mAppList);
                    }
                }
            }
        });
        
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
    protected void onDestroy() {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selection", mSelection);
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
            final CharSequence text;
            if (section == SECTION_HTML) {
                intent.setType("text/html");
                text = Html.fromHtml(AppUtil.appInfoToHTML(this, mAppList, footer, false));
            } else if (section == SECTION_FORUM) {
                intent.setType("text/plain");
                text = AppUtil.appInfoToFroum(this, mAppList, footer);
            } else {
                intent.setType("text/plain");
                text = AppUtil.appInfoToText(this, mAppList, footer);
            }
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    getResources().getQuantityString(R.plurals.share_title, mAppList.size()));
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSelection = position + mStartIndex;
        mMessage.setText(OPTIONS_MSG[mSelection]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        onItemSelected(adapterView, view, position, id);
    }
}
