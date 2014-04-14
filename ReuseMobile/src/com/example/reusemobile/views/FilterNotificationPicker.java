package com.example.reusemobile.views;

import com.example.reusemobile.ManageFilters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FilterNotificationPicker extends MultiSelectListPreference {

    public FilterNotificationPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setEntries(ManageFilters.getSortedFilters(getContext()).toArray(new String[0]));
        setEntryValues(ManageFilters.getSortedFilters(getContext()).toArray(new String[0]));
    }
    
}