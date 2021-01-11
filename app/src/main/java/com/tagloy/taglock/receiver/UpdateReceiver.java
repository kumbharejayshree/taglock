package com.tagloy.taglock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tagloy.taglock.activity.SplashActivity;
import com.tagloy.taglock.utils.SuperClass;

import java.util.Objects;

public class UpdateReceiver extends BroadcastReceiver {

    SuperClass superClass;
    @Override
    public void onReceive(Context context, Intent intent) {
        superClass = new SuperClass(context);
        if (Objects.equals(intent.getAction(), Intent.ACTION_MY_PACKAGE_REPLACED)){
            Log.d("Update", "Success");
            superClass.restartDevice();
//            Intent launcherIntent = new Intent(context, SplashActivity.class);
//            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(launcherIntent);

        }
    }
}
