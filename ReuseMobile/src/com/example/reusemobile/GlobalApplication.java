package com.example.reusemobile;

import java.util.Date;
import java.util.List;

import android.app.Application;

import com.example.reusemobile.model.Item;
import com.roscopeco.ormdroid.Entity;
import com.roscopeco.ormdroid.ORMDroidApplication;

public class GlobalApplication extends Application {
    
    private String[] items = {"Dell Desktop", "Random electronic stuff", "Free Dogecoin!!!"};
    private String[] descriptions = {"An old dell desktop. Missing HDD. Please take all.",
                                     "Box of random electric things. Floppy disks galore!",
                                     "Come see me if you want some free dogecoin. Much currency. Such value. Wow!"};
    public static String filterPreferences = "com.example.reuse.filters";

    @Override
    public void onCreate() {
        super.onCreate();
        ORMDroidApplication.initialize(this);
        // Empty db
        List<Item> previousEntries = Entity.query(Item.class).executeMulti();
        if (previousEntries.size() == 0) {
            // Insert new items
            for (int i = 0; i < 3; i++) {
                (new Item(items[i], descriptions[i], new Date(), "32-123")).save();
            }
        }
    }
}