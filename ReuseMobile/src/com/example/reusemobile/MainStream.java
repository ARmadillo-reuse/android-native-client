package com.example.reusemobile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateFormat;
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

import com.example.reusemobile.logging.Sting;
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
    private final static int NOTIFICATION_ID = 12345;
    
    public final static String ITEM_ID = "com.example.reusemobile.ID";
    public final static String ITEM_NAME = "com.example.reusemobile.ITEM_NAME";
    public final static String ITEM_DESCRIPTION = "com.example.reusemobile.ITEM_DESCRIPTION";
    public final static String ITEM_DATE = "com.example.reusemobile.ITEM_DATE";
    public final static String ITEM_LOCATION = "com.example.reusemobile.ITEM_LOCATION";
    public final static String ITEM_AVAILABLE = "com.example.reusemobile.ITEM_AVAILABLE";
    public final static String NOTIFICATION_FILTER = "com.example.reusemobile.NOTIFICATION_FILTER";
    public final static String FILTERS = "com.example.reusemobile.FILTERS";
    public final static String PULL_ACTION = "com.example.reusemobile.PULL";
    
    public DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mDrawerToggle;
    public ListView itemList;
    public Drawer drawer;
    public SwipeRefreshLayout refreshLayout;
    
    private static Context appContext;
    
    private String[] currentKeywords;
    private Timer timer = new Timer();
    private int refreshInterval = 30 * 1000; // Refresh every 30 seconds
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
    private int pullInterval = 30 * 60 * 1000; // Pull every 30 minutes
    private TimerTask pullFromServerTask = new TimerTask() {
        @Override
        public void run() {
            Log.i("Pull", "Pulling from server");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pullFromServerAndUpdate();
                    refreshItems();
                }
            });
        }
    };
    private PullReceiver receiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new PullReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PULL_ACTION);
        registerReceiver(receiver, filter);
        
        // Check if user is verified
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isVerified", false)) {
            startActivity(new Intent(this, CreateAccount.class));
            finish();
        } else {
            // Set a timer to update itemList
            timer.schedule(refreshTask, 0, refreshInterval);
            timer.schedule(pullFromServerTask, 0, pullInterval);
            

        }
        
        setContentView(R.layout.activity_main_stream);
        appContext = getApplicationContext();
        
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
            
            // Setup Swipe to Refresh Listener
            refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //refreshLayout.setRefreshing(true);
                    pullFromServerAndUpdate();
                }
            });
            refreshLayout.setColorScheme(R.color.mit_red,
                                         R.color.mit_gray,
                                         R.color.mit_red,
                                         R.color.mit_gray);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Sting.logActivityStart(this);
        // Check if user is verified
        if(checkPlayServices()) {
            if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isVerified", false)) {
                startActivity(new Intent(this, CreateAccount.class));
                finish();
            } else {
                pullFromServerAndUpdate();
                refreshItems();
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                // Close the drawer
                Sting.logButtonPush(this, Sting.DRAWER_CLOSE_MENU_BUTTON);
                mDrawerLayout.closeDrawers();
            } else {
                // Open the drawer
                Sting.logButtonPush(this, Sting.DRAWER_OPEN_MENU_BUTTON);
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
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                Sting.logButtonPush(this, Sting.DRAWER_CLOSE_BUTTON);
            } else {
                Sting.logButtonPush(this, Sting.DRAWER_OPEN_BUTTON);
            }
            return true;
        }
        
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_map_view:
            Intent intent = new Intent(this, MapView.class);
            intent.putExtra(FILTERS, currentKeywords);
            Sting.logButtonPush(this, Sting.ACTION_MAP);
            startActivity(intent);
            return true;
        case R.id.action_search:
            return true;
        case R.id.action_settings:
            return super.onOptionsItemSelected(item);
        case R.id.action_new_post:
            Sting.logButtonPush(this, Sting.ACTION_NEW_POST);
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
        setTitle("All Items");
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
        setTitle("Filtered Items");
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
                //datum.put("description", item.description);
                StringBuilder details = new StringBuilder();
                if (!item.location.equals("")) details.append("Location: " + item.location + "\n");
                details.append(DateFormat.format("hh:mm:ssa EEEE MMM d, yyyy", item.date));
                datum.put("description", details.toString());
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
                //datum.put("description", item.description);
                StringBuilder details = new StringBuilder();
                if (!item.location.equals("")) details.append("Location: " + item.location + "\n");
                details.append(DateFormat.format("hh:mm:ssa EEEE MMM d, yyyy", item.date));
                datum.put("description", details.toString());
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
            setTitle("Filtered Items");
            Sting.logButtonPush(this, Sting.DISPLAY_SEARCH + " " + Arrays.toString(currentKeywords));
            refreshItems();
        } else if(intent.hasExtra(NOTIFICATION_FILTER)){
            String filter = intent.getStringExtra(NOTIFICATION_FILTER);
            applyFilter(filter);
        } else {
            showAll();
        }
    }
    
    /**
     * Ensures the app can connect to the network service and issued
     * the Async pull request.
     */
    public void pullFromServerAndUpdate() {
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
    
    private void processItemUpdate(Integer pk, String name, String description,
            Date date, String location, String tags, Boolean isAvailable, String lat, String lon) {
        // Check if item is already in db
        Item oldItem = Entity.query(Item.class).where(Query.eql("pk", pk)).execute();
        if(oldItem != null) {
            // Update old item
            oldItem.name = name;
            oldItem.description = description;
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
            Item item = new Item(pk, name, description, date, location, tags, isAvailable, lat, lon);
            item.save();
            checkIfShouldNotify(item);
        }
    }
    
    @SuppressLint("NewApi")
    private void checkIfShouldNotify(Item item) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getBoolean("notifications_new_item", true)) {
            
            Set<String> notifyFilters = pref.getStringSet("notifications_filters", new HashSet<String>());
            for(String filter : notifyFilters) {
                String[] tags = getSharedPreferences(GlobalApplication.filterPreferences, Context.MODE_PRIVATE).getString(filter, "").split(" ");
                for(String tag : tags) {
                    if(item.name.toLowerCase(Locale.ENGLISH).contains(tag) ||
                       item.description.toLowerCase(Locale.ENGLISH).contains(tag) ||
                       item.tags.toLowerCase(Locale.ENGLISH).contains(tag) ||
                       item.location.toLowerCase(Locale.ENGLISH).contains(tag)) {
                        
                        //Sting.logNotificationEvent((Activity) appContext, filter);
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("New '" + filter + "' Item Available!")
                                .setContentText("Click here to view new item")
                                .setAutoCancel(true);
                        // Set notification settings
                        if(pref.getBoolean("notifications_new_item_vibrate", true)) mBuilder.setVibrate(new long[] {0, 500});
                        mBuilder.setSound(Uri.parse(pref.getString("notifications_new_item_ringtone", "content://settings/system/notification_sound")));
                        
                        // Creates an explicit intent for an Activity in your app
                        Intent resultIntent = new Intent(this, MainStream.class);
                        resultIntent.putExtra(NOTIFICATION_FILTER, filter);
    
                        PendingIntent resultPendingIntent;
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            // The stack builder object will contain an artificial back stack for the
                            // started Activity.
                            // This ensures that navigating backward from the Activity leads out of
                            // your application to the Home screen.
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                            // Adds the back stack for the Intent (but not the Intent itself)
                            stackBuilder.addParentStack(ItemDetails.class);
                            // Adds the Intent that starts the Activity to the top of the stack
                            stackBuilder.addNextIntent(resultIntent);
                            resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    );
                        } else {
                            resultPendingIntent = PendingIntent.getActivity(
                                            this,
                                            0,
                                            resultIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        );
                        }

                        mBuilder.setContentIntent(resultPendingIntent);
                        NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        // mId allows you to update the notification later on.
                        mNotificationManager.notify(NOTIFICATION_ID + filter.hashCode(), mBuilder.build());
                    }
                }
            }
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
        @Override
        protected String doInBackground(Long... params) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'+'HH:mm", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            String lastUpdate = formatter.format(new Date(params[0]));
            Log.i("last update", lastUpdate);
            
            String port = GlobalApplication.getServerPort();
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://armadillo.xvm.mit.edu:" + port + "/api/thread/get/?after=" + lastUpdate);
            String email = PreferenceManager.getDefaultSharedPreferences(appContext).getString("username", "");
            httpget.addHeader("USERNAME", email);
            String token = PreferenceManager.getDefaultSharedPreferences(appContext).getString("token", "");
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
                        df.setTimeZone(TimeZone.getTimeZone("GMT"));
                        Date date = df.parse(fields.getString("modified"));
                        
                        // Get if available, lat, and long
                        Boolean isAvailable = !fields.getBoolean("claimed");
                        String lat = fields.getString("lat");
                        String lon = fields.getString("lon");
                        
                        String location = fields.getString("location");
                        String tags = fields.getString("tags");
                        
                        processItemUpdate(id, name, desc, date, location, tags, isAvailable, lat, lon);
                    }
                    // Set current time as new last update
                    PreferenceManager.getDefaultSharedPreferences(appContext).edit().putLong("lastUpdate", new Date().getTime()).commit();
                    
                    return null;
                } else {
                    //Sting.logError((Activity) appContext, Sting.PULL_ERROR,
                    //        String.valueOf(response.getStatusLine().getStatusCode()) + response.getStatusLine().getReasonPhrase());
                    Log.e("Pull Error", String.valueOf(response.getStatusLine().getStatusCode()) + response.getStatusLine().getReasonPhrase());
                    return "An Error occured in item pull:\n" + response.getStatusLine().getReasonPhrase();
                }
            } catch (Exception e) {
                //Sting.logError((Activity) appContext, Sting.PULL_ERROR, "Exception: " + e.getLocalizedMessage());
                Log.i("Exception", e.getLocalizedMessage());
                return "An exception occured:\n" + e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result == null) {
                refreshItems();
                if(refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
            } else {
                if(GlobalApplication.isDebug()) Toast.makeText(appContext, result, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private class PullReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            pullFromServerAndUpdate();
        }
        
    }
}
