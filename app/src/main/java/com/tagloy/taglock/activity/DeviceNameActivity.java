package com.tagloy.taglock.activity;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.tagloy.taglock.R;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PermissionsClass;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;
import com.tagloy.taglock.receiver.TaglockAdminReceiver;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

import java.util.List;

import io.fabric.sdk.android.Fabric;

public class DeviceNameActivity extends AppCompatActivity implements View.OnClickListener {

    DevicePolicyManager devicePolicyManager;
    ComponentName devicePolicyAdmin;
    EditText deviceNameEdit;
    Button submitNameBtn;
    SuperClass superClass;
    TaglockDeviceInfo taglockDeviceInfo;
    SharedPreferences sharedPreferences;
    PermissionsClass permissionsClass;
    String ParseJson;
    private static final int REQUEST_SYSTEM_ALERT = 105;
    private static final int REQUEST_OVERLAY = 120;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this,new Crashlytics());
        setContentView(R.layout.activity_device_name);
        deviceNameEdit = findViewById(R.id.deviceNameEdit);
        submitNameBtn = findViewById(R.id.submitNameBtn);
        sharedPreferences = getSharedPreferences(AppConfig.TAGLOCK_PREF,Context.MODE_PRIVATE);
        submitNameBtn.setOnClickListener(this);
        superClass = new SuperClass(this);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        permissionsClass = new PermissionsClass(this);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyAdmin = new ComponentName(this, TaglockAdminReceiver.class);
        PreferenceHelper.setValueBoolean(this,AppConfig.IS_ACTIVE,true);
        superClass.hideNavToggle();
        permissionsClass.getPermission(this, this, Manifest.permission.SYSTEM_ALERT_WINDOW, REQUEST_SYSTEM_ALERT);
        if (isMyPolicyActive()){
            boolean phone = superClass.checkPermission(Manifest.permission.READ_PHONE_STATE);
            boolean location = superClass.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            boolean contacts = superClass.checkPermission(Manifest.permission.READ_CONTACTS);
            boolean camera = superClass.checkPermission(Manifest.permission.CAMERA);
            boolean storage = superClass.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (phone && location && contacts && camera && storage){
                SuperClass.enableActivity(this);
                Intent intent = new Intent(DeviceNameActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }else{
                SuperClass.disableActivity(this);
                Intent intent = new Intent(DeviceNameActivity.this,AdminActivity.class);
                startActivity(intent);
                finish();
            }
        }else{
            SuperClass.disableActivity(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submitNameBtn:
                if (TextUtils.isEmpty(deviceNameEdit.getText())){
                    deviceNameEdit.setError("Please enter device name");
                }else if(deviceNameEdit.getText().toString().equals("TL-")){
                    deviceNameEdit.setError("Please enter valid device name");
                }else{
                    final String deviceName = deviceNameEdit.getText().toString().trim();
                    taglockDeviceInfo.checkNameValidity(deviceName);
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==RESULT_OK){
            switch (requestCode){
                case REQUEST_OVERLAY:
                    Toast.makeText(getApplicationContext(), "Overlay permission granted", Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(getIntent());
                    taglockDeviceInfo.hideStatusBar();
                    break;
            }
        }
    }

    public boolean isMyPolicyActive() {
        return devicePolicyManager.isAdminActive(devicePolicyAdmin);
    }
}
