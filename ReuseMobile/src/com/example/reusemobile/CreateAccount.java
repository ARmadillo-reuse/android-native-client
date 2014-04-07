package com.example.reusemobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class CreateAccount extends ActionBarActivity {
    private Timer timer = new Timer();
    private TimerTask appLogin = new TimerTask() {
        
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    startActivity(new Intent(getApplicationContext(), MainStream.class));
                    finish();
                }
            });

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
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

    
    private class SendLogin extends AsyncTask<String, Void, HttpResponse> {
        String email;
        
        @Override
        protected HttpResponse doInBackground(String... params) {
         // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://armadillo.xvm.mit.edu:8000/api/login/signup/");
            email = params[0];
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("email", email));
                //nameValuePairs.add(new BasicNameValuePair("gcm_id", "1038751243496"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                
                return response;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(HttpResponse result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result.getStatusLine().getStatusCode() == 200) {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("isVerified", true).commit();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("username", email).commit();
                Toast.makeText(getApplicationContext(), "Verification email sent. Please check your email to verify your account", Toast.LENGTH_LONG).show();
                timer.schedule(appLogin, 0);
            } else {
                Toast.makeText(getApplicationContext(), "An Error occured in login:\n" + result.getStatusLine().getReasonPhrase(), Toast.LENGTH_LONG).show();
            }
        }
        
        
        
    }
}
