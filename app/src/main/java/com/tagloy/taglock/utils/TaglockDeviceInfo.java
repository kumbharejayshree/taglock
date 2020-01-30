package com.tagloy.taglock.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tagloy.taglock.R;
import com.tagloy.taglock.activity.MainActivity;
import com.tagloy.taglock.realmcontrollers.DefaultProfileController;
import com.tagloy.taglock.realmcontrollers.DeviceInfoController;
import com.tagloy.taglock.realmmodels.DefaultProfile;
import com.tagloy.taglock.realmmodels.DeviceInformation;
import com.tagloy.taglock.receiver.HdmiListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.realm.RealmResults;

import static android.content.Context.DOWNLOAD_SERVICE;

public class TaglockDeviceInfo {

    private Context context;
    private WindowManager windowManager;
    private customViewGroup view;
    private SuperClass superClass;
    private long wallId;

    public TaglockDeviceInfo(Context context) {
        this.context = context;
    }

    //To get default launcher of the device
    public void getLauncher() {
        PackageManager pm = context.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> lst = pm.queryIntentActivities(i, 0);
        String packageName = lst.get(0).activityInfo.packageName;
        PreferenceHelper.setValueString(context, AppConfig.DEVICE_LAUNCHER, packageName);
    }

    //To check if device is connected to the network
    public boolean isNetworkConnected() {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    //To check if device is connected to WiFi
    public Boolean isWifiConnected() {
        if (isNetworkConnected()) {
            ConnectivityManager cm
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI);
        }
        return false;
    }

