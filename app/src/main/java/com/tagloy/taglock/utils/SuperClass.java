package com.tagloy.taglock.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.tagloy.taglock.BuildConfig;
import com.tagloy.taglock.activity.MainActivity;
import com.tagloy.taglock.realmcontrollers.DefaultProfileController;
import com.tagloy.taglock.realmmodels.DefaultProfile;
import com.topjohnwu.superuser.internal.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import io.realm.RealmResults;

import static com.topjohnwu.superuser.internal.Utils.getContext;

public class SuperClass {
    PowerManager pm;


   public static Context context;

    public SuperClass(Context context) {
        this.context = context;
    }
    private static final int ORIENTATION_0 = 0;
    private static final int ORIENTATION_90 = 3;
    private static final int ORIENTATION_270 = 1;

    public static boolean findBinary(String binaryName) {
        boolean found = false;
        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/",
                "/data/local/xbin/", "/data/local/bin/",
                "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
        for (String where : places) {
            if (new File(where + binaryName).exists()) {
                found = true;

                break;
            }
        }
        return found;
    }

    public static boolean isRooted() {
        return findBinary("su");
    }

    //To grant root permission for the app
    public static void grantRoot() {
        try {
            Process root = Runtime.getRuntime().exec("su");
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    //To restart device using SuperUser commands
    public void restartDevice() {
        try {
            Process reboot = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
            reboot.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To shutdown device using SuperUser commands
    public void shutdownDevice() {
        try {
            Process reboot = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
            reboot.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To check if particular app is running
    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos != null) {
            for (ActivityManager.RunningAppProcessInfo procInfo : processInfos) {
                if (procInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    //To check if particular app is installed in device
    public boolean appInstalled(String uri) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException in) {
            in.printStackTrace();
        }
        return false;
    }

    //To install app from given apk name
    public static void installApp(String apkName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm install /storage/emulated/0/.taglock/" + apkName});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    static String APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.taglock/";

    public static void install(String apkName) {
        File file = new File(APP_DIR + apkName);

        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            String type = "application/vnd.android.package-archive";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri downloadedApk = FileProvider.getUriForFile(context, "com.tagloy.taglock.provider", file);
                intent.setDataAndType(downloadedApk, type);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.fromFile(file), type);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

           context.startActivity(intent);
        } else {
            Toast.makeText(context, "??File not found!", Toast.LENGTH_SHORT).show();
        }
    }



        private static Uri uriFromFile(Context context, File file) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            } else {
                return Uri.fromFile(file);
            }
        }



    //To uninstall app from given apk name by gourav on 25012021
    public static void uninstallApp() {
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        final String packageName = getProfile.get(0).getApp_package_name();
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm uninstall " + packageName});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To clear installed app data
    public static void clearData() {
        final DefaultProfileController defaultProfileController = new DefaultProfileController();
        RealmResults<DefaultProfile> getProfile = defaultProfileController.geDefaultProfileData();
        final String packageName = getProfile.get(0).getApp_package_name();
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm clear " + packageName});
            File dir = new File(Environment.getExternalStorageDirectory() + "/tagloy/");
            File dir1 = new File(Environment.getExternalStorageDirectory() + "/.tagsignage/");
            if (dir.exists()) {
                deleteDir(dir);
            } else if (dir1.exists()) {
                deleteDir(dir1);
            }
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To clear Download Manager App data created by gourav on 21012021
    public static void clearDownloadManager() {
        final String packageName = "com.android.providers.downloads";
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm clear " + packageName});
            root.waitFor();
            Log.d("ClearDownload", " - Success");
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
            Log.e("ClearDownloadM", ie.getMessage());
        }
    }

    public static void getPSK() {
        final String packageName = "com.android.providers.downloads";
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm clear " + packageName});
            root.waitFor();
            Log.d("ClearDownload", " - Success");
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
            Log.e("ClearDownload", ie.getMessage());
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
    public static void updateApp(Context context, String apkName) {
        try {
            Log.d("D", "Yes");
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm install -r /storage/emulated/0/.taglock/" + apkName});
            PreferenceHelper.setValueBoolean(context, AppConfig.TAGLOCK_INSTALL_STATUS, true);
            PreferenceHelper.setValueBoolean(context, AppConfig.UPDATE_STATUS, true);
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To forget network
    public void forgetNetwork(int networkId) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "wpa_cli remove_network " + networkId});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To hide the navigation bar and status bar
    public void hideNavToggle() {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "settings put global policy_control immersive.full=*"});
            root.waitFor();
            PreferenceHelper.setValueBoolean(context, AppConfig.IS_NAV_VISIBLE, false);
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To show the navigation bar and status bar
    public void showNavToggle() {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "settings put global policy_control null*"});
            root.waitFor();
            PreferenceHelper.setValueBoolean(context, AppConfig.IS_NAV_VISIBLE, true);
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable unknown sources in device
    public void enableUnknownSource() {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "settings put secure install_non_market_apps 1"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable write secure settings
    public void enableWriteSettings(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.WRITE_SECURE_SETTINGS"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable camera
    public void enableCamera(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.CAMERA"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable storage
    public void enableStorage(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.WRITE_EXTERNAL_STORAGE"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable read storage
    public void enableReadStorage(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.READ_EXTERNAL_STORAGE"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable contacts
    public void enableContacts(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.WRITE_CONTACTS"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable location
    public void enableLocation(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.ACCESS_FINE_LOCATION"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable location
    public void enableCoarseLocation(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.ACCESS_COARSE_LOCATION"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable phone state
    public void enablePhoneState(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.READ_PHONE_STATE"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable phone calls
    public void enablePhoneCalls(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.PROCESS_OUTGOING_CALLS"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To enable read contacts
    public void enableReadContacts(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + packageName + " android.permission.READ_CONTACTS"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public boolean checkPermission(String permission) {
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
    public void hideDefaultLauncher(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm disable " + packageName});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To UnHide the Default Launcher app
    public void unHideDefaultLauncher(String packageName) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm enable " + packageName});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //To Switch Android debugging
    public void switchDebugging(int set) {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "settings put global adb_enabled " + set});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //Hide navbar
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void hideNav() {
        try {
            show();
            Process root = null;
            Display display =
                    ((WindowManager) Objects.requireNonNull(context.getSystemService(Context.WINDOW_SERVICE)))
                            .getDefaultDisplay();
            int rot = display.getRotation();

            switch (rot) {
                case Surface
                        .ROTATION_0:
                    Log.d("Rot", "0");
                    root = Runtime.getRuntime().exec(new String[]{"su", "-c", "wm overscan 0,0,0,-48"});
                    break;
                case Surface
                        .ROTATION_90:
                    Log.d("Rot", "90");
                    root = Runtime.getRuntime().exec(new String[]{"su", "-c", "wm overscan -48,0,0,0"});
                    break;
                case Surface
                        .ROTATION_180:
                    Log.d("Rot", "180");
                    root = Runtime.getRuntime().exec(new String[]{"su", "-c", "wm overscan 0,-48,0,0"});
                    break;
                case Surface
                        .ROTATION_270:
                    Log.d("Rot", "270");
                    root = Runtime.getRuntime().exec(new String[]{"su", "-c", "wm overscan 0,0,-48,0"});
                    break;
            }

            assert root != null;
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    //Show navbar
    public void show() {
        try {
            Process root = Runtime.getRuntime().exec(new String[]{"su", "-c", "wm overscan 0,0,0,0"});
            root.waitFor();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public void reboot() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot("recovery");
        pm.reboot(null);
        // not working:
        // reboot(null);
    }









}
