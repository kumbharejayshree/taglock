package com.tagloy.taglock.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Apps extends RealmObject {

    @PrimaryKey
    private int id;

    private int allow_code;
    private String app_name, app_package;
    private byte[] app_icon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getApp_package() {
        return app_package;
    }

    public void setApp_package(String app_package) {
        this.app_package = app_package;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public byte[] getApp_icon() {
        return app_icon;
    }

    public void setApp_icon(byte[] app_icon) {
        this.app_icon = app_icon;
    }

    public int getAllow_code() {
        return allow_code;
    }

    public void setAllow_code(int allow_code) {
        this.allow_code = allow_code;
    }
}
