package com.tagloy.taglock.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tagloy.taglock.R;
import com.tagloy.taglock.adapters.PermissionsAdapter;
import com.tagloy.taglock.models.Permissions;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PermissionsClass;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;
import com.tagloy.taglock.receiver.TaglockAdminReceiver;
import com.tagloy.taglock.utils.TaglockDeviceInfo;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AdminActivity extends AppCompatActivity {

    DevicePolicyManager devicePolicyManager;
    ComponentName devicePolicyAdmin;
    PermissionsClass permissionsClass;
    SharedPreferences sharedPreferences;
    SuperClass superClass;
    TaglockDeviceInfo taglockDeviceInfo;
    protected static final int REQUEST_ENABLE = 15;
    private static final int REQUEST_USAGE_ACCESS = 101;
    private static final int REQUEST_USAGE = 102;
    private static final int REQUEST_APP_NOTIFICATION = 103;
    private static final int REQUEST_SYSTEM_ALERT = 105;
    private static final int REQUEST_WRITE_SETTING = 106;
    private static final int REQUEST_OVERLAY = 16;
    private static final int REQUEST_SETTING = 123;
    private static final int REQUEST_NOTIFICATION = 130;
    private static final int REQUEST_PERMISSIONS = 131;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int WRITE_SECURE_SETTINGS = 204;
    private static final int REQUEST_INSTALLED_UNKNOWN_PACKGE = 205;
    ListView permissionListView;
    TextView submitPermission;
    List<Permissions> permissions = new ArrayList<>();
    PermissionsAdapter permissionsAdapter;
    public static boolean b = false;
    public static boolean a = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyAdmin = new ComponentName(this, TaglockAdminReceiver.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        superClass = new SuperClass(this);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        permissionsClass = new PermissionsClass(this);
        permissionsAdapter = new PermissionsAdapter(this, permissions);

        if (isMyPolicyActive()) {
            if (Shell.rootAccess()) {
                SuperClass.grantRoot();
                superClass.enableUnknownSource();
                superClass.enableWriteSettings(getPackageName());
                boolean phone = superClass.checkPermission(READ_PHONE_STATE);
                boolean location = superClass.checkPermission(ACCESS_FINE_LOCATION);
                boolean coarseLocation = superClass.checkPermission(ACCESS_COARSE_LOCATION);
                boolean storage = superClass.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                boolean wStorage = superClass.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (phone && location && storage && coarseLocation) {
                    PreferenceHelper.setValueBoolean(this, AppConfig.IS_ACTIVE, true);
                    SuperClass.enableActivity(AdminActivity.this);
                    Intent intent = new Intent(AdminActivity.this, NetworkActivity.class);
                    startActivity(intent);
                } else {
                    if (!phone) {
                        superClass.enablePhoneCalls(getPackageName());
                        superClass.enablePhoneState(getPackageName());
                    }
                    if (!location || !coarseLocation) {
                        superClass.enableLocation(getPackageName());
                        superClass.enableCoarseLocation(getPackageName());
                    }
                    if (!storage || !wStorage) {
                        superClass.enableStorage(getPackageName());
                        superClass.enableReadStorage(getPackageName());
                    }
                }
            } else {
                //taglockDeviceInfo.showMessage("Please grant admin permissions");
            }
        } else {

        }


        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        permissionListView = findViewById(R.id.permissionsList);
        permissionListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devicePolicyAdmin);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation));
                startActivityForResult(intent, REQUEST_ENABLE);
            } else if (position == 1) {
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(this, this, Manifest.permission.PACKAGE_USAGE_STATS, REQUEST_USAGE_ACCESS);
                }
            } else if (position == 2) {
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(this, this, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, REQUEST_APP_NOTIFICATION);
                }
            } else if (position == 3) {
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(this, this, Manifest.permission.REQUEST_INSTALL_PACKAGES, REQUEST_INSTALLED_UNKNOWN_PACKGE);
                }
            } else if (position == 4) {
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(this, this, Manifest.permission.SYSTEM_ALERT_WINDOW, REQUEST_SYSTEM_ALERT);
                } else if (position == 5) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(this, this, Manifest.permission.WRITE_SECURE_SETTINGS, REQUEST_SETTING);
                    }
                }


            }
        });
