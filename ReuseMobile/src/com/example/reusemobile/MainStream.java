package com.example.reusemobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.reusemobile.model.Item;
import com.roscopeco.ormdroid.Entity;


public class MainStream extends ActionBarActivity {
    public final static String ITEM_NAME = "com.example.reusemobile.ITEM_NAME";
    public final static String ITEM_DESCRIPTION = "com.example.reusemobile.ITEM_DESCRIPTION";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_stream);
        final ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, Entity.query(Item.class).executeMulti());
        ListView itemList = (ListView) findViewById(R.id.stream);
        itemList.setAdapter(adapter);
        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                int position, long id) {
                final Item item = (Item) parent.getItemAtPosition(position);
                displayItemDetails(item);
            }

          });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_stream, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                //openSearch();
                return true;
            case R.id.action_settings:
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void displayItemDetails(Item item) {
        Intent intent = new Intent(this, ItemDetails.class);
        intent.putExtra(ITEM_NAME, item.name);
        intent.putExtra(ITEM_DESCRIPTION, item.description);
        startActivity(intent);
    }
}
