package com.example.reusemobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.reusemobile.logging.Sting;
import com.example.reusemobile.model.Item;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapView extends ActionBarActivity  {
//    private Map<String, LatLng> buildingLocations;
    private Map<Marker, Item> markerToItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        
//        buildingLocations = new HashMap<String, LatLng>();
//        buildingLocations.put("32", new LatLng(42.361706,-71.090649));
//        buildingLocations.put("10", new LatLng(42.359625,-71.09199));
//        buildingLocations.put("16", new LatLng(42.360426,-71.090595));
        
        // Get a handle to the Map Fragment
        GoogleMap map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        
        LatLng mit = new LatLng(42.358953, -71.091634);
        
        // Get all filters from stream
        String[] filters = getIntent().getStringArrayExtra(MainStream.FILTERS);
        
        // Get all items that correspond with that filter
        List<Item> itemList = new ArrayList<Item>();
        for(Map<String, Object> datum : MainStream.getItems(filters)) {
            itemList.add((Item) datum.get("item"));
        }

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mit, 14));
        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            
            @Override
            public void onInfoWindowClick(Marker marker) {
                Item item = markerToItem.get(marker);
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
                                       .snippet(item.description)
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
        intent.putExtra(MainStream.ITEM_NAME, item.name);
        intent.putExtra(MainStream.ITEM_DESCRIPTION, item.description);
        intent.putExtra(MainStream.ITEM_DATE, item.date.getTime());
        intent.putExtra(MainStream.ITEM_LOCATION, item.location);
        intent.putExtra(MainStream.ITEM_AVAILABLE, item.isAvailable);
        startActivity(intent);
    }

}
