package com.example.reusemobile;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
    private String TAG = "GcmIntentService";
    
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging. MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                displayMessage("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                displayMessage("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Process information
                Log.i("RECEIVE GCM", "Got GCM message: " + extras.getString("token"));
                if(extras.containsKey("token")) {
                    Intent loginIntent = new Intent();
                    loginIntent.setAction(CreateAccount.TOKEN_ACTION);
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    pref.edit().putBoolean("isVerified", true).commit();
                    pref.edit().putString("token", extras.getString("token")).commit();
                    sendBroadcast(loginIntent);
                    Log.i("TAG", "Sent intent");
                } else if(extras.containsKey("action")) {
                    if(extras.getString("action").equals("pull")) {
                        Intent pullIntent = new Intent();
                        pullIntent.setAction(MainStream.PULL_ACTION);
                        sendBroadcast(pullIntent);
                    }
                }
                
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                displayMessage("Received: " + extras.toString());
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    
    private void displayMessage(String message) {
        if(GlobalApplication.isDebug()) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}