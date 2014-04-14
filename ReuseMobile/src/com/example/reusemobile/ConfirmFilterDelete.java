package com.example.reusemobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmFilterDelete extends DialogFragment {
    public interface ConfirmFilterDeleteListener {
        public void onDialogPositiveClick(DialogFragment dialog, String filter);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    ConfirmFilterDeleteListener mListener;
    
    public static ConfirmFilterDelete newInstance(String filter, String tags) {
        String message = "Are you sure you want to delete filter:\n\n" +
                         filter + "\n\n" +
                         "With tags:\n" +
                         tags;
        Bundle bdl = new Bundle(2);
        bdl.putString("message", message);
        bdl.putString("filter", filter);
        ConfirmFilterDelete instance = new ConfirmFilterDelete();
        instance.setArguments(bdl);
        return instance;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ConfirmFilterDeleteListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ConfirmPostListener");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Filter Delete")
               .setMessage(getArguments().getString("message"))
               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogPositiveClick(ConfirmFilterDelete.this, getArguments().getString("filter"));
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogNegativeClick(ConfirmFilterDelete.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}