package com.tagloy.taglock.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;
import io.realm.RealmResults;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements LocationListener {

    Context context = MainActivity.this;
    DeviceInfoController deviceInfoController = new DeviceInfoController();
    DeviceInformation deviceInformation, deviceData;
    RecentAppClickReceiver mReceiver = new RecentAppClickReceiver();
    PermissionsClass permissionsClass;
    PackageManager manager;
    TaglockDeviceInfo taglockDeviceInfo;
    LocationManager locationManager;
    WifiManager wifiManager;
    List<Item> list = new ArrayList<>();
    public static GridAdapter gridAdapter;
    RelativeLayout mainLayout;
    public static GridView appsGrid;
    ImageView wallapaperImage;
    TextView versionText, ipText;
    ApplicationInfo apps;
    String provider, ip, versionName;
    public GifImageView downloadProgress;
    private static final String TAG = "MainActivity";
    private static SuperClass superClass;
    int app_call_duration;
    Timer updateTimer, wifiTimer;
    TimerTask updateTimerTask, wifiTimerTask;
    CountDownTimer updateCountDownTimer, appCountDownTimer;
    ApkManagement apkManagement;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainLayout = findViewById(R.id.mainLayout);
        appsGrid = findViewById(R.id.appsGrid);
        manager = getPackageManager();
        permissionsClass = new PermissionsClass(this);
        gridAdapter = new GridAdapter(this, list);
        downloadProgress = findViewById(R.id.downloadProgress);
        versionText = findViewById(R.id.versionText);
        ipText = findViewById(R.id.ipText);
        wallapaperImage = findViewById(R.id.wallpaperImageView);
        superClass = new SuperClass(this);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        apkManagement = new ApkManagement(this);
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        final String pack = getProfile.get(0).getApp_package_name();
        app_call_duration = getProfile.get(0).getDefault_apk_call_duration();
        logUser();
        String taglockVersion = TaglockDeviceInfo.getVersion(this,getPackageName());
        boolean isDefaultInstalled = superClass.appInstalled(pack);
        if (isDefaultInstalled) {
            versionName = TaglockDeviceInfo.getVersion(this,pack);
            PreferenceHelper.setValueString(context,AppConfig.APK_VERSION,versionName);
            try {
                apps = manager.getApplicationInfo(pack, PackageManager.GET_META_DATA);
                CharSequence name = manager.getApplicationLabel(apps);
                versionText.setText("Taglock Version: " + taglockVersion +  "\n" + name +  " Version: " + versionName);
            } catch (PackageManager.NameNotFoundException ne) {
                ne.printStackTrace();
            }
        } else {
            versionName = "NULL";
            versionText.setText("Taglock Version: " + taglockVersion);
        }

        //If debugging is enabled, then disable it
//        if(Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) == 1) {
//            // debugging enabled
//            superClass.switchDebugging(0);
//        }

        //Checking if app is running at time interval of given time
        appCountDownTimer = new CountDownTimer(app_call_duration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (!SuperClass.isAppRunning(context, pack)) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(pack);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Waiting...");
                }
            }
        };
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String packageName = PreferenceHelper.getValueString(this, AppConfig.DEVICE_LAUNCHER);
        superClass.hideDefaultLauncher(packageName);
        superClass.hideNavToggle();
        if (Build.VERSION.SDK_INT>=23)
            taglockDeviceInfo.hideStatusBar();
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
        String wallpaper = PreferenceHelper.getString(context,AppConfig.GROUP_WALLPAPER);
        if (wallpaper == null){
            wallapaperImage.setBackgroundColor(getResources().getColor(R.color.blackWall));
            PreferenceHelper.setValueString(context,AppConfig.GROUP_WALLPAPER,"test");
            PreferenceHelper.setValueBoolean(context,AppConfig.WALLPAPER_DOWN_STATUS, false);
        } else {
            boolean wall_downloaded = PreferenceHelper.getValueBoolean(context,AppConfig.WALLPAPER_DOWN_STATUS);
            if (wall_downloaded){
                File imageFile = new File("/storage/emulated/0/.taglock/" + wallpaper);
                if (imageFile.exists()){
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    wallapaperImage.setImageBitmap(bitmap);
                }
            }
        }
        deviceInformation = taglockDeviceInfo.updateDetails();
        deviceData = taglockDeviceInfo.deviceData();
        taglockDeviceInfo.deviceDetails(deviceInformation);
        boolean realmDevice = deviceInfoController.isDeviceAvailable();
        if (realmDevice) {
            deviceInfoController.updateDeviceData(deviceInformation);
        } else {
            deviceInfoController.addDeviceData(deviceInformation);
        }

        boolean isActive = PreferenceHelper.getValueBoolean(this,AppConfig.IS_ACTIVE);
        //If default app is installed, open it
        if (isDefaultInstalled) {
            new appLoad(context).execute();
            if (isActive){
                appCountDownTimer.start();
                startUpdateTimer();
                startWifiTimer();
            }else {
                stopUpdateTimer();
                stopWifiTimer();
            }
            taglockDeviceInfo.updateDevice(deviceData);
//            taglockDeviceInfo.deviceSession(deviceData);
        } else {
            //Else download default app
            PreferenceHelper.removeStringValue(this, AppConfig.APK_NAME);
            apkManagement.getApk();
        }

        //Checking for new apk and download if available
        updateCountDownTimer =  new CountDownTimer(5*60*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                apkManagement.getApk();
                apkManagement.getTaglock();
                start();
            }
        };
        updateCountDownTimer.start();
        IntentFilter mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mReceiver, mFilter);
        registerReceiver(connectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(apkManagement.downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(batteryInfo, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(taglockDeviceInfo.downloadReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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
                    Integer ipAddress = taglockDeviceInfo.getIpAddress();
                    ip = taglockDeviceInfo.intToIp(ipAddress);
                }else if (taglockDeviceInfo.isEthernetConnected()){
                    ip = taglockDeviceInfo.getIp();
                }else {
                    ip = "NA";
                }
                ipText.setText("IPAddress: " + ip);
                if(!taglockDeviceInfo.isNetworkConnected()) {
                    ipText.append("(Not connected to internet)");
                }else{
                    if (taglockDeviceInfo.isEthernetConnected()){
                        ipText.append("(LAN)");
                    }else if (taglockDeviceInfo.isWifiConnected()){
                        ipText.append("(WiFi)");
                    }
                }
            }else {
                ip = "NA";
                ipText.setText("IPAddress: " + ip);
                ipText.append("(Not connected to internet)");
            }
        }
    };

    public void forceCrash(MenuItem menuItem) {
        throw new RuntimeException();
    }

    //Log user for Fabric
    private void logUser() {
        String device_name = PreferenceHelper.getValueString(context, AppConfig.DEVICE_NAME);
        String device_group = PreferenceHelper.getValueString(context, AppConfig.DEVICE_GROUP);
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

    //Save location on locationChange
    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(lon);
        PreferenceHelper.setValueString(context, AppConfig.LATITUDE, latitude);
        PreferenceHelper.setValueString(context, AppConfig.LONGITUDE, longitude);
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

    //Start timer to update device data
    public void startUpdateTimer(){
        updateTimer = new Timer();
        initializeUpdateTask();
        updateTimer.schedule(updateTimerTask,5*60*1000, 5*60*1000);
    }

    //Start timer to update session
    public void startSessionTimer(){
        updateTimer = new Timer();
        initializeUpdateTask();
        updateTimer.schedule(updateTimerTask,60*60*1000, 60*60*1000);
    }

    //Start timer to check Wifi
    public void startWifiTimer(){
        wifiTimer = new Timer();
        initializeWifiTask();
        wifiTimer.schedule(wifiTimerTask,10*60*1000, 10*60*1000);
    }

    //Initialize update data task
    public void initializeUpdateTask(){
        updateTimerTask = new TimerTask() {
            @Override
            public void run() {
                taglockDeviceInfo.updateDevice(deviceData);
            }
        };
    }

    //Initialize session task
    public void initializeSessionTask(){
        updateTimerTask = new TimerTask() {
            @Override
            public void run() {
                taglockDeviceInfo.deviceSession(deviceData);
            }
        };
    }

    //Initialize check Wifi task
    public void initializeWifiTask(){
        wifiTimerTask = new TimerTask() {
            @Override
            public void run() {
                PreferenceHelper.setValueBoolean(context,AppConfig.IS_LOCKED,true);
                if (!taglockDeviceInfo.isEthernetConnected()) {
                    if (!taglockDeviceInfo.isNetworkConnected()) {
                        wifiManager.setWifiEnabled(false);
                        wifiManager.setWifiEnabled(true);
                    }
                }
            }
        };
    }

    //Stop timer to update device data
    public void stopUpdateTimer(){
        if (updateTimer!=null){
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    //Stop timer to check Wifi
    public void stopWifiTimer(){
        if (wifiTimer!=null){
            wifiTimer.cancel();
            wifiTimer = null;
        }
    }

    //Load apps in GridView dynamically
    public class appLoad extends AsyncTask<Void, Void, Void> {
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        String pack = getProfile.get(0).getApp_package_name();
        Context context;

        private appLoad(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Item item = new Item();
            boolean isDefaultInstalled = superClass.appInstalled(pack);
            if (isDefaultInstalled) {
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
            appsGrid.setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();
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
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(connectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void exitTag(){
        stopUpdateTimer();
        appCountDownTimer.cancel();
        stopWifiTimer();
        taglockDeviceInfo.exitApp();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        final int passcode = getProfile.get(0).getPasscode();
        View view = getLayoutInflater().inflate(R.layout.alert_dialog, null);
        final EditText alertEdit = view.findViewById(R.id.alertEdit);
        switch (item.getItemId()) {
            //On settings menu click
            case R.id.infoMenu:
                Intent infoIntent = new Intent(context, InfoActivity.class);
                startActivity(infoIntent);
                break;
            //On exit menu click
            case R.id.exitMenu:
                boolean is_locked = PreferenceHelper.getValueBoolean(context,AppConfig.IS_LOCKED);
                if (is_locked){
                    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("Enter Passcode")
                            .setMessage("Are you sure you want to exit?")
                            .setView(view)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (TextUtils.isEmpty(alertEdit.getText())) {
                                        Toast.makeText(context, "Please enter passcode", Toast.LENGTH_LONG).show();
                                    } else if (Integer.parseInt(alertEdit.getText().toString()) == passcode) {
                                        exitTag();
                                    } else {
                                        Toast.makeText(context, "Incorrect passcode!", Toast.LENGTH_LONG).show();
                                        int count = PreferenceHelper.getValueInt(context, AppConfig.FAILED_COUNT);
                                        if (count>=5){
                                            SuperClass.clearData();
                                        }else {
                                            count = count + 1;
                                            Toast.makeText(context, "Failed attempts: " + count, Toast.LENGTH_LONG).show();
                                            PreferenceHelper.setValueInt(context, AppConfig.FAILED_COUNT, count);
                                        }
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
                }else {
                    exitTag();
                }
                break;
            //On network settings menu click
            case R.id.settingsMenu:
                Intent settingIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(settingIntent);
                break;
            //On logout menu click
            case R.id.clearMenu:
                taglockDeviceInfo.clearData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean isCallable(Activity activity, Intent intent) {
        List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @Override
    public void onBackPressed() {
    }

    //Window focus change
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        boolean isActive = PreferenceHelper.getValueBoolean(context,AppConfig.IS_ACTIVE);
        if (isActive){
            if (hasFocus) {
                final DefaultProfileController defaultProfileController = new DefaultProfileController();
                RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
                final String pack = getProfile.get(0).getApp_package_name();
                boolean isDefaultInstalled = superClass.appInstalled(pack);
                if (isDefaultInstalled) {
                    appCountDownTimer.start();
                }else {
                    Log.d("Status", "APK is not installed");
                    apkManagement.getApk();
                }
            } else {
                appCountDownTimer.cancel();
            }
        }else {
            appCountDownTimer.cancel();
            stopUpdateTimer();
            stopWifiTimer();
        }
    }

    //On activity destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(apkManagement.downloadReceiver);
        unregisterReceiver(batteryInfo);
        unregisterReceiver(mReceiver);
        unregisterReceiver(taglockDeviceInfo.downloadReceiver);
    }

    //To install apk with given path
    public static class InstallApp extends AsyncTask<Void, Void, Void> {
        TaglockDeviceInfo taglockDeviceInfo;
        DeviceInfoController deviceInfoController = new DeviceInfoController();
        DeviceInformation deviceInfo = new DeviceInformation();
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
            Toast.makeText(context,"Installing App in background...",Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SuperClass.installApp(fileName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (superClass.appInstalled(pack)) {
                appsGrid.invalidateViews();
                gridAdapter.notifyDataSetChanged();
                taglockDeviceInfo = new TaglockDeviceInfo(context);
                PreferenceHelper.setValueBoolean(context, AppConfig.INSTALL_STATUS, true);
                String version = TaglockDeviceInfo.getVersion(context,pack);
                PreferenceHelper.setValueString(context,AppConfig.APK_VERSION,version);
                deviceInfo.setApp_download_status(PreferenceHelper.getValueBoolean(context,AppConfig.INSTALL_STATUS));
                deviceInfo.setDefault_apk_version(version);
                deviceInfoController.updateApkDetails(deviceInfo);
                taglockDeviceInfo.updateDevice(deviceInfo);
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
        TaglockDeviceInfo taglockDeviceInfo;
        DeviceInfoController deviceInfoController = new DeviceInfoController();
        DeviceInformation deviceInfo = new DeviceInformation();
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
            PreferenceHelper.setValueBoolean(context,AppConfig.INSTALL_STATUS,false);
            Log.d("APK Status", "APK is updating...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SuperClass.updateApp(context,fileName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            PreferenceHelper.setValueBoolean(context,AppConfig.APK_DOWN_STATUS,false);
            boolean appDownloaded = PreferenceHelper.getValueBoolean(context,AppConfig.UPDATE_STATUS);
            if (appDownloaded) {
                taglockDeviceInfo = new TaglockDeviceInfo(context);
                String version = TaglockDeviceInfo.getVersion(context,pack);
                PreferenceHelper.setValueString(context,AppConfig.APK_VERSION,version);
                deviceInfo.setApp_download_status(PreferenceHelper.getValueBoolean(context,AppConfig.INSTALL_STATUS));
                deviceInfo.setDefault_apk_version(version);
                deviceInfoController.updateApkDetails(deviceInfo);
                taglockDeviceInfo.updateDevice(deviceInfo);
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(pack);
                context.startActivity(intent);
            }
        }
    }

    //To update taglock with given path
    public static class UpdateTaglock extends AsyncTask<Void, Void, Void> {
        TaglockDeviceInfo taglockDeviceInfo;
        @SuppressLint("StaticFieldLeak")
        Context context;
        String fileName;

        public UpdateTaglock(Context context, String fileName) {
            this.context = context;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            PreferenceHelper.setValueBoolean(context,AppConfig.TAGLOCK_INSTALL_STATUS,false);
            Log.d("Taglock Status", "Taglock is updating...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SuperClass.updateApp(context,fileName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            PreferenceHelper.setValueBoolean(context,AppConfig.TAGLOCK_DOWN_STATUS,false);
            taglockDeviceInfo = new TaglockDeviceInfo(context);
            boolean taglockDownloaded = PreferenceHelper.getValueBoolean(context,AppConfig.TAGLOCK_INSTALL_STATUS);
            if (taglockDownloaded) {
                Intent intent = new Intent(context, SplashActivity.class);
                context.startActivity(intent);
            }
        }
    }
}
