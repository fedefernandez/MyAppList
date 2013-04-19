package com.projectsexception.myapplist.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.ShareActivity;

public class ShareFragment extends SherlockFragment implements View.OnClickListener {
    
    private static final String SECTION = "section";

    public static ShareFragment newInstance(int section) {
        Bundle args = new Bundle();
        args.putInt(SECTION, section);
        ShareFragment frg = new ShareFragment();
        frg.setArguments(args);
        return frg;
    }
    
    public static interface CallBack {
        void fragmentAttached(int section, ShareFragment f);
        void fragmentStopped(int section); 
    }
    
    private CallBack mCallBack;
    private int mSection;
    private CheckBox mCheckFooter;
    private ToggleButton mToggleText;
    private ToggleButton mToggleFile;
    private TextView mTextView;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CallBack) {
            mCallBack = (CallBack) activity;
            mCallBack.fragmentAttached(getArguments().getInt(SECTION, ShareActivity.SECTION_XML), this);
        } else {
            throw new IllegalStateException(activity.getClass().getName() + " must implement " + CallBack.class.getName());
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        mCallBack = null;
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (mCallBack != null) {
            mCallBack.fragmentStopped(mSection);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_share, container, false);
        mSection = getArguments().getInt(SECTION, ShareActivity.SECTION_XML);
        mCheckFooter = (CheckBox) v.findViewById(R.id.footer);
        if (mSection == ShareActivity.SECTION_XML) {
            v.findViewById(R.id.buttons).setVisibility(View.GONE);
            mCheckFooter.setVisibility(View.GONE);
        } else {
            mCheckFooter.setChecked(true);
            mToggleText = (ToggleButton) v.findViewById(R.id.button_text);
            mToggleText.setOnClickListener(this);
            mToggleFile = (ToggleButton) v.findViewById(R.id.button_file);
            mToggleFile.setOnClickListener(this);
        }
        mTextView = (TextView) v.findViewById(R.id.text);
        return v;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            if (mSection == ShareActivity.SECTION_XML) {
                mTextView.setText(R.string.share_xml_message);
            } else if (mSection == ShareActivity.SECTION_HTML) {
                if (mToggleFile.isChecked()) {
                    mTextView.setText(R.string.share_html_file_message);                    
                } else {
                    mTextView.setText(R.string.share_html_message);                    
                }
            } else {
                if (mToggleFile.isChecked()) {
                    mTextView.setText(R.string.share_text_file_message);
                } else {
                    mTextView.setText(R.string.share_text_message);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_text) { 
            mCheckFooter.setEnabled(true);           
            mToggleText.setChecked(true);
            mToggleFile.setChecked(false);
            if (mSection == ShareActivity.SECTION_HTML) {
                mTextView.setText(R.string.share_html_message);
            } else {
                mTextView.setText(R.string.share_text_message);
            }
        } else if (v.getId() == R.id.button_file) {
            mCheckFooter.setChecked(true);
            mCheckFooter.setEnabled(false);
            mToggleText.setChecked(false);
            mToggleFile.setChecked(true);
            if (mSection == ShareActivity.SECTION_HTML) {
                mTextView.setText(R.string.share_html_file_message);
            } else {
                mTextView.setText(R.string.share_text_file_message);
            }            
        }
    }
    
    public boolean isFile() {
        return mSection == ShareActivity.SECTION_XML || mToggleFile.isChecked();
    }
    
    public boolean isFooter() {
        return mCheckFooter.isChecked();
    }

}
