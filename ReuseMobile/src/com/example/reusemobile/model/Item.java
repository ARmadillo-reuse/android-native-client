package com.example.reusemobile.model;

import java.util.Date;

import com.roscopeco.ormdroid.Entity;

public class Item extends Entity {
    public Integer id;
    public Integer pk;
    public String name;
    public String description;
    public Date date;
    public String location;
    public String tags;
    public Boolean isAvailable;
    public String lat;
    public String lon;
    public String sender;
    
    public Item() {
      this(null, null, null, null, null, null, null, null, null, null);
    }
    
    public Item(Integer pk, String name, String description, Date date, String location, String tags, Boolean isAvailable, String lat, String lon, String sender) {
      this.pk = pk;
      this.name = name;
      this.description = description;
      this.date = date;
      this.location = location;
      this.tags = tags;
      this.isAvailable = isAvailable;
      this.lat = lat;
      this.lon = lon;
      this.sender = sender;
      
      if(isAvailable != null && name != null && !isAvailable) {
          this.name = "<font color='#858789'>Claimed: " + this.name + "</font>";
      }
    }
    
    public String toString() {
      return name;
    }
    
    public void markAsClaimed() {
        name = "<font color='#858789'>Claimed: " + this.name + "</font>";
        isAvailable = false;
    }
}