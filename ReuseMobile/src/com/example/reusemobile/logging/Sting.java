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
import android.util.Log;
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
    private static final String ERROR = "ERROR: ";
    
    // Error Ids
    public static final String CLAIM_ERROR = "CLAIM";
    public static final String POST_ERROR = "POST";
    public static final String PULL_ERROR = "PULL";
    
    // Button Ids
    public static final String ACTION_MAP = "ACTION_MAP";
    public static final String ACTION_NEW_POST = "ACTION_NEW_POST";
    public static final String DRAWER_OPEN_BUTTON = "DRAWER_OPEN";
    public static final String DRAWER_CLOSE_BUTTON = "DRAWER_CLOSE";
    public static final String DRAWER_OPEN_MENU_BUTTON = "DRAWER_OPEN_MENU";
    public static final String DRAWER_CLOSE_MENU_BUTTON = "DRAWER_CLOSE_MENU";
    public static final String DRAWER_MANAGE_FILTERS = "DRAWER_MANAGE_FILTERS";
    public static final String DRAWER_NEW_POST = "DRAWER_NEW_POST";
    public static final String DRAWER_SETTINGS = "DRAWER_SETTINGS";
    public static final String DISPLAY_ALL = "DISPLAY_ALL";
    public static final String DISPLAY_FILTER = "DISPLAY_FILTER";
    public static final String DISPLAY_SEARCH = "DISPLAY_SEARCH";
    public static final String ADD_FILTER_BUTTON = "ADD_FILTER";
    public static final String TOUCH_FILTER_BUTTON = "TOUCH_FILTER";
    public static final String DELETE_FILTER_BUTTON = "DELETE_FILTER";
    public static final String CREATE_FILTER_BUTTON = "CREATE_FILTER";
    public static final String CLAIM_BUTTON = "CLAIM";
    public static final String POST_BUTTON = "POST";
    public static final String MAP_MARKER = "MAP_MARKER";
    public static final String MAP_DETAILS = "MAP_DETAILS";
    
    public static void logButtonPush(Activity activity, String buttonId) {
        if(GlobalApplication.logging) {
            Log.i(BUTTON_PUSH, buttonId);
            new sendLogData(activity).execute(BUTTON_PUSH, buttonId);
        }
    }
    
    public static void logActivityStart(Activity activity) {
        if(GlobalApplication.logging) {
            Log.i(ACTIVITY_START, activity.getClass().toString());
            new sendLogData(activity).execute(ACTIVITY_START, activity.getClass().toString());
        }
    }
    
    public static void logNotificationEvent(Activity activity, String eventId) {
        if(GlobalApplication.logging) {
            Log.i(NOTIFICATION_EVENT, eventId);
            new sendLogData(activity).execute(NOTIFICATION_EVENT, eventId);
        }
    }
    
    public static void logError(Activity activity, String errorId, String reason) {
        if(GlobalApplication.logging) {
            Log.i(ERROR + errorId, reason);
            new sendLogData(activity).execute(ERROR + errorId, reason);
        }
    }
    
    private static class sendLogData extends AsyncTask<String, Void, String> {
        private Activity activity;
        
        public sendLogData(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {
            // Create a new HttpClient and Post Header
            String port = GlobalApplication.getServerPort();
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://armadillo.xvm.mit.edu:" + port + "/api/thread/log/");
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
                    return "An error occured in sending log:\n" + response.getStatusLine().getReasonPhrase();
                }
                
                return "Successfully Logged!\n" + logEvent + ": " + logDetails;
            } catch (Exception e) {
                return "An exception occured during sending log:\n" + e.getLocalizedMessage();
            }
        }
        
        @Override
        protected void onPostExecute(String result) {
            if(GlobalApplication.isDebugLogging()) {
                Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
            }
        }
        
    }
}