package com.tagloy.taglock.activity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.tagloy.taglock.R;
import com.tagloy.taglock.realmcontrollers.DefaultProfileController;
import com.tagloy.taglock.realmmodels.DefaultProfile;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

import java.util.Objects;

import io.realm.RealmResults;

public class InfoActivity extends AppCompatActivity {

    TextView deviceNameText,taglockText,appNameText,ipText,macText,storageText;
    TaglockDeviceInfo taglockDeviceInfo;
    SuperClass superClass;
    ApplicationInfo app;
    PackageManager manager;
    CharSequence appName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        superClass = new SuperClass(this);
        deviceNameText = findViewById(R.id.device_name_text);
        taglockText = findViewById(R.id.taglock_text);
        appNameText = findViewById(R.id.appname_text);
        ipText = findViewById(R.id.ip_text);
        macText = findViewById(R.id.mac_text);
        storageText = findViewById(R.id.storage_text);
        manager = getPackageManager();

        String deviceName = PreferenceHelper.getValueString(this, AppConfig.DEVICE_NAME);
        deviceNameText.setText(deviceName);


        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();

        try {
            String pack = getProfile.get(0).getApp_package_name();
            Log.d("Pack", pack);
            app = manager.getApplicationInfo(pack, PackageManager.GET_META_DATA);
            appName = manager.getApplicationLabel(app);
            boolean isTagboxInstalled = superClass.appInstalled(pack);
            String versionName = "";
            if (isTagboxInstalled){
                versionName = taglockDeviceInfo.getVersion(pack);
            }else {
                versionName = "NULL";
            }
            String taglockVersion = taglockDeviceInfo.getVersion(getPackageName());
            taglockText.setText("Taglock Version: " + taglockVersion);
            appNameText.setText(appName + " Version: " + versionName);

        }catch (PackageManager.NameNotFoundException ne){
            ne.printStackTrace();
        }catch (NullPointerException np){
            np.printStackTrace();
        }


        String mac = TaglockDeviceInfo.getMACAddress("wlan0");
        String macAddressEthernet = TaglockDeviceInfo.getMACAddress("eth0");
        if (mac.equals("") && macAddressEthernet.equals("")) {
            mac = "NA";
        }else if(mac.equals("")){
            mac = macAddressEthernet;
        }
        Integer ipAddress = taglockDeviceInfo.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        if (ipAddress.equals("")) {
            ip = "NA";
        }
        ipText.setText("IP Address: " + ip);
        macText.setText("MAC Address: " + mac);


        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable, bytesTotal;
        bytesTotal = (statFs.getBlockSizeLong() * statFs.getBlockCountLong());
        bytesAvailable = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
        long free = bytesAvailable / (1024 * 1024 * 1024);
        long used = (bytesTotal - bytesAvailable) / (1024 * 1024);
        long total = bytesTotal / (1024 * 1024 * 1024);

        storageText.setText("Internal: " + used + "MB/" + free + "GB/" + total + "GB");
    }
}
