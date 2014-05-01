package com.example.reusemobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class CreateAccount extends ActionBarActivity {
    // GCM Stuff
    public static final String TOKEN_ACTION = "com.example.reusemobile.TOKEN_MESSAGE";
    public static final String EXTRA_TOKEN = "com.example.reusemobile.TOKEN";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private LoginReceiver receiver;
    String SENDER_ID = "1038751243496";
    static final String TAG = "ReuseMobile";
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
        
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isVerified", false)) {
            login();
        } else {
            //  GCM registration.
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(getApplicationContext());
    
            receiver = new LoginReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(TOKEN_ACTION);
            registerReceiver(receiver, filter);
        }
    }
    
    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void createAccount(View view) {
        EditText emailBox = (EditText) findViewById(R.id.create_account_email);
        String email = emailBox.getText().toString().trim();
        if(!email.endsWith("@mit.edu")) {
            Toast.makeText(this, "Email address must be a valid MIT email", Toast.LENGTH_SHORT).show();
        } else {
            ConnectivityManager connMgr = (ConnectivityManager) 
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new SendLogin().execute(email);
            } else {
                Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
            }
            
            
            // REMOVE ME

        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_create_account,
                    container, false);
            return rootView;
        }
    }
    
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.equals("")) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private boolean registerForGCM() {
        boolean wasSuccessful;
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            }
            regid = gcm.register(SENDER_ID);
            final String msg = "Device registered, registration ID=" + regid;
            if(GlobalApplication.isDebug()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Persist the regID - no need to register again.
            storeRegistrationId(getApplicationContext(), regid);
            wasSuccessful = true;
        } catch (IOException ex) {
            final String msg = "Error :" + ex.getMessage();
            if(GlobalApplication.isDebug()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
            wasSuccessful = false;
        }
        return wasSuccessful;
    }
    
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    
    private class SendLogin extends AsyncTask<String, Void, String> {
        String email;
        
        @Override
        protected String doInBackground(String... params) {
         // Create a new HttpClient and Post Header
            String port = GlobalApplication.getServerPort();
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://armadillo.xvm.mit.edu:" + port + "/api/login/signup/");
            email = params[0];
            if(regid.equals("")) {
                if (!registerForGCM()) return "Error in GCM Registration";
            }
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("email", email));
                nameValuePairs.add(new BasicNameValuePair("gcm_id", regid));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                Log.i("Reg ID", regid);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                if(response.getStatusLine().getStatusCode() == 200) {
                    return "Successful";
                } else {
                    return response.getStatusLine().getReasonPhrase();
                }
            } catch (IOException e) {
                return "Exception: " + e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result.equals("Successful")) {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("username", email).commit();
//                if(email.equals("crogers3@mit.edu")) {
//                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("token", "64919ef302e63945b80b171e0ca2ec2c46b889ae301e5491a66b0831").commit();
//                } else if(email.equals("shaladi@mit.edu")) {
//                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("token", "abdcb57ca3fb20f73e4d7c4546a43ce869b1217a368864ffec86ae82").commit();
//                } else if(email.equals("manting@mit.edu")) {
//                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("token", "079646d723d12daaba4449789bd7ba8021c26953dcc09d5c017eed7f").commit();
//                } else if(email.equals("akonradi@mit.edu")) {
//                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("token", "1831ef98490487f93463bbdfb81bb8a0ee483a38ad6c79a05f10b69b").commit();
//                }
                Toast.makeText(getApplicationContext(), "Verification email sent. Please check your email to verify your account", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "An Error occured in login:\n" + result, Toast.LENGTH_LONG).show();
            }
        }

    }
    
    private class LoginReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            login();
        }
        
    }
    
    private void login() {
        startActivity(new Intent(this, MainStream.class));
        finish();
    }
}
