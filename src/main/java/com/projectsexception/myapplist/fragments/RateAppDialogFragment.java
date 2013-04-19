package com.projectsexception.myapplist.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.projectsexception.myapplist.R;
import com.projectsexception.myapplist.util.CustomLog;

public class RateAppDialogFragment extends SherlockDialogFragment implements DialogInterface.OnClickListener {

    public static final String NUM_EXECUTIONS = "num_executions";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getSherlockActivity();
        if (context != null) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(R.string.rate_dialog_title);
            dialog.setMessage(R.string.rate_dialog_msg);
            dialog.setCancelable(false);
            dialog.setNegativeButton(R.string.rate_dialog_negative, this);
            dialog.setNeutralButton(R.string.rate_dialog_neutral, this);
            dialog.setPositiveButton(R.string.rate_dialog_positive, this);
            return dialog.create();
        }
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final Context context = getSherlockActivity();
        if (context != null) {
            final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            int newValue = -1;
            if (DialogInterface.BUTTON_NEUTRAL == which) {
                newValue = 0;
            } else if (DialogInterface.BUTTON_POSITIVE == which) {
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
                } catch (Exception e) {
                    CustomLog.error("RateAppDialogFragment", e);
                }
            }
            editor.putInt(NUM_EXECUTIONS, newValue).commit();
        }
    }
}
