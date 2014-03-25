package com.example.reusemobile.model;

import com.roscopeco.ormdroid.Entity;

public class Item extends Entity {
    public int _id;
    public String name;
    public String description;
    
    public Item() {
      this(null, null);
    }
    
    public Item(String name) {
        this(name, null);
    }
    
    public Item(String name, String description) {
      this.name = name;
      this.description = description;
    }
    
    public String toString() {
      return name;
    }
}