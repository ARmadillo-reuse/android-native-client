package com.example.reusemobile;

import com.example.reusemobile.model.Item;
import com.roscopeco.ormdroid.Entity;
import com.roscopeco.ormdroid.Query;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ItemDetails extends ActionBarActivity {
    private TextView nameField;
    private TextView descField;
    private TextView dateField;
    private TextView locField;
    private Button claimButton;

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
        String itemName = intent.getStringExtra(MainStream.ITEM_NAME);
        String itemDescription = intent.getStringExtra(MainStream.ITEM_DESCRIPTION);
        Long itemDate = intent.getLongExtra(MainStream.ITEM_DATE, 0L);
        String itemLocation = intent.getStringExtra(MainStream.ITEM_LOCATION);
        Boolean itemAvailable = intent.getBooleanExtra(MainStream.ITEM_AVAILABLE, false);
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
        //REMOVEME
        Item item = Entity.query(Item.class).where(Query.and(Query.eql("name", nameField.getText()), Query.eql("description", descField.getText()))).execute();
        item.markAsClaimed();
        claimButton.setEnabled(false);
        claimButton.setText("Claimed");
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

}
