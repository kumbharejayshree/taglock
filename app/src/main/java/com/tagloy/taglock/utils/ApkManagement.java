package com.tagloy.taglock.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

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
import com.tagloy.taglock.realmmodels.DefaultProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmResults;
import pl.droidsonroids.gif.GifImageView;

import static android.content.Context.DOWNLOAD_SERVICE;

public class ApkManagement {
    private long apkId, taglockId;
    private Context context;
    private SuperClass superClass;
    private TaglockDeviceInfo taglockDeviceInfo;
    private GifImageView progressBar;

    public ApkManagement(Context context) {
        this.context = context;
    }

    //Get uploaded apk name and download
    public void getApk() {
        superClass = new SuperClass(context);
        taglockDeviceInfo = new TaglockDeviceInfo(context);
        if (taglockDeviceInfo.isNetworkConnected()) {
            DefaultProfileController defaultProfileController = new DefaultProfileController();
            RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
            final String pack = getProfile.get(0).getApp_package_name();
            RequestQueue queue = Volley.newRequestQueue(context);
            try{
                String device_name = PreferenceHelper.getValueString(context, AppConfig.DEVICE_NAME);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("device_name",device_name);
                final String request = jsonObject.toString();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GET_APK_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        boolean isTagboxInstalled = superClass.appInstalled(pack);
                        try {
                            JSONObject apk = new JSONObject(response);
                            String apk_name = apk.getString("apk_name");
                            String version = apk.getString("apk_version");
                            if (isTagboxInstalled) {
                                String versionName = version.replace(".", "");
                                String installedVersion = (taglockDeviceInfo.getVersion(context,pack)).replace(".", "");
                                long ver = Long.parseLong(versionName);
                                long ins = Long.parseLong(installedVersion);
                                if (ver > ins) {
                                    PreferenceHelper.setValueString(context, AppConfig.APK_NAME, apk_name);
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/.taglock/.apkmanagement");
                                    TaglockDeviceInfo.deleteDir(dir);
                                    downloadApk(apk_name);
                                } else {
                                    Log.d("File Status", "Not downloaded");
                                }
                            } else {
                                PreferenceHelper.setValueString(context, AppConfig.APK_NAME, apk_name);
                                File dir = new File(Environment.getExternalStorageDirectory() + "/.taglock/.apkmanagement");
                                TaglockDeviceInfo.deleteDir(dir);
                                downloadApk(apk_name);
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Status: ", "Failed to get apk");
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
            }catch (JSONException je){
                je.printStackTrace();
            }
        } else {
            Log.d("Network Status", "Not connected");
        }
    }

    //To get new Taglock update
    public void getTaglock() {
        superClass = new SuperClass(context);
        taglockDeviceInfo = new TaglockDeviceInfo(context);
        if (taglockDeviceInfo.isNetworkConnected()) {
            try{
                String group_name = PreferenceHelper.getValueString(context, AppConfig.DEVICE_GROUP);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("group_name",group_name);
                final String  request = jsonObject.toString();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GET_TAGLOCK_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject apk = new JSONObject(response);
                            String apk_name = apk.getString("apk_name");
                            String version = apk.getString("apk_version");
                            String versionName = version.replace(".", "");
                            String installedVersion = (taglockDeviceInfo.getVersion(context,context.getPackageName())).replace(".", "");
                            long ver = Long.parseLong(versionName);
                            long ins = Long.parseLong(installedVersion);
                            if (ver > ins) {
                                PreferenceHelper.setValueString(context, AppConfig.TAGLOCK_APK, apk_name);
                                File dir = new File(Environment.getExternalStorageDirectory() + "/.taglock/.taglockmanagement");
                                TaglockDeviceInfo.deleteDir(dir);
                                downloadTaglock(apk_name);
                            } else {
                                Log.d("File Status", "Not downloaded");
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Status: ", "Failed to get apk");
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
            }catch (JSONException je){
                je.printStackTrace();
            }
        } else {
            Log.d("Network Status", "Not connected");
        }
    }

    //start downloading apk
    private void downloadApk(String fileName) {
        progressBar = ((Activity) context).findViewById(R.id.downloadProgress);
        Uri uri = Uri.parse(AppConfig.APK_URI + fileName);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setVisibleInDownloadsUi(false);
        String apkPath = "/.taglock/.apkmanagement/";
        request.setDestinationInExternalPublicDir(apkPath, uri.getLastPathSegment());
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        apkId = downloadManager.enqueue(request);
        PreferenceHelper.setValueString(context,AppConfig.APP_DOWN_ID,String.valueOf(apkId));
        progressBar.setVisibility(View.VISIBLE);
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
                PreferenceHelper.setValueBoolean(context, AppConfig.APK_DOWN_STATUS, false);
                if (taglockDeviceInfo.isNetworkConnected()) {
                    getApk();
                } else {
                    Log.d("Network Status", "No network");
                }
            }
        }
    }

    //start downloading TagLock
    private void downloadTaglock(String fileName) {
        progressBar = ((Activity) context).findViewById(R.id.downloadProgress);
        Uri uri = Uri.parse(AppConfig.TAGLOCK_URI + fileName);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setVisibleInDownloadsUi(false);
        String taglockPath = "/.taglock/.taglockmanagement/";
        request.setDestinationInExternalPublicDir(taglockPath, uri.getLastPathSegment());
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        taglockId = downloadManager.enqueue(request);
        PreferenceHelper.setValueString(context,AppConfig.TAGLOCK_DOWN_ID,String.valueOf(taglockId));
        progressBar.setVisibility(View.VISIBLE);
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
                if (taglockDeviceInfo.isNetworkConnected()) {
                    getTaglock();
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
            progressBar = ((Activity) context).findViewById(R.id.downloadProgress);
            final DefaultProfileController defaultProfileController = new DefaultProfileController();
            RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
            String pack = getProfile.get(0).getApp_package_name();
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == apkId) {
                progressBar.setVisibility(View.GONE);
                PreferenceHelper.setValueBoolean(context, AppConfig.APK_DOWN_STATUS, true);
                String fileName = ".apkmanagement/" + PreferenceHelper.getValueString(context, AppConfig.APK_NAME);
                boolean isTagboxInstalled = superClass.appInstalled(pack);
                if (!isTagboxInstalled) {
                    new MainActivity.InstallApp(context, fileName).execute();
                } else {
                    new MainActivity.UpdateApp(context, fileName).execute();
                }
            } else if (id == taglockId) {
                progressBar.setVisibility(View.GONE);
                PreferenceHelper.setValueBoolean(context, AppConfig.TAGLOCK_DOWN_STATUS, true);
                String fileName = ".taglockmanagement/" + PreferenceHelper.getValueString(context, AppConfig.TAGLOCK_APK);
                new MainActivity.UpdateTaglock(context, fileName).execute();
            }
        }
    };
}
