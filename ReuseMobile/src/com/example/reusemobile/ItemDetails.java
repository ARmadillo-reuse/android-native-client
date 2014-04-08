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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ItemDetails extends ActionBarActivity {
    private TextView nameField;
    private TextView descField;
    private TextView dateField;
    private TextView locField;
    private Button claimButton;
    
    private int itemId;
    private String itemName;
    private String itemDescription;
    private Long itemDate;
    private String itemLocation;
    private Boolean itemAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
        
        nameField = (TextView) findViewById(R.id.item_name);
        descField = (TextView) findViewById(R.id.item_desc);
        dateField = (TextView) findViewById(R.id.desc_date);
        locField = (TextView) findViewById(R.id.desc_loc);
        claimButton = (Button) findViewById(R.id.claim_button);
        
        Intent intent = getIntent();
        itemId = intent.getIntExtra(MainStream.ITEM_ID, -1);
        itemName = intent.getStringExtra(MainStream.ITEM_NAME);
        itemDescription = intent.getStringExtra(MainStream.ITEM_DESCRIPTION);
        itemDate = intent.getLongExtra(MainStream.ITEM_DATE, 0L);
        itemLocation = intent.getStringExtra(MainStream.ITEM_LOCATION);
        itemAvailable = intent.getBooleanExtra(MainStream.ITEM_AVAILABLE, false);
        nameField.setText(itemName);
        descField.setText(itemDescription);
        dateField.setText(DateFormat.format("MM/dd/yyyy hh:mm:ssa", itemDate));
        locField.setText(itemLocation);
        if(!itemAvailable) {
            claimButton.setEnabled(false);
            claimButton.setText("Claimed");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.item_details, menu);
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
    
    public void claim(View view) {
        // Process claim action
        claimButton.setEnabled(false);

        new SendClaim().execute(itemId);
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
            View rootView = inflater.inflate(R.layout.fragment_item_details,
                    container, false);
            
            return rootView;
        }
    }

    
    private class SendClaim extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
         // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://armadillo.xvm.mit.edu:8000/api/thread/claim/");
            String email = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("username", "");
            httppost.addHeader("USERNAME", email);
            String token = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("token", "");
            httppost.addHeader("TOKEN", token);
            Integer id = params[0];
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("item_id", id.toString()));
                //nameValuePairs.add(new BasicNameValuePair("gcm_id", "1038751243496"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                
                if(response.getStatusLine().getStatusCode() != 200) {
                    Log.e("Item Details", String.valueOf(itemId));
                    return "An error occured in item claim:\n" + response.getStatusLine().getReasonPhrase();
                }
                boolean wasSuccessful = new JSONObject(EntityUtils.toString(response.getEntity())).getBoolean("success");
                if(!wasSuccessful) {
                    return "Item claim failed:\nThe item was already claimed :'(";
                }
                
                return null;
            } catch (Exception e) {
                return "An exception occured during item claim:\n" + e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result == null) {
                claimButton.setText("Claimed");
                Toast.makeText(getApplicationContext(), "Item Claimed", Toast.LENGTH_SHORT).show();
            } else {
                claimButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