//        View footerView = ((LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_layout,null,false);
//        grantButton = footerView.findViewById(R.id.grantButton);
        /*if (isMyPolicyActive()) {
            boolean phone = superClass.checkPermission(Manifest.permission.READ_PHONE_STATE);
            boolean location = superClass.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            boolean coarseLocation = superClass.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            boolean storage = superClass.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            boolean wStorage = superClass.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (phone && location && storage && coarseLocation) {
                PreferenceHelper.setValueBoolean(this, AppConfig.IS_ACTIVE, true);
                SuperClass.enableActivity(AdminActivity.this);
                Intent intent = new Intent(AdminActivity.this, NetworkActivity.class);
                startActivity(intent);
            } else {
                if (!phone) {
                    superClass.enablePhoneCalls(getPackageName());
                    superClass.enablePhoneState(getPackageName());
                }
                if (!location || !coarseLocation) {
                    superClass.enableLocation(getPackageName());
                    superClass.enableCoarseLocation(getPackageName());
                }
                if (!storage || !wStorage) {
                    superClass.enableStorage(getPackageName());
                    superClass.enableReadStorage(getPackageName());
                }
            }
        } else {
            taglockDeviceInfo.showMessage("Please grant admin permissions");
        }*/
//        permissionListView.addFooterView(footerView);
        submitPermission = findViewById(R.id.submitPermission);
        submitPermission.setClickable(false);
        new GetPermissions(this).execute();
        enableSubmit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionsAdapter.notifyDataSetChanged();
    }

    // when user clicks Backbutton it will pop confirmation Dialog Box by gourav on 27012021
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ENABLE:
                    taglockDeviceInfo.showMessage("Admin permission granted");
                    finish();
                    startActivity(getIntent());
                    break;
                case REQUEST_USAGE:
                    taglockDeviceInfo.showMessage("Usage access granted");
                    finish();
                    break;
                case REQUEST_SETTING:
                    taglockDeviceInfo.showMessage("Write settings permission granted");
                    a = true;
                    Log.e("nssasaxjz", String.valueOf(a));
                    finish();
                    startActivity(getIntent());
                    break;
                case REQUEST_OVERLAY:
                    taglockDeviceInfo.showMessage("Overlay permission granted");
                    finish();
                    startActivity(getIntent());
                    break;
                case REQUEST_NOTIFICATION:
                    taglockDeviceInfo.showMessage("Overlay permission granted");
                    finish();
                    startActivity(getIntent());
                    break;
                case REQUEST_INSTALLED_UNKNOWN_PACKGE:
                    taglockDeviceInfo.showMessage("Install permission granted");
                    b = true;
                    Log.e("nxjz", String.valueOf(b));
                    finish();
                    startActivity(getIntent());
                    break;



            }
        }
    }

    public void enableSubmit() {
        submitPermission.setClickable(true);
        submitPermission.setTextColor(getResources().getColor(R.color.tagColor));
        submitPermission.setOnClickListener(v -> {
            if (Shell.rootAccess()) {
                if (isMyPolicyActive()) {
                    boolean phone = superClass.checkPermission(READ_PHONE_STATE);
                    boolean location = superClass.checkPermission(ACCESS_COARSE_LOCATION);
                    boolean storage = superClass.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (phone && location && storage) {
                        SuperClass.enableActivity(AdminActivity.this);
                        Intent intent = new Intent(AdminActivity.this, NetworkActivity.class);
                        startActivity(intent);
                    } else {
                        taglockDeviceInfo.showMessage("Please Grant root permission and restart the application!");
                    }
                } else
                    taglockDeviceInfo.showMessage("Please grant admin permissions");
            } else {
                if (isMyPolicyActive()) {
                    boolean phone = superClass.checkPermission(READ_PHONE_STATE);
                    boolean location = superClass.checkPermission(ACCESS_COARSE_LOCATION);
                    boolean storage = superClass.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (phone && location && storage && b) {
                        SuperClass.enableActivity(AdminActivity.this);
                        Intent intent = new Intent(AdminActivity.this, NetworkActivity.class);
                        startActivity(intent);
                    } else {
                        taglockDeviceInfo.showMessage("Please Grant root permission and restart the application!");
                    }
                } else
                    taglockDeviceInfo.showMessage("Please grant admin permissions");
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_USAGE_ACCESS:
                if (!isAccessGranted()) {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivityForResult(intent, REQUEST_USAGE);
                }
                break;
            case REQUEST_APP_NOTIFICATION:
                boolean isNotificationServiceRunning = isNotificationServiceRunning();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (!isNotificationServiceRunning) {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivityForResult(intent, REQUEST_NOTIFICATION);
                    }
                } else {
                    if (!isNotificationServiceRunning) {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    }
                }
                break;
            case REQUEST_SYSTEM_ALERT:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_OVERLAY);
                }
                break;
            case REQUEST_WRITE_SETTING:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    a = true;
                    startActivityForResult(intent, REQUEST_SETTING);
                }
                break;

            case REQUEST_INSTALLED_UNKNOWN_PACKGE:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    if (!isInstall()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        Log.e("SETDTA", String.valueOf(intent));
                        startActivityForResult(intent, REQUEST_INSTALLED_UNKNOWN_PACKGE);
                    }
                }
                break;

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions() {
        ArrayList<String> permissionsArrayList = new ArrayList<>();
        permissionsArrayList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionsArrayList.add(READ_PHONE_STATE);
        permissionsArrayList.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        permissionsArrayList.add(Manifest.permission.CAMERA);
        permissionsArrayList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionsArrayList.add(Manifest.permission.READ_CONTACTS);
        permissionsArrayList.add(ACCESS_COARSE_LOCATION);
        permissionsArrayList.add(ACCESS_FINE_LOCATION);
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissionsArrayList) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), REQUEST_PERMISSIONS);
    }

    private boolean isNotificationServiceRunning() {
        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }

    private boolean isInstall() {
        ContentResolver contentResolver = getContentResolver();
        String getEnabledListener = Settings.Secure.getString(contentResolver, "enabled_install_listeners");
        String packageName = getPackageName();
        if (getEnabledListener == null || !getEnabledListener.contains(packageName)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isMyPolicyActive() {
        return devicePolicyManager.isAdminActive(devicePolicyAdmin);

    }

    private void PrepareData() {
        Permissions permission = new Permissions("Activate Device Admin", getResources().getString(R.string.admin_permission), getDrawable(R.drawable.ic_person_black_24dp));
        permissions.add(permission);


        if (Build.VERSION.SDK_INT >= 23) {
            permission = new Permissions("Allow Usage Access", getResources().getString(R.string.usage_access), getDrawable(R.drawable.ic_accessibility_black_24dp));
            permissions.add(permission);
        }

        permission = new Permissions("Enable App Notifications", getResources().getString(R.string.app_notification), getDrawable(R.drawable.ic_settings_remote_black_24dp));
        permissions.add(permission);

        permission = new Permissions("Install From Unknown Sources", getResources().getString(R.string.unknown_source), getDrawable(R.drawable.ic_file_download_black_24dp));
        permissions.add(permission);

        if (Build.VERSION.SDK_INT >= 23) {
            permission = new Permissions("Enable Overlay Permission", getResources().getString(R.string.overlay_permission), getDrawable(R.drawable.ic_block_black_24dp));
            permissions.add(permission);
        }

        permission = new Permissions("Enable Write System Settings", getResources().getString(R.string.write_system), getDrawable(R.drawable.ic_settings_black_24dp));
        permissions.add(permission);

        permission = new Permissions("Enable Download Manager", getResources().getString(R.string.download_manager), getDrawable(R.drawable.ic_file_download_black_24dp));
        permissions.add(permission);

        permission = new Permissions("Access Device Details", getResources().getString(R.string.device_details), getDrawable(R.drawable.ic_perm_device_information_black_24dp));
        permissions.add(permission);

//        permission = new Permissions("Access Device Camera", getResources().getString(R.string.device_camera), getDrawable(R.drawable.ic_camera_black_24dp));
//        permissions.add(permission);

        permission = new Permissions("Access Device Storage", getResources().getString(R.string.device_storage), getDrawable(R.drawable.ic_storage_black_24dp));
        permissions.add(permission);

//        permission = new Permissions("Access Contacts", getResources().getString(R.string.contacts_access), getDrawable(R.drawable.ic_contact_phone_black_24dp));
//        permissions.add(permission);

        permission = new Permissions("Access Device Location", getResources().getString(R.string.device_location), getDrawable(R.drawable.ic_location_on_black_24dp));
        permissions.add(permission);

        permissionsAdapter.notifyDataSetChanged();
    }

    public class GetPermissions extends AsyncTask<Void, Void, Void> {
        Context context;

        public GetPermissions(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PrepareData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            permissionListView.setAdapter(permissionsAdapter);
        }
    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{READ_PHONE_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(AdminActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


}
