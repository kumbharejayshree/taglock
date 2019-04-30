package com.tagloy.taglock.realmmodels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SelectedAllAppDetails extends RealmObject {
    @PrimaryKey
    private String app_id;
    private String app_package_name;
    private boolean is_app_enable;
    private boolean app_visibility;
    private boolean is_Launcher_app;
    private String app_local_storage_path;

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getApp_package_name() {
        return app_package_name;
    }

    public void setApp_package_name(String app_package_name) {
        this.app_package_name = app_package_name;
    }

    public boolean isIs_app_enable() {
        return is_app_enable;
    }

    public void setIs_app_enable(boolean is_app_enable) {
        this.is_app_enable = is_app_enable;
    }

    public boolean isApp_visibility() {
        return app_visibility;
    }

    public void setApp_visibility(boolean app_visibility) {
        this.app_visibility = app_visibility;
    }

    public boolean isIs_Launcher_app() {
        return is_Launcher_app;
    }

    public void setIs_Launcher_app(boolean is_Launcher_app) {
        this.is_Launcher_app = is_Launcher_app;
    }

    public String getApp_local_storage_path() {
        return app_local_storage_path;
    }

    public void setApp_local_storage_path(String app_local_storage_path) {
        this.app_local_storage_path = app_local_storage_path;
    }
}
