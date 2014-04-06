package com.example.reusemobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.reusemobile.model.Item;
import com.example.reusemobile.views.Drawer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.roscopeco.ormdroid.Entity;
import com.roscopeco.ormdroid.ORMDroidApplication;
import com.roscopeco.ormdroid.Query;
import com.roscopeco.ormdroid.TypeMapper;

public class MainStream extends ActionBarActivity {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
    public final static String ITEM_NAME = "com.example.reusemobile.ITEM_NAME";
    public final static String ITEM_DESCRIPTION = "com.example.reusemobile.ITEM_DESCRIPTION";
    public final static String ITEM_DATE = "com.example.reusemobile.ITEM_DATE";
    public final static String ITEM_LOCATION = "com.example.reusemobile.ITEM_LOCATION";
    public final static String ITEM_AVAILABLE = "com.example.reusemobile.ITEM_AVAILABLE";
    public final static String FILTERS = "com.example.reusemobile.FILTERS";
    
    public DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mDrawerToggle;
    public ListView itemList;
    public Drawer drawer;
    
    private String[] currentFilters;
    private Timer timer = new Timer();
    private int updateInterval = 30 * 1000;
    private TimerTask refreshTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshItems();
                }
            });

        }
    };
    private int pullInterval = 60 * 1000;
    private TimerTask pullFromServerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pullFromServerAndUpdate();
                }
            });

        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is verified
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isVerified", false)) {
            startActivity(new Intent(this, CreateAccount.class));
            finish();
        }
        
        setContentView(R.layout.activity_main_stream);
        
        if(checkPlayServices()) {
            itemList = (ListView) findViewById(R.id.stream);
            drawer = new Drawer(this);
            
            // Check if intent was a search
            Intent intent = getIntent();
            handleIntent(intent);
            itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                    int position, long id) {
                    @SuppressWarnings("unchecked")
                    final Item item = (Item) ((Map<String, Object>) parent.getItemAtPosition(position)).get("item");
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
            
            // Set a timer to update itemList
            timer.schedule(refreshTask, 0, updateInterval); // Update every 30 seconds
            timer.schedule(pullFromServerTask, 0, pullInterval); // Update every 30 seconds
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is verified
        if(checkPlayServices()) {
            if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isVerified", false)) {
                startActivity(new Intent(this, CreateAccount.class));
                finish();
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                // Close the drawer
                mDrawerLayout.closeDrawers();
            } else {
                // Open the drawer
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_stream, menu);
        
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        // Assumes current activity is the searchable activity
        SearchableInfo searchInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchInfo);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_map_view:
            Intent intent = new Intent(this, MapView.class);
            intent.putExtra(FILTERS, currentFilters);
            startActivity(intent);
            return true;
        case R.id.action_search:
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
    
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
    
    private void displayItemDetails(Item item) {
        Intent intent = new Intent(this, ItemDetails.class);
        intent.putExtra(ITEM_NAME, item.name);
        intent.putExtra(ITEM_DESCRIPTION, item.description);
        intent.putExtra(ITEM_DATE, item.date.getTime());
        intent.putExtra(ITEM_LOCATION, item.location);
        intent.putExtra(ITEM_AVAILABLE, item.isAvailable);
        startActivity(intent);
    }
    
    public void showAll() {
        currentFilters = new String[0];
        refreshItems();
    }
    
    public void applyFilter(String filter) {
        String[] keywords = getSharedPreferences(GlobalApplication.filterPreferences, Context.MODE_PRIVATE).getString(filter, "").split(" ");
        currentFilters = keywords;
        refreshItems();
    }
    
    private void refreshItems() {
        if (currentFilters.length == 0) {
            setTitle("All Items");
        } else {
            setTitle("Filtered Items");
        }
        List<Map<String, Object>> data = getItems(currentFilters);
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                                                  android.R.layout.simple_list_item_2,
                                                  new String[] {"name", "description"},
                                                  new int[] {android.R.id.text1,
                                                             android.R.id.text2}) {

                                                                @Override
                                                                public void setViewText(
                                                                        TextView v,
                                                                        String text) {
                                                                    // TODO Auto-generated method stub
                                                                    v.setMaxLines(2);
                                                                    v.setEllipsize(TruncateAt.END);
                                                                    super.setViewText(v, text);
                                                                }
            
            
        };
        itemList.setAdapter(adapter);
    }
    
    public static List<Map<String, Object>> getItems(String[] keywords) {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        
        // No keywords
        if (keywords.length == 0) {
            for (Item item : Entity.query(Item.class).executeMulti()) {
                Map<String, Object> datum = new HashMap<String, Object>(3);
                datum.put("name", item.name);
                datum.put("description", item.description);
                datum.put("item", item);
                data.add(datum);
            }
        } else {
            StringBuilder whereQuery = new StringBuilder();
            for (int i = 0; i < keywords.length - 1; i++) {
                whereQuery.append("name LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[i] + '%') + " OR ");
                whereQuery.append("description LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[i] + '%') + " OR ");
            }
            whereQuery.append("name LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[keywords.length - 1] + '%') + " OR ");
            whereQuery.append("description LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[keywords.length - 1] + '%'));
            for (Item item : Entity.query(Item.class).where(whereQuery.toString()).executeMulti()) {
                Map<String, Object> datum = new HashMap<String, Object>(3);
                datum.put("name", item.name);
                datum.put("description", item.description);
                datum.put("item", item);
                data.add(datum);
            }
        }
        
        return data;
    }
    
    private void handleIntent(Intent intent) {
        drawer.updateFilters();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            currentFilters = query.trim().split(" ");
            refreshItems();
        } else {
            showAll();
        }
    }
    
    private void pullFromServerAndUpdate() {
        // Pull from server
        List<Item> itemUpdates = new ArrayList<Item>();
        itemUpdates.add(new Item("Refrigerator", "It's a broken refrigerator", new Date(), "32-044", "appliance", true));
        
        // Update
        for (Item item : itemUpdates) {
            String name = item.name;
            String desc = item.description;
            Item storedItem = Entity.query(Item.class).where(Query.and(Query.eql("name", name), Query.eql("description", desc))).execute();
            if(storedItem != null) {
                storedItem.isAvailable = item.isAvailable;
                storedItem.save();
            } else {
                // Add new item
                item.save();
            }
        }
        refreshItems();
    }
    
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("NOT_SUPPORTED", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
