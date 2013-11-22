package com.manuelpeinado.multichoiceadapter;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.widget.BaseAdapter;

public class MultiChoiceAdapterHelperFix extends MultiChoiceAdapterHelperBaseFix {

    private ActionMode actionMode;

    protected MultiChoiceAdapterHelperFix(BaseAdapter owner) {
        super(owner);
    }

    @Override
    protected void startActionMode() {
        if (!(adapterView.getContext() instanceof ActionBarActivity)) {
            throw new IllegalStateException("List view must belong to an ActionBarActivity");
        }
        if (!(owner instanceof ActionMode.Callback)) {
            throw new IllegalStateException("Owner adapter must implement ActionMode.Callback");
        }
        ActionBarActivity activity = (ActionBarActivity) adapterView.getContext();
        actionMode = activity.startSupportActionMode((ActionMode.Callback)owner);
    }

    @Override
    protected void finishActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    @Override
    protected void setActionModeTitle(String title) {
        actionMode.setTitle(title);
    }

    @Override
    protected boolean isActionModeStarted() {
        return actionMode != null;
    }

    @Override
    protected void clearActionMode() {
        actionMode = null;
    }
}