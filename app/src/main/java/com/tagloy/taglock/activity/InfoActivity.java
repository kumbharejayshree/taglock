package com.tagloy.taglock.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

    TextView deviceNameText,taglockText,appNameText,ipText,wifiMacText,lanMacText,storageText,connectionText,
    connectivityText;
    TaglockDeviceInfo taglockDeviceInfo;
    SuperClass superClass;
    ApplicationInfo app;
    PackageManager manager;
    CharSequence appName = "";
    String ip,versionName;
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        superClass = new SuperClass(this);
        deviceNameText = findViewById(R.id.device_name_text);
        taglockText = findViewById(R.id.taglock_text);
        appNameText = findViewById(R.id.appname_text);
        connectionText = findViewById(R.id.connection_text);
        connectivityText = findViewById(R.id.connectivity_text);
        ipText = findViewById(R.id.ip_text);
        wifiMacText = findViewById(R.id.wifiMac_text);
        lanMacText = findViewById(R.id.lanMac_text);
        storageText = findViewById(R.id.storage_text);
        manager = getPackageManager();

        String deviceName = PreferenceHelper.getValueString(this, AppConfig.DEVICE_NAME);
        deviceNameText.setText(deviceName);


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
            String taglockVersion = TaglockDeviceInfo.getVersion(this,getPackageName());
            taglockText.setText("Taglock Version: " + taglockVersion);
            appNameText.setText(appName + " Version: " + versionName);

        }catch (PackageManager.NameNotFoundException | NullPointerException ne){
            ne.printStackTrace();
        }

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
            Integer ipAddress = taglockDeviceInfo.getIpAddress();
            ip = taglockDeviceInfo.intToIp(ipAddress);
        }else if (taglockDeviceInfo.isEthernetConnected()){
            ip = taglockDeviceInfo.getIp();
        }else {
            ip = "NA";
        }
        ipText.setText("IP Address: " + ip);
        wifiMacText.setText("WiFI MAC: " + mac);
        lanMacText.setText("LAN MAC: " + macAddressEthernet);


        String memory = taglockDeviceInfo.checkMemory();

        storageText.setText("Internal: " + memory);

        connectivityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoActivity.this, WebActivity.class);
                startActivity(intent);
            }
        });
    }
}
