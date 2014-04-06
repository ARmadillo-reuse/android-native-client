package com.example.reusemobile;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Application;

import com.example.reusemobile.model.Item;
import com.roscopeco.ormdroid.Entity;
import com.roscopeco.ormdroid.ORMDroidApplication;

public class GlobalApplication extends Application {
    
    private Integer[] ids = {1, 2, 3};
    private String[] items = {"Dell Desktop", "Random electronic stuff", "Free Dogecoin!!!"};
    private String[] descriptions = {"An old dell desktop. Missing HDD. Please take all.",
                                     "Box of random electric things. Floppy disks galore!",
                                     "Come see me if you want some free dogecoin. Much currency. Such value. Wow! This is an arbitrarily long string. AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHhhhh"};
    private String[] tags = {"computer desktop dell",
                             "capacitors floppy disks wires",
                             "dogecoin wow"};
    private String[] locations = {"32-123", "10-250", "16-676"};
    private Boolean[] available = {true, true, true};
    
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
                (new Item(ids[i], items[i], descriptions[i], new Date(), locations[i], tags[i], available[i])).save();
            }
        }
        
        // Create db cleaner
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            
            @Override
            public void run() {
                for(Item item : Entity.query(Item.class).executeMulti()) {
                    long elapsedMins = (new Date().getTime() - item.date.getTime()) / 60000;
                    if(!item.isAvailable && elapsedMins > 30) {
                        item.delete();
                    }
                }
            }
        };
        timer.schedule(task, 0, 60 * 1000); // Check every minute
    }
}