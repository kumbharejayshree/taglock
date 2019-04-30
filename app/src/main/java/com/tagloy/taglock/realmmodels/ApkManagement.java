package com.tagloy.taglock.realmmodels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ApkManagement extends RealmObject {
    @PrimaryKey
    private String id;
    private String app_name;
    private String app_package_name;
    private String app_version;
    private String app_local_storage_path;
    private Boolean is_latest_apk_installed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getApp_package_name() {
        return app_package_name;
    }

    public void setApp_package_name(String app_package_name) {
        this.app_package_name = app_package_name;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getApp_local_storage_path() {
        return app_local_storage_path;
    }

    public void setApp_local_storage_path(String app_local_storage_path) {
        this.app_local_storage_path = app_local_storage_path;
    }

    public Boolean getIs_latest_apk_installed() {
        return is_latest_apk_installed;
    }

    public void setIs_latest_apk_installed(Boolean is_latest_apk_installed) {
        this.is_latest_apk_installed = is_latest_apk_installed;
    }
}