    //To check if device is connected to LAN
    public Boolean isEthernetConnected() {
        if (isNetworkConnected()) {
            ConnectivityManager cm
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_ETHERNET);
        }
        return false;
    }

    //Get version of the mentioned package
    public static String getVersion(Context context1,String packageName) {
        String versionName = "";
        try {
            PackageInfo packageInfo = context1.getPackageManager().getPackageInfo(packageName, 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException pe) {
            pe.printStackTrace();
        }
        return versionName;
    }

    //To get device IP address for WiFI
    public Integer getWifiIp() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

    //To get device IP address for LAN
    public String getLANIp(){
        String ipAddress = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ipAddress = inetAddress.getHostAddress();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {}
        return null;
    }

    //To get device MAC address
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    //To check if device name entered by user is unique
    public void checkNameValidity(final String deviceName) {
        if (isNetworkConnected()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("device_name", deviceName);
                final String request = jsonObject.toString();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.NAME_VALIDITY_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject name = new JSONObject(response);
                            String status = name.getString("status");
                            if (status.equals("200")) {
                                Toast.makeText(context, "Name already exists", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        PreferenceHelper.setValueString(context, AppConfig.DEVICE_NAME, deviceName);
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("Content-Type", "application/json");
                        return parameter;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return request == null ? null : request.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", request, "utf-8");
                            return null;
                        }
                    }
                };
                queue.add(stringRequest);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Please check network connection", Toast.LENGTH_LONG).show();
        }
    }

    //To check validity of group key entered by user
    public void checkGroupKey(final String deviceName, final String groupId, final String groupKey) {
        if (isNetworkConnected()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("group_id", groupId);
                jsonObject.put("group_key", groupKey);
                final String request = jsonObject.toString();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GROUP_VALIDITY_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject name = new JSONObject(response);
                            String status = name.getString("status");
                            if (status.equals("200")) {
                                PreferenceHelper.setValueString(context, AppConfig.GROUP_ID, groupId);
                                checkNameValidity(deviceName);
                            } else if (status.equals("404")) {
                                Toast.makeText(context, "Group key is invalid. Please check!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("API", "Error");
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("Content-Type", "application/json");
                        return parameter;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return request == null ? null : request.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", request, "utf-8");
                            return null;
                        }
                    }
                };
                queue.add(stringRequest);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Please check network connection", Toast.LENGTH_LONG).show();
        }
    }

    //Get group name of the device
    public void getGroup(String groupId){
        if (isNetworkConnected()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("group_id", groupId);
                final String request = jsonObject.toString();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GROUP_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject name = new JSONObject(response);
                            String groupName = name.getString("group_name");
                            PreferenceHelper.setValueString(context, AppConfig.DEVICE_GROUP, groupName);
                            applyProfile(groupName);
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("API", "Error");
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("Content-Type", "application/json");
                        return parameter;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return request == null ? null : request.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", request, "utf-8");
                            return null;
                        }
                    }
                };
                queue.add(stringRequest);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Please check network connection", Toast.LENGTH_LONG).show();
        }
    }

    public void setTimeZone(String timeZone){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setTimeZone(TimeZone.getTimeZone(timeZone).getID());
    }

    public void getTimeZone() {
        TimeZone tz = TimeZone.getDefault();
    }

    public void getCreds(){
        if (isNetworkConnected()) {
            try {
                String device_name = PreferenceHelper.getValueString(context,AppConfig.DEVICE_NAME);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("device_name", device_name);
                final String request = jsonObject.toString();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.CREDENTIALS_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject name = new JSONObject(response);
                            String username = name.getString("username");
                            String password = name.getString("password");
                            String data = username + "\n" + password;
                            createFile(data);
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("API", "Error");
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("Content-Type", "application/json");
                        return parameter;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return request == null ? null : request.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", request, "utf-8");
                            return null;
                        }
                    }
                };
                queue.add(stringRequest);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Please check network connection", Toast.LENGTH_LONG).show();
        }
    }

    //
    public void createFile(String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "/.taglock/.app");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "appdata");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //To get memory of the device
    public String checkMemory() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        float bytesAvailable, bytesTotal;
        bytesTotal = (statFs.getBlockSizeLong() * statFs.getBlockCountLong());
        bytesAvailable = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
        float free = bytesAvailable / (1024 * 1024 * 1024);
        float used = (bytesTotal - bytesAvailable) / (1024 * 1024);
        float total = bytesTotal / (1024 * 1024 * 1024);
        String freeMemory = String.format("%.02f",free);
        String usedMemory = String.format("%.02f",used);
        String totalMemory = String.format("%.02f",total);
        String memory = usedMemory + "MB/" + freeMemory + "GB/" + totalMemory + "GB";
        return memory;
    }

    //To get RAM of the device
    public String checkRAM() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalMemory = ((memoryInfo.totalMem / 1024) / 1024);
        String ram = totalMemory + "MB";
        return ram;
    }

    //To check if wifi is connected or not
    public boolean checkWifi() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    //Get Manufacturer of the device
    public String getBoxName() {
        StringBuilder builder = new StringBuilder();
        String model = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;
        builder.append(manufacturer).append(" ").append(model);
        return builder.toString();
    }

    //Get Android version of the device
    public String getBoxAndroid() {
        StringBuilder builder = new StringBuilder();
        Field[] fields = Build.VERSION_CODES.class.getFields();
        String OS = Build.VERSION.RELEASE;
        String osName = "";
        for (Field field : fields) {
            osName = field.getName();
        }
        builder.append(osName).append(" ").append(OS);
        return builder.toString();
    }

    //Get API version of the Android version
    public String getBoxApi() {
        StringBuilder builder = new StringBuilder();
        Field[] fields = Build.VERSION_CODES.class.getFields();
        int api = -1;
        for (Field field : fields) {
            try {
                api = field.getInt(new Object());
            } catch (IllegalAccessException ie) {
                ie.printStackTrace();
            }
        }
        builder.append(api);
        return builder.toString();
    }

    public String getDeviceTime() {
        String IST = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").format(new Date());
        return IST;
    }

    //Get profile details and apply profile to the device
    public void applyProfile(String group_name) {
        if (isNetworkConnected()){
            final DefaultProfileController defaultProfileController = new DefaultProfileController();
            final DefaultProfile defaultProfile = new DefaultProfile();
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("group_name", group_name);
                final String request = jsonObject.toString();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.PROFILE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject profile = jsonArray.getJSONObject(i);
                                defaultProfile.setApp_package_name(profile.getString("apk_package"));
                                defaultProfile.setTaglock_exited_status("1".equals(profile.getString("taglock_status")));
                                defaultProfile.setNavigationbar_status("1".equals(profile.getString("nav_status")));
                                defaultProfile.setPasscode(profile.getInt("passcode"));
                                defaultProfile.setClear_data_passcode(profile.getInt("clear_passcode"));
                                defaultProfile.setDefault_apk_call_duration(profile.getInt("default_apk_call"));
                                defaultProfile.setGroup_name(profile.getString("group_name"));
                                defaultProfile.setDefault_apk_version(profile.getString("apk_version"));
                                boolean profileRealm = defaultProfileController.isAvailablProfileData();
                                if (profileRealm) {
                                    defaultProfileController.updateProfileDataContent(defaultProfile);
                                } else {
                                    defaultProfileController.addDefaultProfileData(defaultProfile);
                                }
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("Content-Type", "application/json");
                        return parameter;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return request == null ? null : request.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", request, "utf-8");
                            return null;
                        }
                    }
                };
                queue.add(stringRequest);
            }catch (JSONException je) {
                je.printStackTrace();
            }
        }else {
            Log.d("Network Status", "Not connected");
        }
    }

    //For first entry of the device
    public void deviceDetails(final DeviceInformation deviceInformation) {
        if (isNetworkConnected()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("device_name", deviceInformation.getDevice_name());
                jsonObject.put("device_group", deviceInformation.getDevice_group());
                jsonObject.put("latitude", deviceInformation.getLatitudes());
                jsonObject.put("longitude", deviceInformation.getLongitudes());
                jsonObject.put("box_name", deviceInformation.getBox_Name());
                jsonObject.put("box_android", deviceInformation.getAndroid_version());
                jsonObject.put("box_api", deviceInformation.getDevice_Api_version());
                jsonObject.put("device_locked", deviceInformation.getDevice_locked_status());
                jsonObject.put("hdmi_status", deviceInformation.getHdmi_status());
                jsonObject.put("default_apk_version", deviceInformation.getDefault_apk_version());
                jsonObject.put("taglock_version", deviceInformation.getTaglock_version());
                jsonObject.put("app_download_status", deviceInformation.getApp_download_status());
                jsonObject.put("taglock_download_status", deviceInformation.getTaglock_download_status());
                jsonObject.put("ip_address", deviceInformation.getIp_Address());
                jsonObject.put("wifimac_address", deviceInformation.getWifimac_Address());
                jsonObject.put("lanmac_address", deviceInformation.getLanimac_Address());
                jsonObject.put("memory", deviceInformation.getStorage_memory());
                jsonObject.put("ram", deviceInformation.getRam());
                jsonObject.put("device_token", deviceInformation.getDevice_Token());
                jsonObject.put("wifi_status", deviceInformation.getWifi_status());
                jsonObject.put("updated_at", String.valueOf(System.currentTimeMillis() / 1000));
                final String request = jsonObject.toString();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.INSERT_DEVICE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("status");
                            if (res.equals("201")) {
                                Log.d("Success: ", "Device details inserted");
                                int id = jsonObject.getInt("id");
                                Log.d("Id", String.valueOf(id));
                                PreferenceHelper.setValueInt(context, AppConfig.DEVICE_ID, id);
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Failure: ", "Device details not inserted");
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("Content-Type", "application/json");
                        return parameter;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return request == null ? null : request.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", request, "utf-8");
                            return null;
                        }
                    }
                };
                queue.add(stringRequest);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            Log.d("Network Status", "Not connected");
        }
    }

    //Update device data
    public void updateDevice(final DeviceInformation deviceInformation) {
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        final DeviceInfoController deviceInfoController = new DeviceInfoController();
        final DeviceInformation deviceInformation1 = new DeviceInformation();
        final DefaultProfile defaultProfile = new DefaultProfile();
        if (isNetworkConnected()) {
            try {
                final int id = PreferenceHelper.getInt(context,AppConfig.DEVICE_ID);
                String device_name = PreferenceHelper.getString(context,AppConfig.DEVICE_NAME);
                String device_group = PreferenceHelper.getString(context,AppConfig.DEVICE_GROUP);
                JSONObject jsonObject = new JSONObject();
                String url;
                if (id != 0){
                    jsonObject.put("id",id);
                    url = AppConfig.UPDATE_DEVICEID_URL;
                }else {
                    jsonObject.put("device_name",device_name);
                    jsonObject.put("device_group", device_group);
                    url = AppConfig.UPDATE_DEVICENAME_URL;
                }
//                jsonObject.put("device_name", deviceInformation.getDevice_name());
//                jsonObject.put("device_group", deviceInformation.getDevice_group());
                jsonObject.put("latitude", deviceInformation.getLatitudes());
                jsonObject.put("longitude", deviceInformation.getLongitudes());
                jsonObject.put("box_name", deviceInformation.getBox_Name());
                jsonObject.put("box_android", deviceInformation.getAndroid_version());
                jsonObject.put("box_api", deviceInformation.getDevice_Api_version());
                jsonObject.put("device_locked", deviceInformation.getDevice_locked_status());
                jsonObject.put("hdmi_status", deviceInformation.getHdmi_status());
                jsonObject.put("default_apk_version", deviceInformation.getDefault_apk_version());
                jsonObject.put("taglock_version", deviceInformation.getTaglock_version());
                jsonObject.put("app_download_status", deviceInformation.getApp_download_status());
                jsonObject.put("taglock_download_status", deviceInformation.getTaglock_download_status());
                jsonObject.put("ip_address", deviceInformation.getIp_Address());
                jsonObject.put("wifimac_address", deviceInformation.getWifimac_Address());
                jsonObject.put("lanmac_address", deviceInformation.getLanimac_Address());
                jsonObject.put("memory", deviceInformation.getStorage_memory());
                jsonObject.put("ram", deviceInformation.getRam());
                jsonObject.put("device_token", deviceInformation.getDevice_Token());
                jsonObject.put("wifi_status", deviceInformation.getWifi_status());
                jsonObject.put("updated_at", String.valueOf(System.currentTimeMillis() / 1000));
                final String request = jsonObject.toString();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            if (res.equals("200")) {
                                Log.d("Success ", message);
                                if (id != 0){
                                    String result = jsonObject.getString("result");
                                    JSONObject resultObject = new JSONObject(result);
                                    int device_id = resultObject.getInt("id");
                                    String device_name = resultObject.getString("device_name");
                                    String device_group = resultObject.getString("device_group");
                                    String group_image = resultObject.getString("group_image");
                                    int exit_passcode = resultObject.getInt("passcode");
                                    int clear_passcode = resultObject.getInt("clear_passcode");
                                    int call_duration = resultObject.getInt("default_apk_call");
                                    String package_name = resultObject.getString("apk_package");
                                    String device_n = PreferenceHelper.getValueString(context,AppConfig.DEVICE_NAME);
                                    String device_g = PreferenceHelper.getValueString(context,AppConfig.DEVICE_GROUP);
                                    String group_i = PreferenceHelper.getString(context,AppConfig.GROUP_WALLPAPER);
                                    if (group_i!= null){
                                        PreferenceHelper.setValueString(context,AppConfig.GROUP_WALLPAPER,group_image);
                                        File imageFile = new File("/storage/emulated/0/.taglock/" + group_image);
                                        if(!imageFile.exists())
                                            downloadWallpaper(group_image);
                                    }else if(group_image != null){
                                        PreferenceHelper.setValueString(context,AppConfig.GROUP_WALLPAPER,group_image);
                                        downloadWallpaper(group_image);
                                    }
                                    if (!device_n.equals(device_name) || !device_g.equals(device_group)){
                                        deviceInformation1.setDevice_name(device_name);
                                        deviceInformation1.setDevice_group(device_group);
                                        deviceInfoController.updateDevice(deviceInformation1);
                                        PreferenceHelper.setValueString(context,AppConfig.DEVICE_NAME,device_name);
                                        PreferenceHelper.setValueString(context,AppConfig.DEVICE_GROUP,device_group);
                                        Log.d("Realm Device","Updated");
                                    }
                                    if (!device_g.equals(device_group)){
                                        defaultProfile.setPasscode(exit_passcode);
                                        defaultProfile.setGroup_name(device_group);
                                        defaultProfile.setClear_data_passcode(clear_passcode);
                                        defaultProfileController.updateProfile(defaultProfile);
                                        defaultProfile.setDefault_apk_call_duration(call_duration);
                                        defaultProfile.setApp_package_name(package_name);
                                        defaultProfileController.updateProfileData(defaultProfile);
                                        PreferenceHelper.setValueString(context,AppConfig.DEVICE_GROUP,device_group);
                                        Log.d("Realm Profile","Updated");
                                    }
                                    Log.d("Id", String.valueOf(device_id));
                                    Log.d("Name", device_name);
                                    Log.d("Group", device_group);
                                }else{
                                    String result = jsonObject.getString("id");
                                    JSONObject resultObject = new JSONObject(result);
                                    int id = resultObject.getInt("id");
                                    PreferenceHelper.setValueInt(context,AppConfig.DEVICE_ID,id);
                                }

                            } else {
                                Log.d("Failure ", message);
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("onErrorResponse: ", error.toString());
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("Content-Type", "application/json");
                        return parameter;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return request == null ? null : request.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", request, "utf-8");
                            return null;
                        }
                    }
                };
                queue.add(stringRequest);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            Log.d("Network Status", "Not connected");
        }
    }

    //Download wallpaper if uploaded by user
    public void downloadWallpaper(String fileName){
        Uri uri = Uri.parse(AppConfig.WALLPAPER_URI + fileName);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setVisibleInDownloadsUi(false);
        String taglockPath = "/.taglock/";
        request.setDestinationInExternalPublicDir(taglockPath, uri.getLastPathSegment());
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        wallId = downloadManager.enqueue(request);
        PreferenceHelper.setValueString(context,AppConfig.WALLPAPER_DOWN_ID,String.valueOf(wallId));
        DownloadManager.Query query = null;
        query = new DownloadManager.Query();
        Cursor cursor = null;
        if (query != null) {
            query.setFilterByStatus(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_SUCCESSFUL | DownloadManager.STATUS_PAUSED |
                    DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
        }
        cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (status == DownloadManager.STATUS_FAILED) {
                PreferenceHelper.setValueBoolean(context, AppConfig.TAGLOCK_DOWN_STATUS, false);
                if (isNetworkConnected()) {
                    Log.d("Network Status", "Connected");
                } else {
                    Log.d("Network Status", "No network");
                }
            }
        }
    }

    //Receiver for completion of download
    public BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == wallId) {
                PreferenceHelper.setValueBoolean(context, AppConfig.WALLPAPER_DOWN_STATUS, true);
            }
        }
    };

    //Session management
    public void deviceSession(final DeviceInformation deviceInformation) {
        if (isNetworkConnected()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("device_name", deviceInformation.getDevice_name());
                jsonObject.put("device_group", deviceInformation.getDevice_group());
                jsonObject.put("hdmi_status", deviceInformation.getHdmi_status());
                jsonObject.put("device_locked", deviceInformation.getDevice_locked_status());
                final String request = jsonObject.toString();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.SESSION_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            if (res.equals("201")) {
                                Log.d("Session Success", message);
                            } else {
                                Log.d("Session Failure ", message);
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("onErrorResponse: ", error.toString());
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("Content-Type", "application/json");
                        return parameter;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return request == null ? null : request.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", request, "utf-8");
                            return null;
                        }
                    }
                };
                queue.add(stringRequest);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            Log.d("Network Status", "Not connected");
        }
    }

    @SuppressLint("DefaultLocale")
    public String intToIp(int data){
        return String.format("%d.%d.%d.%d", (data & 0xff), (data >> 8 & 0xff), (data >> 16 & 0xff), (data >> 24 & 0xff));
    }

    //Get basic device information
    public DeviceInformation updateDetails(){
        superClass = new SuperClass(context);
        String ip;
        DeviceInformation deviceInformation = new DeviceInformation();
        if (isWifiConnected()){
            Integer ipAddress = getWifiIp();
            ip = intToIp(ipAddress);
        }else if (isEthernetConnected()){
            ip = getLANIp();
        }else {
            ip = "NA";
        }
        String latitude = PreferenceHelper.getValueString(context, AppConfig.LATITUDE);
        String longitude = PreferenceHelper.getValueString(context, AppConfig.LONGITUDE);
        String taglockVersion = getVersion(context,context.getPackageName());
        Log.d("Location", "Lat: " + latitude + " Long: " + longitude);
        deviceInformation.setLatitudes(latitude);
        deviceInformation.setLongitudes(longitude);
        deviceInformation.setIp_Address(ip);
        deviceInformation.setDevice_Token(PreferenceHelper.getValueString(context, AppConfig.FCM_TOKEN));
        String memory_details = checkMemory();
        deviceInformation.setStorage_memory(memory_details);
        boolean isWifiEnabled = checkWifi();
        deviceInformation.setWifi_status(isWifiEnabled);
        boolean deviceStatus = PreferenceHelper.getValueBoolean(context,AppConfig.IS_ACTIVE);
        deviceInformation.setDevice_locked_status(deviceStatus);
        boolean app_down_status = PreferenceHelper.getValueBoolean(context, AppConfig.APK_DOWN_STATUS);
        boolean taglock_down_status = PreferenceHelper.getValueBoolean(context, AppConfig.TAGLOCK_DOWN_STATUS);
        boolean hdmi_status = HdmiListener.state;
        deviceInformation.setHdmi_status(hdmi_status);
        deviceInformation.setTaglock_version(taglockVersion);
        deviceInformation.setApp_download_status(app_down_status);
        deviceInformation.setTaglock_download_status(taglock_down_status);
        return deviceInformation;
    }

    //Update details of the device
    public DeviceInformation deviceData(){
        superClass = new SuperClass(context);
        String ip, versionName;
        DeviceInformation deviceInformation = new DeviceInformation();
        String mac = TaglockDeviceInfo.getMACAddress("wlan0");
        String macAddressEthernet = TaglockDeviceInfo.getMACAddress("eth0");
        if (isWifiConnected()){
            Integer ipAddress = getWifiIp();
            ip = intToIp(ipAddress);
        }else if (isEthernetConnected()){
            ip = getLANIp();
        }else {
            ip = "NA";
        }
        String latitude = PreferenceHelper.getValueString(context, AppConfig.LATITUDE);
        String longitude = PreferenceHelper.getValueString(context, AppConfig.LONGITUDE);
        String taglockVersion = getVersion(context,context.getPackageName());
        Log.d("Location", "Lat: " + latitude + " Long: " + longitude);
        versionName = PreferenceHelper.getString(context, AppConfig.APK_VERSION);
        deviceInformation.setLatitudes(latitude);
        deviceInformation.setLongitudes(longitude);
        deviceInformation.setIp_Address(ip);
        deviceInformation.setWifimac_Address(mac);
        deviceInformation.setLanimac_Address(macAddressEthernet);
        deviceInformation.setDevice_Token(PreferenceHelper.getValueString(context, AppConfig.FCM_TOKEN));
        String memory_details = checkMemory();
        deviceInformation.setStorage_memory(memory_details);
        String RAM = checkRAM();
        deviceInformation.setRam(RAM);
        boolean isWifiEnabled = checkWifi();
        deviceInformation.setWifi_status(isWifiEnabled);
        String box_name = getBoxName();
        deviceInformation.setBox_Name(box_name);
        String box_android = getBoxAndroid();
        deviceInformation.setAndroid_version(box_android);
        String box_api = getBoxApi();
        deviceInformation.setDevice_Api_version(box_api);
        boolean deviceStatus = PreferenceHelper.getValueBoolean(context,AppConfig.IS_ACTIVE);
        deviceInformation.setDevice_locked_status(deviceStatus);
        String device_name = PreferenceHelper.getValueString(context, AppConfig.DEVICE_NAME);
        String device_group = PreferenceHelper.getValueString(context, AppConfig.DEVICE_GROUP);
        boolean app_down_status = PreferenceHelper.getValueBoolean(context, AppConfig.APK_DOWN_STATUS);
        boolean taglock_down_status = PreferenceHelper.getValueBoolean(context, AppConfig.TAGLOCK_DOWN_STATUS);
        boolean hdmi_status = HdmiListener.state;
        deviceInformation.setDevice_name(device_name);
        deviceInformation.setDevice_group(device_group);
        deviceInformation.setHdmi_status(hdmi_status);
        deviceInformation.setDefault_apk_version(versionName);
        deviceInformation.setTaglock_version(taglockVersion);
        deviceInformation.setApp_download_status(app_down_status);
        deviceInformation.setTaglock_download_status(taglock_down_status);
        return deviceInformation;
    }


    //To switch the navigation
    public void switchNav(){
        superClass = new SuperClass(context);
        boolean is_visible = PreferenceHelper.getValueBoolean(context, AppConfig.IS_NAV_VISIBLE);
        if (is_visible){
            superClass.hideNavToggle();
        }else {
            superClass.showNavToggle();
        }
    }

    //Hide the status and navigation bar
    public void hideStatusBar() {
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        view = new customViewGroup(context);
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                // This is to enable the notification to receive touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = (int) (20 * context.getResources()
                .getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;
        windowManager.addView(view, localLayoutParams);
    }

    public void showStatusBar() {
        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        view = new customViewGroup(context);
        windowManager.removeView(view);
    }

    public class customViewGroup extends ViewGroup {

        public customViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Log.v("customViewGroup", "**********Intercepted");
            return true;
        }
    }

    //Exit from the TagLock
    public void exitApp() {
        final DeviceInfoController deviceInfoController = new DeviceInfoController();
        final DeviceInformation deviceInformation = new DeviceInformation();
        superClass = new SuperClass(context);
        final String packageName = PreferenceHelper.getValueString(context, AppConfig.DEVICE_LAUNCHER);
        final String deviceName = PreferenceHelper.getValueString(context, AppConfig.DEVICE_NAME);
        superClass.showNavToggle();
        PreferenceHelper.setValueBoolean(context, AppConfig.IS_ACTIVE, false);
        deviceInformation.setDevice_locked_status(false);
        deviceInfoController.updateTaglockStatus(deviceInformation);
        updateDevice(deviceInformation);
        superClass.unHideDefaultLauncher(packageName);
        SuperClass.disableActivity(context);
    }


    //Clear TagBox data and delete it's directory
    public void clearData() {
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        final int clearPass = getProfile.get(0).getClear_data_passcode();
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.alert_dialog, null);
        final EditText alertEdit = view.findViewById(R.id.alertEdit);
        final AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setTitle("Enter Passcode")
                .setMessage("Are you sure you want to clear data?")
                .setView(view)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(alertEdit.getText())) {
                            //alertEdit.setError("Please enter passcode");
                            Toast.makeText(context, "Please enter passcode", Toast.LENGTH_LONG).show();
                        } else if (Integer.parseInt(alertEdit.getText().toString()) == clearPass) {
                            SuperClass.clearData();
                            dialog.cancel();
                            Toast.makeText(context, "Data cleared successfully!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "Passcode is incorrect", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog dialog = alert.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        };

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 30000);
    }

    //To delete directory
    public static boolean deleteDir(File dir) {
        try {
            if (dir.isDirectory() && dir.exists()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
