package com.projectsexception.myapplist.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * RelativeLayout that implements Checkable interface
 * Taken from androcode.es
 */
public class CheckedLayout extends FrameLayout implements Checkable {
 
    /**
     * Variable to store state
     */
    private boolean mChecked=false;
 
    private final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };
 
    public CheckedLayout(Context context) {
        super(context);
    }
 
    public CheckedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        refreshDrawableState();
        invalidate();
    }
 
    @Override
    public boolean isChecked() {
        return mChecked;
    }
 
    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
 
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}