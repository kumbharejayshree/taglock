package com.tagloy.taglock.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.tagloy.taglock.R;
import com.tagloy.taglock.receiver.TaglockAdminReceiver;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;
import com.topjohnwu.superuser.Shell;

public class SplashActivity extends AppCompatActivity {

    ImageView imageView;
    SuperClass superClass;
    DevicePolicyManager devicePolicyManager;
    ComponentName devicePolicyAdmin;
    Context mContext;
    private int STORAGE_PERMISSION_CODE = 1;
    public static boolean  rooted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = SplashActivity.this;
        superClass = new SuperClass(this);
        imageView = findViewById(R.id.imageView);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyAdmin = new ComponentName(this, TaglockAdminReceiver.class);
        PreferenceHelper.setValueInt(mContext, AppConfig.FAILED_COUNT, 0);
        PreferenceHelper.setValueBoolean(mContext, AppConfig.HDMI_STATUS, true);
        PreferenceHelper.setValueBoolean(mContext, AppConfig.IS_LOCKED, true);
        Handler handler = new Handler();
        if(SuperClass.isRooted()){
          rooted = true;
        }

        handler.postDelayed(() -> {
            if (isMyPolicyActive()) {
                SuperClass.enableActivity(mContext);
                boolean phone = superClass.checkPermission(Manifest.permission.READ_PHONE_STATE);
                boolean coarseLocation = superClass.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                boolean location = superClass.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
//                boolean contacts = superClass.checkPermission(Manifest.permission.READ_CONTACTS);
//                boolean camera = superClass.checkPermission(Manifest.permission.CAMERA);
                boolean storage = superClass.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
//                boolean wContacts = superClass.checkPermission(Manifest.permission.WRITE_CONTACTS);
                boolean wStorage = superClass.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (phone && location && wStorage && coarseLocation) {
                    String device_name = PreferenceHelper.getValueString(mContext, AppConfig.DEVICE_NAME);
                    Log.e("NAME DEVICE",device_name);
                    if (device_name != null) {
                        PreferenceHelper.setValueBoolean(mContext, AppConfig.IS_ACTIVE, true);
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(mContext, NetworkActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    PreferenceHelper.setValueBoolean(mContext, AppConfig.IS_ACTIVE, true);
                    if (!phone) {
                        superClass.enablePhoneCalls(getPackageName());
                        superClass.enablePhoneState(getPackageName());
                    }
                    if (!location || !coarseLocation) {
                        superClass.enableLocation(getPackageName());
                        superClass.enableCoarseLocation(getPackageName());
                    }
//                    if (!contacts || !wContacts){
//                        superClass.enableReadContacts(getPackageName());
//                        superClass.enableContacts(getPackageName());
//                    }
//                    if (!camera){
//                        superClass.enableCamera(getPackageName());
//                    }
                    if (!storage || !wStorage) {
                        superClass.enableStorage(getPackageName());
                        superClass.enableReadStorage(getPackageName());
                    }
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            } else {
                PreferenceHelper.setValueBoolean(mContext, AppConfig.IS_ACTIVE, false);
                Intent intent = new Intent(mContext, AdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }


        }, 3000);
    }

    public boolean isMyPolicyActive() {
        return devicePolicyManager.isAdminActive(devicePolicyAdmin);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SplashActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            PreferenceHelper.setValueBoolean(mContext, AppConfig.IS_ACTIVE, false);
                            Intent intent = new Intent(mContext, AdminActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

}
