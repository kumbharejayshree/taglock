package com.tagloy.taglock.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tagloy.taglock.activity.MainActivity;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {
    SuperClass superClass;

    @Override
    public void onReceive(Context context, Intent intent) {
        superClass = new SuperClass(context);
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            boolean phone = superClass.checkPermission(Manifest.permission.READ_PHONE_STATE);
            boolean location = superClass.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            boolean coarseLocation = superClass.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            boolean storage = superClass.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            boolean wStorage = superClass.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (!phone){
                superClass.enablePhoneCalls(context.getPackageName());
                superClass.enablePhoneState(context.getPackageName());
            }
            if (!location || !coarseLocation){
                superClass.enableLocation(context.getPackageName());
                superClass.enableCoarseLocation(context.getPackageName());
            }
            if (!wStorage || !storage){
                superClass.enableStorage(context.getPackageName());
                superClass.enableReadStorage(context.getPackageName());
            }
            boolean deviceStatus = SuperClass.isAppRunning(context, context.getPackageName());
            if (deviceStatus) {
                boolean taglock = PreferenceHelper.getValueBoolean(context,AppConfig.IS_ACTIVE);
                if (taglock){
                    SuperClass.enableActivity(context);
                    Intent intent1 = new Intent(context, MainActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent1);
                }
            }
        }
    }
}
