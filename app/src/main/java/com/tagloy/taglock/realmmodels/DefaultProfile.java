package com.tagloy.taglock.realmmodels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DefaultProfile extends RealmObject {
    private String group_name;
    private String app_package_name;
    private boolean navigationbar_status;
    private boolean taglock_exited_status;
    private int passcode;
    private int clear_data_passcode;
    private int default_apk_call_duration;
    private String default_apk_version;

    public String getApp_package_name() {
        return app_package_name;
    }

    public void setApp_package_name(String app_package_name) {
        this.app_package_name = app_package_name;
    }

    public boolean isNavigationbar_status() {
        return navigationbar_status;
    }

    public void setNavigationbar_status(boolean navigationbar_status) {
        this.navigationbar_status = navigationbar_status;
    }

    public boolean isTaglock_exited_status() {
        return taglock_exited_status;
    }

    public void setTaglock_exited_status(boolean taglock_exited_status) {
        this.taglock_exited_status = taglock_exited_status;
    }

    public int getPasscode() {
        return passcode;
    }

    public void setPasscode(int passcode) {
        this.passcode = passcode;
    }

    public int getClear_data_passcode() {
        return clear_data_passcode;
    }

    public void setClear_data_passcode(int clear_data_passcode) {
        this.clear_data_passcode = clear_data_passcode;
    }

    public int getDefault_apk_call_duration() {
        return default_apk_call_duration;
    }

    public void setDefault_apk_call_duration(int default_apk_call_duration) {
        this.default_apk_call_duration = default_apk_call_duration;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getDefault_apk_version() {
        return default_apk_version;
    }

    public void setDefault_apk_version(String default_apk_version) {
        this.default_apk_version = default_apk_version;
    }
}
