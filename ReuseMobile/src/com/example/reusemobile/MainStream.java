package com.example.reusemobile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.Toast;

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
    
    public final static String ITEM_ID = "com.example.reusemobile.ID";
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
    
    private String[] currentKeywords;
    private Timer timer = new Timer();
    private int updateInterval = 30 * 1000;
    private TimerTask refreshTask = new TimerTask() {
        @Override
        public void run() {
            Log.i("Refresh", "Refreshing item list");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshItems();
                }
            });

        }
    };
    private int pullInterval = 1 * 60 * 1000;
    private TimerTask pullFromServerTask = new TimerTask() {
        @Override
        public void run() {
            Log.i("Pull", "Pulling from server");
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
        } else {
            // Set a timer to update itemList
            timer.schedule(refreshTask, 0, updateInterval); // Update every 30 seconds
            timer.schedule(pullFromServerTask, 0, pullInterval); // Update every 30 minutes
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
            } else {
                pullFromServerAndUpdate();
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
            intent.putExtra(FILTERS, currentKeywords);
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
    
    /**
     * Launches ItemDetails activity
     * @param item the item to display details for
     */
    private void displayItemDetails(Item item) {
        Intent intent = new Intent(this, ItemDetails.class);
        intent.putExtra(ITEM_ID, item.pk);
        intent.putExtra(ITEM_NAME, item.name);
        intent.putExtra(ITEM_DESCRIPTION, item.description);
        intent.putExtra(ITEM_DATE, item.date.getTime());
        intent.putExtra(ITEM_LOCATION, item.location);
        intent.putExtra(ITEM_AVAILABLE, item.isAvailable);
        startActivity(intent);
    }
    
    /**
     * Shows all items in db on the screen
     */
    public void showAll() {
        currentKeywords = new String[0];
        refreshItems();
    }
    
    /**
     * Grabs the keywords associated with a filter and updates them
     * in the currentFilters field.
     * @param filter the filter whose keywords are to be used.
     */
    public void applyFilter(String filter) {
        String[] keywords = getSharedPreferences(GlobalApplication.filterPreferences, Context.MODE_PRIVATE).getString(filter, "").split(" ");
        currentKeywords = keywords;
        refreshItems();
    }
    
    /**
     * Using the current filters, the keywords from the filter is
     * used in a query and updates the page with the queried items.
     */
    private void refreshItems() {
        if (currentKeywords.length == 0) {
            setTitle("All Items");
        } else {
            setTitle("Filtered Items");
        }
        List<Map<String, Object>> data = getItems(currentKeywords);
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
    
    /**
     * Generates data for the SimpleAdapter to use
     * @param keywords the keywords to search for objects with
     * @return a list of maps, each map representing an item
     */
    public static List<Map<String, Object>> getItems(String[] keywords) {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        
        // No keywords
        if (keywords.length == 0) {
            for (Item item : Entity.query(Item.class).sql(Entity.query(Item.class).orderBy("date").toSql() + " DESC").executeMulti()) {
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
                whereQuery.append("tags LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[i] + '%') + " OR ");
                whereQuery.append("location LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[i] + '%') + " OR ");
            }
            whereQuery.append("name LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[keywords.length - 1] + '%') + " OR ");
            whereQuery.append("description LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[keywords.length - 1] + '%') + " OR ");
            whereQuery.append("tags LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[keywords.length - 1] + '%') + " OR ");
            whereQuery.append("location LIKE " + TypeMapper.encodeValue(ORMDroidApplication.getDefaultDatabase(), '%' + keywords[keywords.length - 1] + '%'));
            for (Item item : Entity.query(Item.class).sql(Entity.query(Item.class).where(whereQuery.toString()).orderBy("date").toSql() + " DESC").executeMulti()) {
                Map<String, Object> datum = new HashMap<String, Object>(3);
                datum.put("name", item.name);
                datum.put("description", item.description);
                datum.put("item", item);
                data.add(datum);
            }
        }
        
        return data;
    }
    
    /**
     * Sets up a filter display if coming from a search query
     * @param intent the intent that started this activity
     */
    private void handleIntent(Intent intent) {
        drawer.updateFilters();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            currentKeywords = query.trim().split(" ");
            refreshItems();
        } else {
            showAll();
        }
    }
    
    /**
     * Ensures the app can connect to the network service and issued
     * the Async pull request.
     */
    private void pullFromServerAndUpdate() {
        // Get date of last update
        long lastUpdate = PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", 0);

        
        // Pull from server
        ConnectivityManager connMgr = (ConnectivityManager) 
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new PullRequest().execute(lastUpdate); // Updates on completion
        } else {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
        }
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
    
    /**
     * Makes an HTTP GET request to the server, asking for a list of updated
     * items, since a given time.
     */
    private class PullRequest extends AsyncTask<Long, Void, String> {
        //TODO: Add time param
        
        @Override
        protected String doInBackground(Long... params) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'+'HH:mm", Locale.ENGLISH);
            String lastUpdate = formatter.format(new Date(params[0]));
            Log.i("last update", lastUpdate);
            
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://armadillo.xvm.mit.edu:8000/api/thread/get/?after=" + lastUpdate);
            String email = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("username", "");
            httpget.addHeader("USERNAME", email);
            String token = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("token", "");
            httpget.addHeader("TOKEN", token);
            try {
                // Execute HTTP Get Request
                HttpResponse response = httpclient.execute(httpget);
                if(response.getStatusLine().getStatusCode() == 200) {
                    // Parse JSON response
                    JSONArray jlist = new JSONArray(EntityUtils.toString(response.getEntity()));
                    Log.i("Response", jlist.toString());
                    for (int i = 0; i < jlist.length(); i++) {
                        // Parse from JSON to Item
                        JSONObject jsonObject = jlist.getJSONObject(i);
                        
                        // Get id
                        Integer id = jsonObject.getInt("pk");
                        JSONObject fields = jsonObject.getJSONObject("fields");
                        
                        // Get name and description
                        String name = fields.getString("name");
                        String desc = fields.getString("description");
                        
                        // Get date
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                        Date date = df.parse(fields.getString("modified"));
                        
                        // Get if available, lat, and long
                        Boolean isAvailable = !fields.getBoolean("claimed");
                        String lat = fields.getString("lat");
                        String lon = fields.getString("lon");
                        
                        String location;
                        String tags;
                        if(!fields.getBoolean("is_email")) {
                            // Expect location, tags
                            location = fields.getString("location");
                            tags = fields.getString("tags");
                        } else {
                            location = "";
                            tags = "";
                        }
                        
                        // Check if item is already in db
                        Item oldItem = Entity.query(Item.class).where(Query.eql("pk", id)).execute();
                        if(oldItem != null) {
                            // Update old item
                            oldItem.name = name;
                            oldItem.description = desc;
                            oldItem.date = date;
                            if(!isAvailable) {
                                oldItem.markAsClaimed();
                            } else {
                                oldItem.isAvailable = isAvailable;
                            }
                            oldItem.location = location;
                            oldItem.tags = tags;
                            oldItem.lat = lat;
                            oldItem.lon = lon;
                            oldItem.save();
                        } else {
                            // Add new item
                            Item item = new Item(id, name, desc, date, location, tags, isAvailable, lat, lon);
                            item.save();
                        }
                    }
                    // Set current time as new last update
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putLong("lastUpdate", new Date().getTime()).commit();
                    
                    return null;
                } else {
                    Log.e("Pull Error", String.valueOf(response.getStatusLine().getStatusCode()) + response.getStatusLine().getReasonPhrase());
                    return "An Error occured in item pull:\n" + response.getStatusLine().getReasonPhrase();
                }
            } catch (Exception e) {
                Log.i("Exception", e.getLocalizedMessage());
                return "An exception occured:\n" + e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result == null) {
                refreshItems();
            } else {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }
        }
        
        
        
    }
}
