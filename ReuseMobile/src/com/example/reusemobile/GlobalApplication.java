package com.example.reusemobile;

import java.util.List;

import android.app.Application;

import com.example.reusemobile.model.Item;
import com.roscopeco.ormdroid.Entity;
import com.roscopeco.ormdroid.ORMDroidApplication;

public class GlobalApplication extends Application {
    
    private String[] items = {"Item 1", "Item 2", "Item 3"};

    @Override
    public void onCreate() {
        super.onCreate();
        ORMDroidApplication.initialize(this);
        // Empty db
        List<Item> previousEntries = Entity.query(Item.class).executeMulti();
        if (previousEntries.size() == 0) {
            // Insert new items
            for (String item : items) {
                (new Item(item, "Short Description")).save();
            }
        }
    }
}