package com.example.reusemobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmPost extends DialogFragment {
    public interface ConfirmPostListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    ConfirmPostListener mListener;
    
    public static ConfirmPost newInstance(String name, String description, String location, String tags) {
        String message = "Name:\n" +
                         name + "\n\n" +
                         "Description:\n" +
                         description + "\n\n" +
                         "Location:\n" +
                         location + "\n\n" + 
                         "Tags:\n" +
                         tags;
        Bundle bdl = new Bundle(1);
        bdl.putString("message", message);
        ConfirmPost instance = new ConfirmPost();
        instance.setArguments(bdl);
        return instance;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ConfirmPostListener) activity;
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
        builder.setTitle("Confirm New Post")
               .setMessage(getArguments().getString("message"))
               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogPositiveClick(ConfirmPost.this);
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogNegativeClick(ConfirmPost.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}