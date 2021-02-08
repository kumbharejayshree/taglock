package com.tagloy.taglock.services;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tagloy.taglock.activity.MainActivity;
import com.tagloy.taglock.utils.ApkManagement;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    Context mContext;
    private static final String TAG = "MyFirebaseMessagingServ";
    SuperClass superClass = new SuperClass(this);
    TaglockDeviceInfo taglockDeviceInfo = new TaglockDeviceInfo(this);
    ApkManagement apkManagement = new ApkManagement(this);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mContext = this;
        final RemoteMessage message = remoteMessage;
        final String apk_name = "apkmanagement/" + PreferenceHelper.getValueString(this, AppConfig.APK_NAME);
        final String taglock_apk = "taglockmanagement/" + PreferenceHelper.getValueString(this, AppConfig.TAGLOCK_APK);
        final boolean apkDown = PreferenceHelper.getValueBoolean(this, AppConfig.APK_DOWN_STATUS);
        final boolean tagDown = PreferenceHelper.getValueBoolean(this, AppConfig.TAGLOCK_DOWN_STATUS);
        if (remoteMessage.getData().size() > 0) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                //Toast.makeText(getApplicationContext(), "Data: " + message.getData().get("command"), Toast.LENGTH_LONG).show();
                String command = message.getData().get("command");
                Log.d("FCMcommand", command);
                switch (command) {
                    case "restart":
                        superClass.restartDevice();
                        break;
                    case "shutdown":
                        superClass.shutdownDevice();
                        break;
                    case "uninstall":
                        SuperClass.uninstallApp();
                        break;
                    case "update":
                        if (apkDown) {
                            new MainActivity.UpdateApp(getApplicationContext(), apk_name).execute();
                        } else {
                            apkManagement.getApk();
                        }
                        break;
                    case "clear":
                        SuperClass.clearData();
                        break;
                    case "cleardownloadmanager":
                        SuperClass.clearDownloadManager();
                        break;
                    case "refresh":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            superClass.hideNavToggle();
                        }
                        break;
                    case "unlock":
                        PreferenceHelper.setValueBoolean(mContext, AppConfig.IS_LOCKED, false);
                        break;
                    case "installtag":
                        new MainActivity.InstallApp(getApplicationContext(), taglock_apk).execute();
                        break;
                    case "updatetag":
                        if (tagDown) {
                            new MainActivity.UpdateTaglock(getApplicationContext(), taglock_apk).execute();
                        } else {
                            apkManagement.getTaglock();
                        }
                        break;
                }
            });
        }
    }

    @Override
    public void onNewToken(String token) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            String deviceToken = instanceIdResult.getToken();
            PreferenceHelper.setValueString(getApplicationContext(), AppConfig.FCM_TOKEN, deviceToken);
        });
    }
}
