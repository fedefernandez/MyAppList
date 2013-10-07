package com.projectsexception.myapplist.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.xml.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.InjectView;
import butterknife.Views;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class FolderPickerFragment extends ListFragment implements View.OnClickListener {

    public static final String ARG_PATH = "path";

    public static FolderPickerFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putString(ARG_PATH, path);
        FolderPickerFragment frg = new FolderPickerFragment();
        frg.setArguments(args);
        return frg;
    }

    public static interface CallBack {
        void selectedFolder(File folder);
        void cancel();
    }

    @InjectView(android.R.id.button2) ImageButton mNavigationUpButton;
    @InjectView(android.R.id.text1) TextView mSelectedFolder;
    @InjectView(android.R.id.closeButton) Button mCancelButton;
    @InjectView(android.R.id.button1) Button mOkButton;
    @InjectView(android.R.id.empty) TextView mEmptyView;
    FolderAdapter mAdapter;

    CallBack mCallBack;
    File mFolder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CallBack) {
            mCallBack = (CallBack) activity;
        } else {
            throw new IllegalStateException("Activity must implement fragment's callback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBack = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picker_folder, container, false);
        Views.inject(this, view);
        mNavigationUpButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mOkButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        getListView().setEmptyView(mEmptyView);

        prepareFolder(savedInstanceState);

        if (isValidFolder(mFolder, false)) {
            buildView();
        } else {
            mCallBack.cancel();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_picker_folder, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new_folder) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.dialog_new_folder_title);
            alert.setMessage(R.string.dialog_new_folder_msg);
            final EditText input = new EditText(getActivity());
            alert.setView(input);
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    createNewFolder(input.getText().toString());
                }
            });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFolder != null) {
            outState.putString(ARG_PATH, mFolder.getAbsolutePath());
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        File file = (File) mAdapter.getItem(position);
        if (isValidFolder(file, false)) {
            mFolder = file;
            buildView();
        } else {
            Crouton.makeText(getActivity(), R.string.folder_inaccessible, Style.ALERT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mNavigationUpButton) {
            if (mFolder != null && isValidFolder(mFolder.getParentFile(), false)) {
                mFolder = mFolder.getParentFile();
                buildView();
            } else {
                Crouton.makeText(getActivity(), R.string.parent_folder_inaccessible, Style.ALERT).show();
            }
        } else if (v == mCancelButton) {
            mCallBack.cancel();
        } else if (v == mOkButton) {
            if (isValidFolder(mFolder, true)) {
                mCallBack.selectedFolder(mFolder);
            } else {
                Crouton.makeText(getActivity(), R.string.folder_not_writable, Style.ALERT).show();
            }
        }
    }

    void prepareFolder(Bundle savedInstanceState) {
        String path = null;
        if (savedInstanceState != null) {
            path = savedInstanceState.getString(ARG_PATH);
        } else if (getArguments() != null)  {
            path = getArguments().getString(ARG_PATH);
        }

        if (path != null) {
            mFolder = new File(path);
        }

        if (!isValidFolder(mFolder, false)) {
            mFolder = FileUtil.prepareApplicationDir(getActivity(), true);
        }
    }

    void buildView() {
        final String selectedFolder = getString(R.string.selected_folder);
        SpannableStringBuilder sb = new SpannableStringBuilder(selectedFolder);
        sb.append("\n");
        sb.append(mFolder.getAbsolutePath());
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, selectedFolder.length(), 0);
        mSelectedFolder.setText(sb);
        File[] contents = mFolder.listFiles();
        List<File> fileList = new ArrayList<File>();
        if (contents != null) {
            for (File file : contents) {
                if (file.isDirectory()) {
                    fileList.add(file);
                }
            }
            Collections.sort(fileList);
        }
        if (mAdapter == null) {
            mAdapter = new FolderAdapter(getActivity(), fileList);
            setListAdapter(mAdapter);
        } else {
            mAdapter.setFileList(fileList);
        }
    }

    boolean isValidFolder(File folder, boolean write) {
        return folder != null && folder.exists() && folder.isDirectory() && (!write || folder.canWrite());
    }

    void createNewFolder(String folderName) {
        if (isValidFolder(mFolder, true)) {
            File file = new File(mFolder, folderName);
            if (file.exists()) {
                Crouton.makeText(getActivity(), R.string.file_exists, Style.ALERT).show();
            } else if (file.mkdir()) {
                mFolder = file;
                buildView();
            } else {
                Crouton.makeText(getActivity(), R.string.cant_create_folder, Style.ALERT).show();
            }
        } else {
            Crouton.makeText(getActivity(), R.string.folder_not_writable, Style.ALERT).show();
        }
    }

    static class FolderAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<File> mFileList;

        FolderAdapter(Context context, List<File> fileList) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mFileList = fileList;
        }

        void setFileList(List<File> fileList) {
            mFileList = fileList;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return mFileList == null ? 0 : mFileList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            TextView textView;
            if (view == null) {
                view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                textView = (TextView) view.findViewById(android.R.id.text1);
                view.setTag(textView);
            } else {
                textView = (TextView) view.getTag();
            }

            File file = (File) getItem(position);
            textView.setText(file.getName());

            return view;
        }
    }
}
