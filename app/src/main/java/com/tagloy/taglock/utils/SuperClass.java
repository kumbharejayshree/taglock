package com.tagloy.taglock.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.tagloy.taglock.activity.MainActivity;
import com.tagloy.taglock.realmcontrollers.DefaultProfileController;
import com.tagloy.taglock.realmmodels.DefaultProfile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import io.realm.RealmResults;

public class SuperClass {

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
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To shutdown device using SuperUser commands
    public void shutdownDevice(){
        try{
            Process reboot = Runtime.getRuntime().exec(new String[]{"su","-c","reboot -p"});
            reboot.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
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
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "pm install /storage/emulated/0/.taglock/" + apkName});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
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
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
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
    public static void updateApp(Context context,String apkName){
        try{
            Log.d("D","Yes");
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "pm install -r /storage/emulated/0/.taglock/" + apkName});
            PreferenceHelper.setValueBoolean(context,AppConfig.TAGLOCK_INSTALL_STATUS,true);
            PreferenceHelper.setValueBoolean(context,AppConfig.UPDATE_STATUS,true);
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To forget network
    public void forgetNetwork(int networkId){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "wpa_cli remove_network " + networkId});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To hide the navigation bar and status bar
    public void hideNavToggle(){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "settings put global policy_control immersive.full=*"});
            root.waitFor();
            PreferenceHelper.setValueBoolean(context,AppConfig.IS_NAV_VISIBLE,false);
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To show the navigation bar and status bar
    public void showNavToggle(){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "settings put global policy_control null*"});
            root.waitFor();
            PreferenceHelper.setValueBoolean(context,AppConfig.IS_NAV_VISIBLE,true);
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable unknown sources in device
    public void enableUnknownSource(){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","settings put secure install_non_market_apps 1"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable write secure settings
    public void enableWriteSettings(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.WRITE_SECURE_SETTINGS"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable camera
    public void enableCamera(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.CAMERA"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable storage
    public void enableStorage(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.WRITE_EXTERNAL_STORAGE"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable read storage
    public void enableReadStorage(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.READ_EXTERNAL_STORAGE"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable contacts
    public void enableContacts(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.WRITE_CONTACTS"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable location
    public void enableLocation(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.ACCESS_FINE_LOCATION"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable location
    public void enableCoarseLocation(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.ACCESS_COARSE_LOCATION"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable phone state
    public void enablePhoneState(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.READ_PHONE_STATE"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable phone calls
    public void enablePhoneCalls(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.PROCESS_OUTGOING_CALLS"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To enable read contacts
    public void enableReadContacts(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[]{"su","-c","pm grant " + packageName + " android.permission.READ_CONTACTS"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
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
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To UnHide the Default Launcher app
    public void unHideDefaultLauncher(String packageName){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "pm enable " + packageName});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //To Switch Android debugging
    public void switchDebugging(int set){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "settings put global adb_enabled " + set});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //Hide navbar
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void hideNav(){
        try{
            show();
            Process root = null;
            Display display =
                    ((WindowManager) Objects.requireNonNull(context.getSystemService(Context.WINDOW_SERVICE)))
                            .getDefaultDisplay();
            int rot = display.getRotation();

            switch (rot){
                case Surface
                        .ROTATION_0:
                    Log.d("Rot", "0");
                    root = Runtime.getRuntime().exec(new String[] {"su", "-c", "wm overscan 0,0,0,-48"});
                    break;
                case Surface
                        .ROTATION_90:
                    Log.d("Rot", "90");
                    root = Runtime.getRuntime().exec(new String[] {"su", "-c", "wm overscan -48,0,0,0"});
                    break;
                case Surface
                        .ROTATION_180:
                    Log.d("Rot", "180");
                    root = Runtime.getRuntime().exec(new String[] {"su", "-c", "wm overscan 0,-48,0,0"});
                    break;
                case Surface
                        .ROTATION_270:
                    Log.d("Rot", "270");
                    root = Runtime.getRuntime().exec(new String[] {"su", "-c", "wm overscan 0,0,-48,0"});
                    break;
            }

            assert root != null;
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }

    //Show navbar
    public void show(){
        try{
            Process root = Runtime.getRuntime().exec(new String[] {"su", "-c", "wm overscan 0,0,0,0"});
            root.waitFor();
        }catch (IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }
}
