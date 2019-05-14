package com.tagloy.taglock.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.tagloy.taglock.adapters.GridAdapter;
import com.tagloy.taglock.realmcontrollers.DefaultProfileController;
import com.tagloy.taglock.realmcontrollers.DeviceInfoController;
import com.tagloy.taglock.realmmodels.DefaultProfile;
import com.tagloy.taglock.realmmodels.DeviceInformation;
import com.tagloy.taglock.receiver.RecentAppClickReceiver;
import com.tagloy.taglock.utils.ApkManagement;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.TaglockDeviceInfo;
import com.tagloy.taglock.models.Item;
import com.tagloy.taglock.utils.PermissionsClass;
import com.tagloy.taglock.R;
import com.tagloy.taglock.utils.SuperClass;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements LocationListener {

    DeviceInfoController deviceInfoController = new DeviceInfoController();
    DeviceInformation deviceInformation = new DeviceInformation();
    RecentAppClickReceiver mReceiver = new RecentAppClickReceiver();
    PermissionsClass permissionsClass;
    PackageManager manager;
    TaglockDeviceInfo taglockDeviceInfo;
    LocationManager locationManager;
    WifiManager wifiManager;
    List<Item> list = new ArrayList<>();
    GridAdapter gridAdapter;
    GridView appsGrid;
    TextView versionText, ipText;
    ApplicationInfo apps;
    String provider;
    public ProgressBar downloadProgress;
    private long apkId, taglockId;
    private static final String TAG = "MainActivity";
    private static SuperClass superClass;
    CountDownTimer appCountDownTimer, wifiCountDownTimer;
    ApkManagement apkManagement;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        permissionsClass = new PermissionsClass(this);
        gridAdapter = new GridAdapter(this, list);
        downloadProgress = findViewById(R.id.downloadProgress);
        versionText = findViewById(R.id.versionText);
        ipText = findViewById(R.id.ipText);
        superClass = new SuperClass(this);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        apkManagement = new ApkManagement(this);
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        final String pack = getProfile.get(0).getApp_package_name();
        int apk_call_duraion = getProfile.get(0).getDefault_apk_call_duration();
        //Checking if app is running at time interval of given time
        appCountDownTimer = new CountDownTimer(apk_call_duraion * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (!SuperClass.isAppRunning(MainActivity.this, pack)) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(pack);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Waiting...");
                }
            }
        };
        boolean isTagboxInstalled = superClass.appInstalled(pack);
        String versionName = "";
        if (isTagboxInstalled) {
            versionName = taglockDeviceInfo.getVersion(pack);
        } else {
            versionName = "NULL";
        }
        String taglockVersion = taglockDeviceInfo.getVersion(getPackageName());
        versionText.setText("Taglock Version: " + taglockVersion + " Default App Version: " + versionName);
        appsGrid = findViewById(R.id.appsGrid);
        manager = getPackageManager();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        String packageName = PreferenceHelper.getValueString(this, AppConfig.DEVICE_LAUNCHER);
        superClass.hideDefaultLauncher(packageName);
        if (Build.VERSION.SDK_INT>=23)
            taglockDeviceInfo.hideStatusBar();
        superClass.hideNavToggle();
        taglockDeviceInfo.deviceToken();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);
        if (provider != null && !provider.equals("")) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider, 15000, 1, this);
            if (location != null) {
                onLocationChanged(location);
            } else {
                Log.d("Status ", "No location found");
            }
        }
        if (!taglockDeviceInfo.isEthernetConnected()) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        }
        String mac = TaglockDeviceInfo.getMACAddress("wlan0");
        String macAddressEthernet = TaglockDeviceInfo.getMACAddress("eth0");
        if (!taglockDeviceInfo.isEthernetConnected() && !taglockDeviceInfo.isWifiConnected()) {
            mac = "NA";
        } else if (taglockDeviceInfo.isEthernetConnected()) {
            mac = macAddressEthernet;
        }
        if (taglockDeviceInfo.isWifiConnected()) {
            macAddressEthernet = "NA";
        }

        Integer ipAddress = taglockDeviceInfo.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        if (ipAddress.equals("")) {
            ip = "NA";
        }
        Log.d(TAG, "Mac: " + mac + " Ip: " + ip + " MacIth: " + macAddressEthernet);
        ipText.setText("IPAddress: " + ip);
        String latitude = PreferenceHelper.getValueString(this, AppConfig.LATITUDE);
        String longitude = PreferenceHelper.getValueString(this, AppConfig.LONGITUDE);
        Log.d("Location", "Lat: " + latitude + " Long: " + longitude);
        deviceInformation.setLatitudes(latitude);
        deviceInformation.setLongitudes(longitude);
        deviceInformation.setIp_Address(ip);
        deviceInformation.setMac_Address(mac);
        deviceInformation.setDevice_Token(PreferenceHelper.getValueString(this, AppConfig.FCM_TOKEN));
        String memory_details = taglockDeviceInfo.checkMemory();
        deviceInformation.setStorage_memory(memory_details);
        String RAM = taglockDeviceInfo.checkRAM();
        deviceInformation.setRam(RAM);
        boolean isWifiEnabled = taglockDeviceInfo.checkWifi();
        deviceInformation.setWifi_status(isWifiEnabled);
        String box_name = taglockDeviceInfo.getBoxName();
        deviceInformation.setBox_Name(box_name);
        String box_android = taglockDeviceInfo.getBoxAndroid();
        deviceInformation.setAndroid_version(box_android);
        String box_api = taglockDeviceInfo.getBoxApi();
        deviceInformation.setDevice_Api_version(box_api);
        String IST = taglockDeviceInfo.getDeviceTime();
        long epoch = System.currentTimeMillis() / 1000;
        deviceInformation.setUpdated_at(String.valueOf(epoch));
        Log.d(TAG, "Version Name: " + versionName);
        boolean deviceStatus = SuperClass.isAppRunning(this, this.getPackageName());
        deviceInformation.setDevice_locked_status(deviceStatus);
        String device_name = PreferenceHelper.getValueString(this, AppConfig.DEVICE_NAME);
        String device_group = PreferenceHelper.getValueString(this, AppConfig.DEVICE_GROUP);
        boolean app_down_status = PreferenceHelper.getValueBoolean(this, AppConfig.APK_DOWN_STATUS);
        boolean taglock_down_status = PreferenceHelper.getValueBoolean(this, AppConfig.TAGLOCK_DOWN_STATUS);
        deviceInformation.setDevice_name(device_name);
        deviceInformation.setDevice_group(device_group);
        //deviceInformation.setUpdated_at(IST);
        deviceInformation.setHdmi_status(true);
        deviceInformation.setDefault_apk_version(versionName);
        deviceInformation.setTaglock_version(taglockVersion);
        deviceInformation.setApp_download_status(app_down_status);
        deviceInformation.setTaglock_download_status(taglock_down_status);
        taglockDeviceInfo.deviceDetails(deviceInformation);
        boolean realmDevice = deviceInfoController.isDeviceAvailable(device_name);
        if (realmDevice) {
            deviceInfoController.updateDeviceData(device_name, deviceInformation);
        } else {
            deviceInfoController.addDeviceData(deviceInformation);
        }

        //If tagbox is closed, open it
        if (isTagboxInstalled) {
            new appLoad(MainActivity.this).execute();
            appCountDownTimer.start();
        } else {
            PreferenceHelper.removeStringValue(this, AppConfig.APK_NAME);
            apkManagement.getApk();
        }

        //Checking for new apk and download if available
        new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                taglockDeviceInfo.updateDevice(deviceInformation);
                apkManagement.getApk();
                apkManagement.getTaglock();
                start();
            }
        }.start();
        wifiCountDownTimer = new CountDownTimer(600000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (!taglockDeviceInfo.isEthernetConnected()) {
                    if (!taglockDeviceInfo.isNetworkConnected()) {
                        wifiManager.setWifiEnabled(false);
                        wifiManager.setWifiEnabled(true);
                    }
                }
                start();
            }
        };
        logUser();
        wifiCountDownTimer.start();
        IntentFilter mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mReceiver, mFilter);
        registerReceiver(apkManagement.downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(batteryInfo, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void forceCrash(MenuItem menuItem) {
        throw new RuntimeException();
    }

    private void logUser() {
        String device_name = PreferenceHelper.getValueString(MainActivity.this, AppConfig.DEVICE_NAME);
        String device_group = PreferenceHelper.getValueString(MainActivity.this, AppConfig.DEVICE_GROUP);
        Crashlytics.setUserName(device_group);
        Crashlytics.setUserIdentifier(device_name);
    }

    private BroadcastReceiver batteryInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            Log.d("Battery: ", level + "%");
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(lon);
        PreferenceHelper.setValueString(MainActivity.this, AppConfig.LATITUDE, latitude);
        PreferenceHelper.setValueString(MainActivity.this, AppConfig.LONGITUDE, longitude);
        Log.d("Location", "Latitude: " + lat + " Longitude: " + lon);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    //Load apps in GridView dynamically
    public class appLoad extends AsyncTask<Void, Void, Void> {
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        String pack = getProfile.get(0).getApp_package_name();
        Context context;

        public appLoad(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Item item = new Item();
            boolean isTagboxInstalled = superClass.appInstalled(pack);
            if (isTagboxInstalled) {
                try {
                    apps = manager.getApplicationInfo(pack, PackageManager.GET_META_DATA);
                    item.icon = manager.getApplicationIcon(apps);
                    item.label = apps.packageName;
                    item.name = manager.getApplicationLabel(apps);
                    list.add(item);
                } catch (PackageManager.NameNotFoundException ne) {
                    ne.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            gridAdapter = new GridAdapter(MainActivity.this, list);
            appsGrid.setAdapter(gridAdapter);
            appsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = manager.getLaunchIntentForPackage(list.get(position).label.toString());
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //On settings menu click
            case R.id.infoMenu:
                Intent infoIntent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(infoIntent);
                break;
            //On refresh menu click
            case R.id.refreshMenu:
                superClass.hideNavToggle();
                finish();
                startActivity(getIntent());
                break;
            //On exit menu click
            case R.id.exitMenu:
                appCountDownTimer.cancel();
                taglockDeviceInfo.exitApp();
                return true;
            //On wifi menu click
            case R.id.wifiMenu:
                Intent wifiIntent = new Intent(MainActivity.this, WifiActivity.class);
                startActivity(wifiIntent);
                break;
            //On logout menu click
            case R.id.clearMenu:
                taglockDeviceInfo.clearData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            final DefaultProfileController defaultProfileController = new DefaultProfileController();
            RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
            final String pack = getProfile.get(0).getApp_package_name();
            boolean isTagboxInstalled = superClass.appInstalled(pack);
            if (isTagboxInstalled) {
                appCountDownTimer.start();
            }
            {
                Log.d("Status: ", "APK is not installed");
            }
        } else {
            appCountDownTimer.cancel();
        }
    }

    //On activity resume
    @Override
    protected void onResume() {
        super.onResume();
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        final String pack = getProfile.get(0).getApp_package_name();
        boolean isTagboxInstalled = superClass.appInstalled(pack);
        if (isTagboxInstalled) {
            appCountDownTimer.start();
        }
        {
            apkManagement.getApk();
        }
    }

    //On activity destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(apkManagement.downloadReceiver);
        unregisterReceiver(batteryInfo);
        unregisterReceiver(mReceiver);
    }

    //To install apk with given path
    public static class InstallApp extends AsyncTask<Void, Void, Void> {

        DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        String pack = getProfile.get(0).getApp_package_name();
        @SuppressLint("StaticFieldLeak")
        Context context;
        String fileName;

        public InstallApp(Context context, String fileName) {
            this.context = context;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            Log.d("APK Status", "APK is installing...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SuperClass.installApp(fileName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (superClass.appInstalled(pack)) {
                ((Activity) context).finish();
                context.startActivity(((Activity) context).getIntent());
                PreferenceHelper.setValueBoolean(context, AppConfig.INSTALL_STATUS, true);
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(pack);
                context.startActivity(intent);
            } else {
                PreferenceHelper.setValueBoolean(context, AppConfig.INSTALL_STATUS, false);
                Log.d("Status: ", "APK is not installed");
            }
        }
    }

    //To update apk with given path
    public static class UpdateApp extends AsyncTask<Void, Void, Void> {
        DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        String pack = getProfile.get(0).getApp_package_name();
        @SuppressLint("StaticFieldLeak")
        Context context;
        String fileName;

        public UpdateApp(Context context, String fileName) {
            this.context = context;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            Log.d("APK Status", "APK is updating...");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            SuperClass.updateApp(fileName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (superClass.appInstalled(pack)) {
                PreferenceHelper.setValueBoolean(context, AppConfig.UPDATE_STATUS, true);
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(pack);
                context.startActivity(intent);
            } else {
                PreferenceHelper.setValueBoolean(context, AppConfig.UPDATE_STATUS, false);
                Log.d("Status", "APK is not Updated");
            }
        }
    }
}
