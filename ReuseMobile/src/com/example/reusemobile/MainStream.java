package com.example.reusemobile;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
    public final static String ITEM_DATE = "com.example.reusemobile.ITEM_DATE";
    public final static String ITEM_LOCATION = "com.example.reusemobile.ITEM_LOCATION";
    
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    
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
        
        // Set up navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getTitle());
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getTitle());
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_stream, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                //openSearch();
                return true;
            case R.id.action_settings:
                return super.onOptionsItemSelected(item);
            case R.id.action_new_post:
                startActivity(new Intent(this, NewPost.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    private void displayItemDetails(Item item) {
        Intent intent = new Intent(this, ItemDetails.class);
        intent.putExtra(ITEM_NAME, item.name);
        intent.putExtra(ITEM_DESCRIPTION, item.description);
        intent.putExtra(ITEM_DATE, item.date.getTime());
        intent.putExtra(ITEM_LOCATION, item.location);
        startActivity(intent);
    }
}
