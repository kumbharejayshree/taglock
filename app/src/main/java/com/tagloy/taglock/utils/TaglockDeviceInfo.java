package com.tagloy.taglock.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Button;
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
import com.tagloy.taglock.activity.AdminActivity;
import com.tagloy.taglock.activity.DeviceGroupActivity;
import com.tagloy.taglock.realmcontrollers.DefaultProfileController;
import com.tagloy.taglock.realmcontrollers.DeviceInfoController;
import com.tagloy.taglock.realmmodels.DefaultProfile;
import com.tagloy.taglock.realmmodels.DeviceInformation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;

public class TaglockDeviceInfo {

    DeviceInfoController deviceInfoController = new DeviceInfoController();
    DeviceInformation deviceInformation = new DeviceInformation();
    private Context context;
    private WindowManager windowManager;
    private customViewGroup view;
    SuperClass superClass;

    public TaglockDeviceInfo(Context context) {
        this.context = context;
    }

    public boolean isNetworkConnected() {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return info != null;
    }

    public Integer getIpAddress() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Integer ip = wifiInfo.getIpAddress();
        int speed = wifiInfo.getRssi();
        Log.d("Speed: ", String.valueOf(speed));
        return ip;
    }

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

    public void checkHDMI() {
        Intent intent = new Intent("android.intent.action.HDMI_PLUGGED");
        context.sendBroadcast(intent);
    }

    public void deviceToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("fcm_token", "");
        Log.d("Token: ", token);
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.FCM_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("fcm_token", token);
                return param;
            }
        };
        queue.add(stringRequest);
    }


    public void checkNameValidity(final String deviceName){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device_name", deviceName);
            final String request = jsonObject.toString();
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.NAME_VALIDITY_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject name = new JSONObject(response);
                        String status = name.getString("status");
                        if (status.equals("200")){
                            Toast.makeText(context, "Name already exists", Toast.LENGTH_LONG).show();
                        }
                    }catch (JSONException je){
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    PreferenceHelper.setValueString(context,AppConfig.DEVICE_NAME,deviceName);
                    Intent intent = new Intent(context, DeviceGroupActivity.class);
                    intent.putExtra("device_name",deviceName);
                    context.startActivity(intent);
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
        }catch (JSONException je){
            je.printStackTrace();
        }
    }

    public void checkGroupKey(final String groupName,final String groupKey){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("group_name", groupName);
            jsonObject.put("group_key",groupKey);
            final String request = jsonObject.toString();
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GROUP_VALIDITY_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject name = new JSONObject(response);
                        String status = name.getString("status");
                        if (status.equals("200")){
                            PreferenceHelper.setValueString(context,AppConfig.DEVICE_GROUP,groupName);
                            Intent intent = new Intent(context, AdminActivity.class);
                            context.startActivity(intent);
                        }else if(status.equals("404")){
                            Toast.makeText(context, "Group key is invalid. Please check!", Toast.LENGTH_LONG).show();
                        }
                    }catch (JSONException je){
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("API","Error");
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
        }catch (JSONException je){
            je.printStackTrace();
        }
    }

    public String checkMemory() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable, bytesTotal;
        bytesTotal = (statFs.getBlockSizeLong() * statFs.getBlockCountLong());
        bytesAvailable = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
        long bytesUsed = (bytesTotal - bytesAvailable) / (1024 * 1024);
        long total = bytesTotal / (1024 * 1024);
        String memory = bytesUsed + "/" + total + "MB";
        Log.d("Available memory: ", memory);
        return memory;
    }

    public String checkRAM() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalMemory = ((memoryInfo.totalMem / 1024) / 1024);
        String ram = totalMemory + "MB";
        Log.d("RAM: ", totalMemory + "MB");
        return ram;
    }

    public boolean checkWifi() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public String getBoxName() {
        StringBuilder builder = new StringBuilder();
        String model = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;
        builder.append(manufacturer).append(" ").append(model);
        Log.d("DeviceName: ", builder.toString());
        return builder.toString();
    }

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
//        DateFormat df = DateFormat.getTimeInstance();
//        df.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        Date ISTTime = Calendar.getInstance().getTime();//df.format(new Date());
        String IST = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").format(new Date());
        Log.d("DeviceTime: ", IST);
        return IST;
    }

    public String deviceEPOCHtime() {
        long milliseconds  = System.currentTimeMillis();

        long unixTimeStamp = milliseconds / 1000L;
        Log.d("deviceEPOCHtime: ", String.valueOf(unixTimeStamp));
        return String.valueOf(unixTimeStamp);
    }

    public void applyProfile() {
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        final DefaultProfile defaultProfile = new DefaultProfile();
        String group_name = PreferenceHelper.getValueString(context,AppConfig.DEVICE_GROUP);
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.PROFILE_URL + group_name, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject profile = jsonArray.getJSONObject(i);
                        defaultProfile.setApp_package_name(profile.getString("apk_package"));
                        defaultProfile.setTaglock_exited_status("1".equals(profile.getString("taglock_status")));
                        defaultProfile.setNavigationbar_status("1".equals(profile.getString("nav_status")));
                        defaultProfile.setPasscode(profile.getInt("passcode"));
                        defaultProfile.setClear_data_passcode(profile.getInt("clear_passcode"));
                        defaultProfile.setDefault_apk_call_duration(profile.getInt("default_apk_call"));
                        defaultProfile.setGroup_name(profile.getString("group_name"));
                        defaultProfile.setDefault_apk_version(profile.getString("apk_version"));
                        boolean profileRealm = defaultProfileController.isAvailablProfileData(profile.getString("group_name"));
                        if (profileRealm){
                            defaultProfileController.updateProfileDataContent(profile.getString("group_name"),defaultProfile);
                        }else {
                            defaultProfileController.addDefaultProfileData(defaultProfile);
                        }
                    }

                }catch (JSONException je){
                    je.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }

    public void deviceDetails(final DeviceInformation deviceInformation) {
        final DeviceInfoController deviceInfoController = new DeviceInfoController();
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
            jsonObject.put("hdmi_status",deviceInformation.getHdmi_status());
            jsonObject.put("default_apk_version",deviceInformation.getDefault_apk_version());
            jsonObject.put("taglock_version",deviceInformation.getTaglock_version());
            jsonObject.put("app_download_status",deviceInformation.getApp_download_status());
            jsonObject.put("taglock_download_status",deviceInformation.getTaglock_download_status());
            jsonObject.put("ip_address", deviceInformation.getIp_Address());
            jsonObject.put("mac_address", deviceInformation.getMac_Address());
            jsonObject.put("memory", deviceInformation.getStorage_memory());
            jsonObject.put("ram", deviceInformation.getRam());
            jsonObject.put("device_token", deviceInformation.getDevice_Token());
            jsonObject.put("wifi_status", deviceInformation.getWifi_status());
            jsonObject.put("updated_at",deviceInformation.getUpdated_at());
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
    }

    public void updateDevice(final DeviceInformation deviceInformation){
        final DeviceInfoController deviceInfoController = new DeviceInfoController();
        String device_name = PreferenceHelper.getValueString(context,AppConfig.DEVICE_NAME);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device_group", deviceInformation.getDevice_group());
            jsonObject.put("latitude", deviceInformation.getLatitudes());
            jsonObject.put("longitude", deviceInformation.getLongitudes());
            jsonObject.put("box_name", deviceInformation.getBox_Name());
            jsonObject.put("box_android", deviceInformation.getAndroid_version());
            jsonObject.put("box_api", deviceInformation.getDevice_Api_version());
            jsonObject.put("device_locked", deviceInformation.getDevice_locked_status());
            jsonObject.put("hdmi_status",deviceInformation.getHdmi_status());
            jsonObject.put("default_apk_version",deviceInformation.getDefault_apk_version());
            jsonObject.put("taglock_version",deviceInformation.getTaglock_version());
            jsonObject.put("app_download_status",deviceInformation.getApp_download_status());
            jsonObject.put("taglock_download_status",deviceInformation.getTaglock_download_status());
            jsonObject.put("ip_address", deviceInformation.getIp_Address());
            jsonObject.put("mac_address", deviceInformation.getMac_Address());
            jsonObject.put("memory", deviceInformation.getStorage_memory());
            jsonObject.put("ram", deviceInformation.getRam());
            jsonObject.put("device_token", deviceInformation.getDevice_Token());
            jsonObject.put("wifi_status", deviceInformation.getWifi_status());
            jsonObject.put("updated_at",String.valueOf(System.currentTimeMillis()/1000));
            final String request = jsonObject.toString();
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.UPDATE_DEVICE_URL + device_name, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String res = jsonObject.getString("status");
                        if (res.equals("200")) {
                            Log.d("Success: ", "Device details updated");
                        } else {
                            Log.d("Failure: ", "Device details not updated");
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
    }

    public String getVersion(String packageName) {
        String versionName = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException pe) {
            pe.printStackTrace();
        }
        return versionName;
    }

    //Hide the status and navigation bar
    public void hideStatusBar() {
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        view = new customViewGroup(context);
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
        // this is to enable the notification to receive touch events
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
        windowManager.removeView(view);
        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        view = new customViewGroup(context);
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
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        final RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        final int passcode = getProfile.get(0).getPasscode();
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.alert_dialog, null);
        final EditText alertEdit = view.findViewById(R.id.alertEdit);
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Enter Passcode")
                .setMessage("Are you sure you want to exit?")
                .setView(view)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(alertEdit.getText())){
                            alertEdit.setError("Please enter passcode");
                        }else if (Integer.parseInt(alertEdit.getText().toString()) == passcode) {
                            superClass.showNavToggle();
                            PreferenceHelper.setValueBoolean(context, AppConfig.IS_ACTIVE, false);
                            deviceInformation.setDevice_locked_status(false);
                            deviceInfoController.updateTaglockStatus(PreferenceHelper.getValueString(context, AppConfig.DEVICE_NAME), false);
                            updateDevice(deviceInformation);
                            SuperClass.unHideDefaultLauncher("com.oranth.launcher");
                            SuperClass.disableActivity(context);
                            ((Activity) context).finish();
                        }else {
                            alertEdit.setError("Passcode is incorrect");
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
        dialog.show();

        final Handler handler  = new Handler();
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
                        int pass = Integer.parseInt(alertEdit.getText().toString());
                        if (TextUtils.isEmpty(alertEdit.getText())){
                            alertEdit.setError("Please enter passcode");
                        }else if (pass == clearPass) {
                            SuperClass.clearData();
                            File dir = new File(Environment.getExternalStorageDirectory() + "/tagloy/");
                            if (deleteDir(dir)) {
                                Log.e("Deleted", "");
                            } else {
                                Log.e("Not Deleted", "");
                            }
                            dialog.cancel();
                        } else {
                            alertEdit.setError("Passcode is incorrect");
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
        dialog.show();

        final Handler handler  = new Handler();
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
