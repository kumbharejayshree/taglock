package com.tagloy.taglock.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tagloy.taglock.R;
import com.tagloy.taglock.realmcontrollers.DefaultProfileController;
import com.tagloy.taglock.realmmodels.DefaultProfile;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

import io.realm.RealmResults;

public class InfoActivity extends AppCompatActivity {

    TextView deviceNameText,deviceGroupText,taglockText,appNameText,ipText,wifiMacText,lanMacText,
            storageText,connectionText, connectivityText, orientationText,
            orientationTextView; //timeZoneText, changeTimeZoneText;
    TaglockDeviceInfo taglockDeviceInfo;
    SuperClass superClass;
    ApplicationInfo app;
    PackageManager manager;
    CharSequence appName = "";
    String ip,versionName;
    Button netTestSpeed;
    Context context = InfoActivity.this;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        superClass = new SuperClass(this);
        deviceNameText = findViewById(R.id.device_name_text);
        deviceGroupText = findViewById(R.id.device_group_text);
        taglockText = findViewById(R.id.taglock_text);
        appNameText = findViewById(R.id.appname_text);
        connectionText = findViewById(R.id.connection_text);
        connectivityText = findViewById(R.id.connectivity_text);
        netTestSpeed = findViewById(R.id.networkTestBtn);
        ipText = findViewById(R.id.ip_text);
        wifiMacText = findViewById(R.id.wifiMac_text);
        lanMacText = findViewById(R.id.lanMac_text);
        storageText = findViewById(R.id.storage_text);
        orientationText = findViewById(R.id.orientation_text);
//        changePositionText = findViewById(R.id.change_position);
        orientationTextView = findViewById(R.id.orientationTextView);
//        timeZoneText = findViewById(R.id.timeZoneText);
//        changeTimeZoneText = findViewById(R.id.changeTimeZoneText);
        manager = getPackageManager();

        String deviceName = PreferenceHelper.getValueString(this, AppConfig.DEVICE_NAME);
        deviceNameText.append(" " + deviceName);
        String deviceGroup = PreferenceHelper.getValueString(this, AppConfig.DEVICE_GROUP);
        deviceGroupText.append(" " + deviceGroup);


        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();

        try {
            String pack = getProfile.get(0).getApp_package_name();
            app = manager.getApplicationInfo(pack, PackageManager.GET_META_DATA);
            appName = manager.getApplicationLabel(app);
            boolean isDefaultInstalled = superClass.appInstalled(pack);
            if (isDefaultInstalled){
                versionName = TaglockDeviceInfo.getVersion(this,pack);
            }else {
                versionName = "NULL";
            }
            appNameText.setText(appName + " Version: " + versionName);

        }catch (PackageManager.NameNotFoundException | NullPointerException ne){
            ne.printStackTrace();
        }

        String taglockVersion = TaglockDeviceInfo.getVersion(this,getPackageName());
        taglockText.setText("Taglock Version: " + taglockVersion);

//        TimeZone timeZone = TimeZone.getDefault();
//        timeZoneText.append(" " + timeZone.getID());
//
//        changeTimeZoneText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(context, "Changed!", Toast.LENGTH_LONG).show();
//                taglockDeviceInfo.setTimeZone("Asia/Kolkata");
//                Intent intent = new Intent(Settings.DAT);
//                startActivity(intent);
//            }
//        });

        connectionText.setText("Network Source: ");
        if (taglockDeviceInfo.isEthernetConnected()){
            connectionText.append("LAN");
        }else if (taglockDeviceInfo.isWifiConnected()){
            connectionText.append("WiFi");
        }else {
            connectionText.append("Not connected to internet");
        }
        String mac = TaglockDeviceInfo.getMACAddress("wlan0");
        String macAddressEthernet = TaglockDeviceInfo.getMACAddress("eth0");
        if (taglockDeviceInfo.isWifiConnected()){
            Integer ipAddress = taglockDeviceInfo.getWifiIp();
            ip = taglockDeviceInfo.intToIp(ipAddress);
        }else if (taglockDeviceInfo.isEthernetConnected()){
            ip = taglockDeviceInfo.getLANIp();
        }else {
            ip = "NA";
        }
        ipText.setText("IP Address: " + ip);
        wifiMacText.setText("WiFI MAC: " + mac);
        lanMacText.setText("LAN MAC: " + macAddressEthernet);


        String memory = taglockDeviceInfo.checkMemory();

        storageText.setText("Internal: " + memory);

        netTestSpeed.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, WebActivity.class);
            startActivity(intent);
        });

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            orientationText.append(" Landscape");
        } else {
            // In portrait
            orientationText.append(" Portrait");
        }

//        orientationTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent rotationIntent = new Intent();
//                rotationIntent.setClassName("com.mbox.settings", "com.mbox.settings.MboxSettingActivity");
//                startActivity(rotationIntent);
//            }
//        });

//        changePositionText.setOnClickListener(v -> {
//            Intent positionIntent = new Intent();
//            positionIntent.setClassName("com.android.tv.settings", "com.android.tv.settings.device.display.position.ScreenPositionActivity");
//            startActivity(positionIntent);
//        });

        registerReceiver(connectionReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null || intent.getExtras() == null)
                return;

            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED){
                if (taglockDeviceInfo.isWifiConnected()){
                    Integer ipAddress = taglockDeviceInfo.getWifiIp();
                    ip = taglockDeviceInfo.intToIp(ipAddress);
                }else if (taglockDeviceInfo.isEthernetConnected()){
                    ip = taglockDeviceInfo.getLANIp();
                }else {
                    ip = "NA";
                }
                connectionText.setText("Network Source: ");
                ipText.setText("IPAddress: " + ip);
                if(!taglockDeviceInfo.isNetworkConnected()) {
                    ipText.append("(Not connected to internet)");
                }else{
                    if (taglockDeviceInfo.isEthernetConnected()){
                        connectionText.append("LAN");
                    }else if (taglockDeviceInfo.isWifiConnected()){
                        connectionText.append("WiFi");
                    }
                }
            }else {
                ip = "NA";
                ipText.setText("IPAddress: " + ip);
                connectionText.setText("Network Source: ");
                ipText.append("(Not connected to internet)");
                connectionText.append("Not connected to internet");
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(connectionReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
}
