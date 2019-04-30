package com.tagloy.taglock.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import java.io.IOException;

public class PermissionsClass {
    Context context;
    public PermissionsClass(Context context){
        this.context = context;
    }
    public static void getUnknownSourcePermission(Context context,Activity activity, String permission, Integer requestCode){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,new String[]{permission},requestCode);
            try{
                Process root = Runtime.getRuntime().exec(new String[]{"su","settings", "put", "global", "install_non_market_apps 1"});
                root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
        }else{
            activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
        }
    }

    public void getPermission(Context context,Activity activity, String permission, Integer requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }
}
