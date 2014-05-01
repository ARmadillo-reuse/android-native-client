package com.example.reusemobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reusemobile.logging.Sting;

public class NewPost extends ActionBarActivity implements ConfirmPost.ConfirmPostListener{
    private String name;
    private String description;
    private String location;
    private String tags;
    
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        activity = this;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Sting.logActivityStart(this);
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
        getMenuInflater().inflate(R.menu.new_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
        case R.id.action_settings:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void post(View view) {
        // Post item to server
        Sting.logButtonPush(this, Sting.POST_BUTTON);
        name = ((TextView) findViewById(R.id.post_name)).getText().toString();
        description = ((TextView) findViewById(R.id.post_desc)).getText().toString();
        location = ((TextView) findViewById(R.id.post_loc)).getText().toString();
        tags = ((TextView) findViewById(R.id.post_tags)).getText().toString();
        if (!name.equals("") && !location.equals("") && !tags.equals("")) {
            ConfirmPost confirmation = ConfirmPost.newInstance(name, description, location, tags);
            confirmation.show(getSupportFragmentManager(), "ConfirmPost");
        } else {
            Toast.makeText(this, "Name, location and tags must be filled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Post item
        new SendPost().execute(name, description, location, tags);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Do nothing
    }
    
    private class SendPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
         // Create a new HttpClient and Post Header
            String port = GlobalApplication.getServerPort();
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://armadillo.xvm.mit.edu:" + port + "/api/thread/post/");
            String email = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("username", "");
            httppost.addHeader("USERNAME", email);
            String token = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("token", "");
            httppost.addHeader("TOKEN", token);
            String name = params[0];
            String description = params[1];
            String location = params[2];
            String tags = params[3];
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("name", name));
                nameValuePairs.add(new BasicNameValuePair("description", description));
                nameValuePairs.add(new BasicNameValuePair("location", location));
                nameValuePairs.add(new BasicNameValuePair("tags", tags));
                //nameValuePairs.add(new BasicNameValuePair("gcm_id", "1038751243496"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                
                if(response.getStatusLine().getStatusCode() != 200) {
                    Sting.logError(activity, Sting.POST_ERROR, response.getStatusLine().getReasonPhrase());
                    return "An error occured in posting the item:\n" + response.getStatusLine().getReasonPhrase();
                }
                boolean wasSuccessful = new JSONObject(EntityUtils.toString(response.getEntity())).getBoolean("success");
                if(!wasSuccessful) {
                    Sting.logError(activity, Sting.POST_ERROR, "Post success was false");
                    return "An error occured in posting the item:\nPost success was false";
                }
                
                return null;
            } catch (Exception e) {
                Log.e("Post Exception", e.getLocalizedMessage());
                Sting.logError(activity, Sting.POST_ERROR, "Exception: " + e.getLocalizedMessage());
                return "An exception occured during item claim:\n" + e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result == null) {
                Toast.makeText(getApplicationContext(), "Item Posted", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainStream.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
