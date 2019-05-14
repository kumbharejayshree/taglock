package com.tagloy.taglock.services;

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

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";
    SuperClass superClass = new SuperClass(this);
    ApkManagement apkManagement = new ApkManagement(this);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        final RemoteMessage message = remoteMessage;
        final String apk_name = "apkmanagement/" + PreferenceHelper.getValueString(this,AppConfig.APK_NAME);
        final String taglock_apk = "taglockmanagement/" + PreferenceHelper.getValueString(this,AppConfig.TAGLOCK_APK);
        if (remoteMessage.getData().size() > 0) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Message payload: " + message.getData());
                    //Toast.makeText(getApplicationContext(), "Data: " + message.getData().get("command"), Toast.LENGTH_LONG).show();
                    String command = message.getData().get("command");
                    switch (command) {
                        case "restart":
                            superClass.restartDevice();
                            break;
                        case "shutdown":
                            superClass.shutdownDevice();
                            break;
                        case "download":
                            apkManagement.getApk();
                            break;
                        case "downloadtag":
                            apkManagement.getTaglock();
                            break;
                        case "install":
                            new MainActivity.InstallApp(getApplicationContext(),apk_name).execute();
                            break;
                        case "update":
                            new MainActivity.UpdateApp(getApplicationContext(),apk_name).execute();
                            break;
                        case "clear":
                            SuperClass.clearData();
                            break;
                        case "refresh":
                            superClass.hideNavToggle();
                            break;
                        case "show":
                            superClass.showNavToggle();
                            break;
                        case "installtag":
                            new MainActivity.InstallApp(getApplicationContext(),taglock_apk).execute();
                            break;
                        case "updatetag":
                            new MainActivity.UpdateApp(getApplicationContext(),taglock_apk).execute();
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "New token: " + token);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceToken = instanceIdResult.getToken();
                PreferenceHelper.setValueString(getApplicationContext(),AppConfig.FCM_TOKEN,deviceToken);
            }
        });
    }
}
