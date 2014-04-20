package com.example.reusemobile.views;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.example.reusemobile.GlobalApplication;
import com.example.reusemobile.logging.Sting;

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
            new SendUnregisterRequest().execute();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            pref.edit().putBoolean("isVerified", false).commit();
            ((Activity) context).finish();
        } else {
            // Do nothing
        }
    }
    
    private class SendUnregisterRequest extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
         // Create a new HttpClient and Post Header
            String port = GlobalApplication.serverPort;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://armadillo.xvm.mit.edu:" + port + "/api/login/unregister/");
            String email = PreferenceManager.getDefaultSharedPreferences(context).getString("username", "");
            httppost.addHeader("USERNAME", email);
            String token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "");
            httppost.addHeader("TOKEN", token);
            try {
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                
                if(response.getStatusLine().getStatusCode() != 200) {
                    Sting.logError((Activity) context, Sting.CLAIM_ERROR, response.getStatusLine().getReasonPhrase());
                    Log.i("POST", httppost.toString());
                    return "An error occured in unregister:\n" + response.getStatusLine().getReasonPhrase();
                }
                
                return "Successfully Unregistered";
            } catch (Exception e) {
                Sting.logError((Activity) context, Sting.CLAIM_ERROR, "Exception: " + e.getLocalizedMessage());
                return "An exception occured during unregister:\n" + e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            pref.edit().remove("username").commit();
            pref.edit().remove("token").commit();
            
            if(result == null) {
            } else {
                if(GlobalApplication.debug) Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
}