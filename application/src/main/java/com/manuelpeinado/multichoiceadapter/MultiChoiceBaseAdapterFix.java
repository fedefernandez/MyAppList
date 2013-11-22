package com.manuelpeinado.multichoiceadapter;

import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

import com.manuelpeinado.multichoiceadapter.ItemClickInActionModePolicy;
import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapter;
import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapterHelperFix;

public abstract class MultiChoiceBaseAdapterFix extends BaseAdapter implements ActionMode.Callback, MultiChoiceAdapter {

    private MultiChoiceAdapterHelperFix helper = new MultiChoiceAdapterHelperFix(this);

    public MultiChoiceBaseAdapterFix(Bundle savedInstanceState) {
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public void setAdapterView(AdapterView<? super BaseAdapter> adapterView) {
        helper.setAdapterView(adapterView);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        helper.setOnItemClickListener(listener);
    }

    public void save(Bundle outState) {
        helper.save(outState);
    }

    public void setItemChecked(long position, boolean checked) {
        helper.setItemChecked(position, checked);
    }

    public Set<Long> getCheckedItems() {
        return helper.getCheckedItems();
    }

    public int getCheckedItemCount() {
        return helper.getCheckedItemCount();
    }

    public boolean isChecked(long position) {
        return helper.isChecked(position);
    }

    public void setItemClickInActionModePolicy(ItemClickInActionModePolicy policy) {
        helper.setItemClickInActionModePolicy(policy);
    }

    public ItemClickInActionModePolicy getItemClickInActionModePolicy() {
        return helper.getItemClickInActionModePolicy();
    }

    protected abstract View getViewImpl(int position, View convertView, ViewGroup parent);

    protected void finishActionMode() {
        helper.finishActionMode();
    }

    protected Context getContext() {
        return helper.getContext();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        helper.onDestroyActionMode();
    }

    @Override
    public boolean isItemCheckable(int position) {
        return true;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        View viewWithoutSelection = getViewImpl(position, convertView, parent);
        return helper.getView(position, viewWithoutSelection);
    }
}