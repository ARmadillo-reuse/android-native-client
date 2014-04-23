package com.example.reusemobile.views;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

public class ResetLastUpdateDialog extends DialogPreference {
    Context context;

    public ResetLastUpdateDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult) {
            // Reset
            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("lastUpdate", 0).commit();
            Toast.makeText(context, "Last update reset", Toast.LENGTH_SHORT).show();
        } else {
            // Do nothing
        }
    }
}