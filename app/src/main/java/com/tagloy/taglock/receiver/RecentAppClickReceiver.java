package com.tagloy.taglock.receiver;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RecentAppClickReceiver extends BroadcastReceiver {
    final String SYSTEM_DIALOG_REASON_KEY = "reason";
    final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (reason != null) {
                //Log.e(TAG, "action:" + action + ",reason:" + reason);
                if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                    ActivityManager activityManager = (ActivityManager) context.getApplicationContext()
                            .getSystemService(Context.ACTIVITY_SERVICE);
                    activityManager.moveTaskToFront(((Activity)context).getTaskId(), 0);
                }
            }
        }
    }
}
