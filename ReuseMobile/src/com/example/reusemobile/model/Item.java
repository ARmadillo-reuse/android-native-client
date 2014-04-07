package com.example.reusemobile.model;

import java.util.Date;

import com.roscopeco.ormdroid.Entity;

public class Item extends Entity {
    public Integer id;
    public String name;
    public String description;
    public Date date;
    public String location;
    public String tags;
    public Boolean isAvailable;
    
    public Item() {
      this(null, null, null, null, null, null, null);
    }
    
    public Item(Integer id, String name, String description, Date date, String location, String tags, Boolean isAvailable) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.date = date;
      this.location = location;
      this.tags = tags;
      this.isAvailable = isAvailable;
      
      if(isAvailable != null && name != null && !isAvailable) {
          this.name = "[CLAIMED] " + this.name;
      }
    }
    
    public String toString() {
      return name;
    }
    
    public void markAsClaimed() {
        name = "[CLAIMED] " + name;
        isAvailable = false;
    }
}