package com.example.reusemobile.views;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import com.example.reusemobile.model.Item;
import com.roscopeco.ormdroid.Entity;

public class DumpDBDialog extends DialogPreference {
    Context context;

    public DumpDBDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult) {
            // Dump DB
            for(Item item : Entity.query(Item.class).executeMulti()) {
                item.delete();
            }
            Toast.makeText(context, "DB Cleared", Toast.LENGTH_SHORT).show();
        } else {
            // Do nothing
        }
    }
}