package com.tagloy.taglock.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.tagloy.taglock.R;
import com.tagloy.taglock.activity.MainActivity;
import com.tagloy.taglock.realmcontrollers.DefaultProfileController;
import com.tagloy.taglock.realmmodels.DefaultProfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import io.realm.RealmResults;

public class SuperClass {

    public static final String ACTION_INSTALL_COMPLETE = "com.tagloy.taglock.INSTALL_COMPLETE";
    Context context;
    public SuperClass(Context context){
        this.context = context;
    }

    //To grant root permission for the app
    public static void grantRoot(){
        try{
            Process root = Runtime.getRuntime().exec("su");
        }catch (IOException ie){
            ie.printStackTrace();
        }
    }

    //To restart device using SuperUser commands
    public void restartDevice(){
        try{
            Process reboot = Runtime.getRuntime().exec(new String[]{"su","-c","reboot"});
            reboot.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    //To shutdown device using SuperUser commands
    public void shutdownDevice(){
        try{
            Process reboot = Runtime.getRuntime().exec(new String[]{"su","-c","reboot -p"});
            reboot.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    //To check if particular app is running
    public static boolean isAppRunning(final Context context,final String packageName){
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos != null){
            for (ActivityManager.RunningAppProcessInfo procInfo : processInfos){
                if (procInfo.processName.equals(packageName)){
                    return true;
                }
            }
        }
        return false;
    }
    //To check if particular app is installed in device
    public boolean appInstalled(String uri){
        PackageManager packageManager = context.getPackageManager();
        try{
            packageManager.getPackageInfo(uri,PackageManager.GET_ACTIVITIES);
            return true;
        }catch (PackageManager.NameNotFoundException in){
            in.printStackTrace();
        }
        return false;
    }

    //To install app from given apk name
    public static void installApp(String apkName){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "pm install /storage/emulated/0/taglock/" + apkName});
            root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    //To clear installed app data
    public static void clearData(){
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        final String packageName = getProfile.get(0).getApp_package_name();
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "pm clear " + packageName});
            File dir = new File(Environment.getExternalStorageDirectory() + "/tagloy/");
            File dir1 = new File(Environment.getExternalStorageDirectory() + "/.tagsignage/");
            if (dir.exists()){
                deleteDir(dir);
            }else if (dir1.exists()){
                deleteDir(dir1);
            }
            root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    //To delete directory
    public static boolean deleteDir(File dir) {
        try {
            if (dir.isDirectory() && dir.exists()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
    //To update app from given apk name
    public static void updateApp(String apkName){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "pm install -r /storage/emulated/0/taglock/" + apkName});
            root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    //To hide the navigation bar and status bar
    public void hideNavToggle(){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "settings put global policy_control immersive.full=*"});
            root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    //To show the navigation bar and status bar
    public void showNavToggle(){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "settings put global policy_control null*"});
            root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    //To enable unknown sources in device
    public void enableUnknownSource(){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","settings put secure install_non_market_apps 1"});
            root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    //To enable write secure settings
    public void enableWriteSettings(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.WRITE_SECURE_SETTINGS"});
            root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    public boolean checkPermission(String permission){
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    //Disable MainActivity on exiting app
    public static void disableActivity(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, MainActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    //Enable MainActivity on opening app
    public static void enableActivity(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, MainActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    //To Hide the Default Launcher app
    public void hideDefaultLauncher(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "pm disable " + packageName});
            root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }

    //To UnHide the Default Launcher app
    public void unHideDefaultLauncher(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "pm enable " + packageName});
            root.waitFor();
        }catch (IOException ie){
            ie.printStackTrace();
        }catch (InterruptedException ine){
            ine.printStackTrace();
        }
    }
}
