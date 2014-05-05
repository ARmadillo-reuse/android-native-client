package com.example.reusemobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.example.reusemobile.logging.Sting;
import com.example.reusemobile.model.Item;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.roscopeco.ormdroid.Entity;
import com.roscopeco.ormdroid.Query;

public class MapView extends ActionBarActivity  {
//    private Map<String, LatLng> buildingLocations;
    private Map<Marker, Item> markerToItem;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        activity = this;
        
//        buildingLocations = new HashMap<String, LatLng>();
//        buildingLocations.put("32", new LatLng(42.361706,-71.090649));
//        buildingLocations.put("10", new LatLng(42.359625,-71.09199));
//        buildingLocations.put("16", new LatLng(42.360426,-71.090595));
        
        // Get a handle to the Map Fragment
        GoogleMap map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        
        LatLng mit = new LatLng(42.358953, -71.091634);
        
        List<Item> itemList = new ArrayList<Item>();
        if(getIntent().hasExtra(MainStream.ITEM_ID)) {
            int id = getIntent().getIntExtra(MainStream.ITEM_ID, 0);
            itemList.add(Entity.query(Item.class).where(Query.eql("pk", id)).execute());
        } else {
            // Get all filters from stream
            String[] filters = getIntent().getStringArrayExtra(MainStream.FILTERS);
            
            // Get all items that correspond with that filter

            for(Map<String, Object> datum : MainStream.getItems(filters)) {
                itemList.add((Item) datum.get("item"));
            }
        }

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mit, 14));
        map.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Sting.logButtonPush(activity, Sting.MAP_MARKER);
                return false;
            }
        });
        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            
            @Override
            public void onInfoWindowClick(Marker marker) {
                Item item = markerToItem.get(marker);
                Sting.logButtonPush(activity, Sting.MAP_DETAILS);
                displayItemDetails(item);
            }
        });

        markerToItem = new HashMap<Marker, Item>();
        // Add markers for each active item
        for(Item item : itemList) {
            if(item.isAvailable) {
                String building = item.location.split("-")[0];
                Log.i("Building", building);
                LatLng location;
                if(item.lat.equals("") || item.lon.equals("")) {
                    location = null;
                } else {
                    location = new LatLng(Double.parseDouble(item.lat), Double.parseDouble(item.lon));
                }
                if(location != null) {
                    Marker marker = map.addMarker(new MarkerOptions()
                                       .title(item.name)
                                       .snippet("Location: " + item.location)
                                       .position(location));
                    markerToItem.put(marker, item);
                }
            }
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
        getMenuInflater().inflate(R.menu.map_view, menu);
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
    
    private void displayItemDetails(Item item) {
        Intent intent = new Intent(this, ItemDetails.class);
        intent.putExtra(MainStream.ITEM_ID, item.pk);
        startActivity(intent);
    }

}
