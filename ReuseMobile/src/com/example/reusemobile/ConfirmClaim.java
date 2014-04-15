package com.example.reusemobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmClaim extends DialogFragment {
    public interface ConfirmClaimListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    ConfirmClaimListener mListener;
    
    public static ConfirmClaim newInstance(String itemName) {
        String message = "Please confirm you have already claimed\n\n" +
                         "\"" + itemName + "\"\n\n" +
                         "Note: Please only claim if you have already physically obtained " +
                         "the item.";
        Bundle bdl = new Bundle(1);
        bdl.putString("message", message);
        ConfirmClaim instance = new ConfirmClaim();
        instance.setArguments(bdl);
        return instance;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ConfirmClaimListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ConfirmClaimListener");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Item Claim")
               .setMessage(getArguments().getString("message"))
               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogPositiveClick(ConfirmClaim.this);
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogNegativeClick(ConfirmClaim.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}