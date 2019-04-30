package com.tagloy.taglock.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TaglockAdminReceiver extends DeviceAdminReceiver {

    private static final String TAG = "TaglockAdminReceiver";
    @Override
    public void onEnabled(Context context, Intent intent) {
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        CharSequence disableAdmin = "Taglock device admin disable is requested";
        return disableAdmin;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Received: " + intent.getAction());
        super.onReceive(context, intent);
    }
}
