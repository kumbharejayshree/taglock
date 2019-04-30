package com.tagloy.taglock.realmmodels;

import io.realm.RealmObject;

public class DeviceInformation extends RealmObject {
    private String device_name;
    private String latitudes;
    private String longitudes;
    private String device_group;
    private String android_version;
    private String device_Api_version;
    private Boolean device_locked_status;
    private Boolean hdmi_status;
    private String default_apk_version;
    private String taglock_version;
    private Boolean app_download_status;
    private Boolean taglock_download_status;
    private String ip_Address;
    private String mac_Address;
    private String device_Token;
    private String storage_memory;
    private String ram;
    private String device_expiry_date;
    private String box_Name;
    private Boolean wifi_status;
    private String updated_at;

    public String getLatitudes() {
        return latitudes;
    }

    public void setLatitudes(String latitudes) {
        this.latitudes = latitudes;
    }

    public String getLongitudes() {
        return longitudes;
    }

    public void setLongitudes(String longitudes) {
        this.longitudes = longitudes;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getDevice_group() {
        return device_group;
    }

    public void setDevice_group(String device_group) {
        this.device_group = device_group;
    }

    public String getAndroid_version() {
        return android_version;
    }

    public void setAndroid_version(String android_version) {
        this.android_version = android_version;
    }

    public String getDevice_Api_version() {
        return device_Api_version;
    }

    public void setDevice_Api_version(String device_Api_version) {
        this.device_Api_version = device_Api_version;
    }

    public Boolean getDevice_locked_status() {
        return device_locked_status;
    }

    public void setDevice_locked_status(Boolean device_locked_status) {
        this.device_locked_status = device_locked_status;
    }

    public Boolean getHdmi_status() {
        return hdmi_status;
    }

    public void setHdmi_status(Boolean hdmi_status) {
        this.hdmi_status = hdmi_status;
    }

    public String getDefault_apk_version() {
        return default_apk_version;
    }

    public void setDefault_apk_version(String default_apk_version) {
        this.default_apk_version = default_apk_version;
    }

    public String getTaglock_version() {
        return taglock_version;
    }

    public void setTaglock_version(String taglock_version) {
        this.taglock_version = taglock_version;
    }

    public Boolean getApp_download_status() {
        return app_download_status;
    }

    public void setApp_download_status(Boolean app_download_status) {
        this.app_download_status = app_download_status;
    }

    public Boolean getTaglock_download_status() {
        return taglock_download_status;
    }

    public void setTaglock_download_status(Boolean taglock_download_status) {
        this.taglock_download_status = taglock_download_status;
    }

    public String getIp_Address() {
        return ip_Address;
    }

    public void setIp_Address(String ip_Address) {
        this.ip_Address = ip_Address;
    }

    public String getMac_Address() {
        return mac_Address;
    }

    public void setMac_Address(String mac_Address) {
        this.mac_Address = mac_Address;
    }

    public String getDevice_Token() {
        return device_Token;
    }

    public void setDevice_Token(String device_Token) {
        this.device_Token = device_Token;
    }

    public String getStorage_memory() {
        return storage_memory;
    }

    public void setStorage_memory(String storage_memory) {
        this.storage_memory = storage_memory;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getDevice_expiry_date() {
        return device_expiry_date;
    }

    public void setDevice_expiry_date(String device_expiry_date) {
        this.device_expiry_date = device_expiry_date;
    }

    public String getBox_Name() {
        return box_Name;
    }

    public void setBox_Name(String box_Name) {
        this.box_Name = box_Name;
    }

    public Boolean getWifi_status() {
        return wifi_status;
    }

    public void setWifi_status(Boolean wifi_status) {
        this.wifi_status = wifi_status;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
