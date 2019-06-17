package com.tagloy.taglock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tagloy.taglock.activity.SplashActivity;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean deviceStatus = SuperClass.isAppRunning(context, context.getPackageName());
        if (deviceStatus) {
            boolean taglock = PreferenceHelper.getValueBoolean(context,AppConfig.IS_ACTIVE);
            if (taglock){
                SuperClass.enableActivity(context);
                Intent intent1 = new Intent(context, SplashActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        }
    }
}
