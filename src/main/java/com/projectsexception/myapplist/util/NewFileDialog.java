package com.projectsexception.myapplist.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.Time;
import android.widget.EditText;

import com.projectsexception.myapplist.R;

public class NewFileDialog {
    
    public static interface Listener {
        void nameAccepted(String name);
    }
    
    public static void showDialog(final Context context, final Listener listener) {
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
                listener.nameAccepted(input.getText().toString());
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

}
