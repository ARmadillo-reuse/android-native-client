package com.example.reusemobile.views;

import android.app.Activity;
import android.content.Context;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class UnregisterDialog extends DialogPreference {
    Context context;

    public UnregisterDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult) {
            // Unregister User
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("username").commit();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("isVerified", false).commit();
            ((Activity) context).finish();
        } else {
            // Do nothing
        }
    }
    
    
    
}