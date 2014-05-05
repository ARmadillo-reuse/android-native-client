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
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reusemobile.logging.Sting;
import com.example.reusemobile.model.Item;
import com.roscopeco.ormdroid.Entity;
import com.roscopeco.ormdroid.Query;

public class ItemDetails extends ActionBarActivity implements ConfirmClaim.ConfirmClaimListener {
    private TextView nameField;
    private TextView descField;
    private TextView dateField;
    private TextView locField;
    private TextView senderField;
    private Button claimButton;
    private Button claimSomeButton;
    private Button mapButton;
    
    private int itemId;
    private Item item;
    
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        activity = this;

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
        
        nameField = (TextView) findViewById(R.id.item_name);
        descField = (TextView) findViewById(R.id.item_desc);
        dateField = (TextView) findViewById(R.id.desc_date);
        locField = (TextView) findViewById(R.id.desc_loc);
        senderField = (TextView) findViewById(R.id.desc_sender);
        claimButton = (Button) findViewById(R.id.claim_button);
        claimSomeButton = (Button) findViewById(R.id.claim_some_button);
        mapButton = (Button) findViewById(R.id.view_on_map_button);
        
        Intent intent = getIntent();
        itemId = intent.getIntExtra(MainStream.ITEM_ID, -1);
        item = Entity.query(Item.class).where(Query.eql("pk", itemId)).execute();
        nameField.setText(Html.fromHtml("<b>" + item.name + "</b>"));
        descField.setText(Html.fromHtml(item.description.replaceAll("\\n", "<br>")));
        Linkify.addLinks(descField, Linkify.ALL);
        dateField.setText(DateFormat.format("EEEE h:mm a MMM d, yyyy", item.date));
        locField.setText(item.location);
        senderField.setText(item.sender);
        Linkify.addLinks(senderField, Linkify.EMAIL_ADDRESSES);
        if(!item.isAvailable) {
            claimButton.setEnabled(false);
            claimSomeButton.setEnabled(false);
        }
        if(item.location.equals("") || !item.isAvailable) {
            mapButton.setEnabled(false);
        }
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
        getMenuInflater().inflate(R.menu.item_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
        case R.id.action_settings:
            return true;
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            finish();
            return true;
        case R.id.action_map_view:
            Intent intent = new Intent(this, MapView.class);
            intent.putExtra(MainStream.ITEM_ID, itemId);
            Sting.logButtonPush(this, Sting.ACTION_MAP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void claim(View view) {
        // Process claim action
        Sting.logButtonPush(this, Sting.CLAIM_BUTTON);
        ConfirmClaim confirmClaim = ConfirmClaim.newInstance(item.name);
        confirmClaim.show(getSupportFragmentManager(), "ConfirmClaim");
    }
    
    public void customMessage(View view) {
        Sting.logButtonPush(this, Sting.CUSTOM_RESPONSE);
        Intent intent = new Intent(this, CustomMessage.class);
        intent.putExtra(MainStream.ITEM_ID, itemId);
        startActivity(intent);
    }
    
    public void emailSender(View view) {
        Sting.logButtonPush(this, Sting.EMAIL);
        Intent intent = new Intent(this, EmailSender.class);
        intent.putExtra(MainStream.ITEM_ID, itemId);
        startActivity(intent);
    }
    
    public void map(View view) {
        Sting.logButtonPush(this, Sting.VIEW_MAP);
        Intent intent = new Intent(this, MapView.class);
        intent.putExtra(MainStream.ITEM_ID, itemId);
        startActivity(intent);
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
            String port = GlobalApplication.getServerPort();
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://armadillo.xvm.mit.edu:" + port + "/api/thread/claim/");
            String email = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("username", "");
            httppost.addHeader("USERNAME", email);
            String token = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("token", "");
            httppost.addHeader("TOKEN", token);
            Integer id = params[0];
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("item_id", id.toString()));
                nameValuePairs.add(new BasicNameValuePair("text", ""));
                nameValuePairs.add(new BasicNameValuePair("email", "false"));
                //nameValuePairs.add(new BasicNameValuePair("gcm_id", "1038751243496"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                
                if(response.getStatusLine().getStatusCode() != 200) {
                    Log.e("Item Details", String.valueOf(itemId));
                    Sting.logError(activity, Sting.CLAIM_ERROR, response.getStatusLine().getReasonPhrase());
                    return "An error occured in item claim:\n" + response.getStatusLine().getReasonPhrase();
                }
                boolean wasSuccessful = new JSONObject(EntityUtils.toString(response.getEntity())).getBoolean("success");
                if(!wasSuccessful) {
                    Sting.logError(activity, Sting.CLAIM_ERROR, "Item Already Claimed");
                    return "Item claim failed:\nThe item was already claimed :'(";
                }
                
                return null;
            } catch (Exception e) {
                Sting.logError(activity, Sting.CLAIM_ERROR, "Exception: " + e.getLocalizedMessage());
                return "An exception occured during item claim:\n" + e.getLocalizedMessage();
            }
        }
        
        @Override
        protected void onPreExecute() {
            claimButton.setText("Claiming...");
            claimButton.setEnabled(false);
            claimSomeButton.setEnabled(false);
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result == null) {
                claimButton.setText("Claimed");
                Toast.makeText(getApplicationContext(), "Item Claimed", Toast.LENGTH_SHORT).show();
            } else {
                claimButton.setText(getResources().getText(R.string.details_claim_button));
                claimButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        new SendClaim().execute(itemId);
    }


    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Do Nothing
    }
}
