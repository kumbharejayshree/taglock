package com.tagloy.taglock.activity;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.tagloy.taglock.R;
import com.tagloy.taglock.receiver.TaglockAdminReceiver;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;

public class SplashActivity extends AppCompatActivity {

    ImageView imageView;
    SuperClass superClass;
    DevicePolicyManager devicePolicyManager;
    ComponentName devicePolicyAdmin;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = SplashActivity.this;
        superClass = new SuperClass(this);
        imageView = findViewById(R.id.imageView);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyAdmin = new ComponentName(this, TaglockAdminReceiver.class);
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isMyPolicyActive()){
                    boolean phone = superClass.checkPermission(Manifest.permission.READ_PHONE_STATE);
                    boolean location = superClass.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                    boolean contacts = superClass.checkPermission(Manifest.permission.READ_CONTACTS);
                    boolean camera = superClass.checkPermission(Manifest.permission.CAMERA);
                    boolean storage = superClass.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (phone && location && contacts && camera && storage){
                        SuperClass.enableActivity(mContext);
                        String device_name = PreferenceHelper.getValueString(mContext,AppConfig.DEVICE_NAME);
                        if (!device_name.equals("")){
                            PreferenceHelper.setValueBoolean(mContext,AppConfig.IS_ACTIVE,true);
                            Intent intent = new Intent(mContext,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Intent intent = new Intent(mContext,NetworkActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        PreferenceHelper.setValueBoolean(mContext,AppConfig.IS_ACTIVE,false);
                        startActivity(new Intent(mContext, AdminActivity.class));
                        finish();
                    }
                }else {
                    PreferenceHelper.setValueString(mContext,AppConfig.DEVICE_NAME,"");
                    PreferenceHelper.setValueBoolean(mContext,AppConfig.IS_ACTIVE,false);
                    startActivity(new Intent(mContext, AdminActivity.class));
                    finish();
                }
            }
        }, 3000);
    }
    public boolean isMyPolicyActive() {
        return devicePolicyManager.isAdminActive(devicePolicyAdmin);
    }
}
