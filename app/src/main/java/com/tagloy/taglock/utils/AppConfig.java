package com.tagloy.taglock.utils;

public class AppConfig {
    //URLs used in Taglock

    //Preprod base URL for Taglock
//    public static final String BASE_URL = "http://13.232.206.57";
//
    // Preprod base URL for Taglock
//    public static final String BASE_URL = "https://prelockapi.tagloy.com";

    //Prod base URL for Taglock
    public static final String BASE_URL = "https://taglockapi.tagloy.com";

    //Teleperfomance base URL for Taglock
//    public static final String BASE_URL = "https://telelockapi.tagcx.com";

    //Localhost base URL
//    public static final String BASE_URL = "http://10.10.11.108/taglock-api";

    public static final String NAME_VALIDITY_URL = BASE_URL + "/device/validname";
    public static final String FCM_URL = BASE_URL + "/device/token";
    public static final String GROUP_URL = BASE_URL + "/groups/androidgroup";
    public static final String GROUP_VALIDITY_URL = BASE_URL + "/groups/validgroup";
    public static final String PROFILE_URL = BASE_URL + "/device/deviceprofile";
    public static final String INSERT_DEVICE_URL = BASE_URL + "/device/androiddevice";
    public static final String UPDATE_DEVICENAME_URL = BASE_URL + "/device/androidupdate";
    public static final String UPDATE_DEVICEID_URL = BASE_URL + "/device/deviceupdate";
    public static final String SESSION_URL = BASE_URL + "/device/session";
    public static final String CREDENTIALS_URL = BASE_URL + "/credentials/getcred";
//    public static final String GET_APK_URL = BASE_URL + "/apk/getapk";
//    public static final String GET_APK_URL = BASE_URL + "/apk/getandroid";
//    public static final String GET_TAGLOCK_URL = BASE_URL + "/taglock/gettaglock";
    public static final String GET_APK_URL = BASE_URL + "/apk/getapkandroid";
    public static final String GET_TAGLOCK_URL = BASE_URL + "/taglock/gettagandroid";
    public static final String APK_URI = BASE_URL + "/upload/apk/";
    public static final String TAGLOCK_URI = BASE_URL + "/upload/taglock/";
    public static final String WALLPAPER_URI = BASE_URL + "/upload/wallpaper/";

    //Shared Preference
    public static final String TAGLOCK_PREF = "taglock_pref";

    //Constants
    public static final String IS_ACTIVE = "taglock";
    public static final String IS_LOCKED = "is_locked";
    public static final String IS_NAV_VISIBLE = "nav";
    public static final String APK_VERSION = "apk_version";
    public static final String FAILED_COUNT = "failed_count";
    public static final String DEVICE_LAUNCHER = "device_launcher";
    public static final String APK_NAME = "apk_name";
    public static final String APK_DOWN_STATUS = "apk_down_status";
    public static final String INSTALL_STATUS = "install_status";
    public static final String UPDATE_STATUS = "update_status";
    public static final String HDMI_STATUS = "hdmi_status";
    public static final String GROUP_WALLPAPER = "group_wallpaper";
    public static final String IMAGE_ID = "img_id";
    public static final String TAGLOCK_DOWN_STATUS = "taglock_down_status";
    public static final String TAGLOCK_INSTALL_STATUS = "taglock_install_status";
    public static final String WALLPAPER_DOWN_STATUS = "wallpaper_down_status";
    public static final String TAGLOCK_APK = "taglock_apk";
    public static final String FCM_TOKEN = "fcm_token";
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_GROUP = "device_group";
    public static final String GROUP_ID = "group_id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String APP_DOWN_ID = "appid";
    public static final String TAGLOCK_DOWN_ID = "taglockid";
    public static final String WALLPAPER_DOWN_ID = "wallid";
}
