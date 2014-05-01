package com.example.reusemobile;

import com.example.reusemobile.logging.Sting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class CustomMessage extends ActionBarActivity {
    private int itemId;
    private EditText customMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_message);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
        
        Intent intent = getIntent();
        itemId = intent.getIntExtra(MainStream.ITEM_ID, -1);
        customMessage = (EditText) findViewById(R.id.custom_message);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        getMenuInflater().inflate(R.menu.custom_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            finish();
            return true;
        case  R.id.action_settings:
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void send(View view) {
        //TODO add this feature to server
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
            View rootView = inflater.inflate(R.layout.fragment_custom_message,
                    container, false);
            return rootView;
        }
    }

}
