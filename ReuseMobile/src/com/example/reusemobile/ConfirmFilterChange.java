package com.example.reusemobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmFilterChange extends DialogFragment {
    public interface ConfirmFilterDeleteListener {
        public void onEditClick(DialogFragment dialog, String filter, String tags);
        public void onDeleteClick(DialogFragment dialog, String filter);
        public void onCancelClick(DialogFragment dialog);
    }
    
    ConfirmFilterDeleteListener mListener;
    
    public static ConfirmFilterChange newInstance(String filter, String tags) {
        String message = filter + "\n\n" +
                         "With tags:\n" +
                         tags;
        Bundle bdl = new Bundle(2);
        bdl.putString("message", message);
        bdl.putString("filter", filter);
        bdl.putString("tags", tags);
        ConfirmFilterChange instance = new ConfirmFilterChange();
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
        builder.setTitle("Change Filter")
               .setMessage(getArguments().getString("message"))
               .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onEditClick(ConfirmFilterChange.this, getArguments().getString("filter"), getArguments().getString("tags"));
                   }
               })
               .setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onDeleteClick(ConfirmFilterChange.this, getArguments().getString("filter"));
                }
            })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onCancelClick(ConfirmFilterChange.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}