package com.example.reusemobile.logging;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.example.reusemobile.GlobalApplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Static class for logging client actions and
 * sending to the server.
 * 
 * "Every move you make,
 *  Every step you take,
 *  I'll be watching you."
 *              -- Sting & The Police
 */
public class Sting {
    private static final String BUTTON_PUSH = "BUTTON_PUSH";
    private static final String ACTIVITY_START = "ACTIVITY_START";
    private static final String NOTIFICATION_EVENT = "NOTIFICATION_EVENT";
    
    // Button Ids
    public static final String DRAWER_OPEN_BUTTON = "DRAWER_OPEN";
    public static final String DRAWER_CLOSE_BUTTON = "DRAWER_CLOSE";
    public static final String DRAWER_OPEN_MENU_BUTTON = "DRAWER_OPEN_MENU";
    public static final String DRAWER_CLOSE_MENU_BUTTON = "DRAWER_CLOSE_MENU";
    public static final String ADD_FILTER_BUTTON = "ADD_FILTER";
    public static final String TOUCH_FILTER_BUTTON = "TOUCH_FILTER";
    public static final String DELETE_FILTER_BUTTON = "DELETE_FILTER";
    public static final String CREATE_FILTER_BUTTON = "CREATE_FILTER";
    public static final String SEARCH_BUTTON = "SEARCH";
    public static final String CLAIM_BUTTON = "CLAIM";
    public static final String POST_BUTTON = "POST";
    
    public static void logButtonPush(Activity activity, String buttonId) {
        //new sendLogData(activity).execute(BUTTON_PUSH, buttonId);
    }
    
    public static void logActivityStart(Activity activity) {
        //new sendLogData(activity).execute(ACTIVITY_START, activity.getClass().toString());
    }
    
    public static void logNotificationEvent(Activity activity, String eventId) {
        //new sendLogData(activity).execute(NOTIFICATION_EVENT, eventId);
    }
    
    private static class sendLogData extends AsyncTask<String, Void, String> {
        private Activity activity;
        
        public sendLogData(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://armadillo.xvm.mit.edu:8000/api/log/"); //TODO Proper url
            String email = PreferenceManager.getDefaultSharedPreferences(activity).getString("username", "");
            String token = PreferenceManager.getDefaultSharedPreferences(activity).getString("token", "");
            httppost.addHeader("USERNAME", email);
            httppost.addHeader("TOKEN", token);
            
            String logEvent = params[0];
            String logDetails = params[1];
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("log_event", logEvent));
                nameValuePairs.add(new BasicNameValuePair("log_details", logDetails));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                
                if(response.getStatusLine().getStatusCode() != 200) {
                    return "An error occured in item claim:\n" + response.getStatusLine().getReasonPhrase();
                }
                
                return "Successfully Logged!\n" + logEvent + ": " + logDetails;
            } catch (Exception e) {
                return "An exception occured during item claim:\n" + e.getLocalizedMessage();
            }
        }
        
        @Override
        protected void onPostExecute(String result) {
            if(GlobalApplication.debug) {
                Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
            }
        }
        
    }
}