package com.tagloy.taglock.models;

public class DeviceInfo {

    private  String id;
    private String latitudes;
    private String longitudes;
    private String device_name;
    private String device_group;
    private String android_version;
    private String device_Api_version;
    private Boolean device_locked_status;
    private String ip_Address;
    private String mac_Address;
    private String device_Token;
    private String storage_memory;
    private String ram;
    private String device_expiry_date;
    private String box_Name;
    private Boolean wifi_status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
}
