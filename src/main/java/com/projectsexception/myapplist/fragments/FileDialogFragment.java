package com.projectsexception.myapplist.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.projectsexception.myapplist.R;

public class FileDialogFragment extends SherlockDialogFragment {

    public static interface CallBack {
        void nameAccepted(String name);
    }

    private CallBack mCallBack;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CallBack) {
            mCallBack = (CallBack) activity;
        } else {
            throw new IllegalStateException("Activity must implements Fragment's CallBack");
        }
    }

    @Override
    public void onDetach() {
        mCallBack = null;
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getSherlockActivity();
        if (context != null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle(R.string.new_file_dialog_title);
            alert.setMessage(R.string.new_file_dialog_msg);
            // By default, the name is rmb-<date>.xml
            Time time = new Time();
            time.setToNow();
            String fileName = context.getString(R.string.new_file_dialog_name, time.format("%Y%m%d"));
            // Set an EditText view to get user input
            final EditText input = new EditText(context);
            input.setText(fileName);
            alert.setView(input);
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (mCallBack != null) {
                        mCallBack.nameAccepted(input.getText().toString());
                    }
                }
            });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            return alert.create();
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }
}
