package com.tagloy.taglock.models;

import android.graphics.drawable.Drawable;

public class Permissions {
    public String id, name, description;
    public Drawable icon;
    public boolean grant;

    public Permissions(){}

    public Permissions(String name,String description, Drawable icon){
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGrant(boolean grant) {
        this.grant = grant;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
