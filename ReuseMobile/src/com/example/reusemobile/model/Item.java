package com.example.reusemobile.model;

import java.util.Date;

import com.roscopeco.ormdroid.Entity;

public class Item extends Entity {
    public int _id;
    public String name;
    public String description;
    public Date date;
    public String location;
    public String tags;
    
    public Item() {
      this(null, null, null, null, null);
    }
    
    public Item(String name, String description, Date date, String location, String tags) {
      this.name = name;
      this.description = description;
      this.date = date;
      this.location = location;
      this.tags = tags;
    }
    
    public String toString() {
      return name;
    }
}