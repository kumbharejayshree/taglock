package com.tagloy.taglock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.tagloy.taglock.realmmodels.DeviceInformation;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.TaglockDeviceInfo;


public class HdmiListener extends BroadcastReceiver {
    TaglockDeviceInfo taglockDeviceInfo;
    public static boolean state = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        final DeviceInformation deviceInformation = new DeviceInformation();
        taglockDeviceInfo = new TaglockDeviceInfo(context);
        String deviceName = PreferenceHelper.getValueString(context,AppConfig.DEVICE_NAME);
        String action = intent.getAction();
        String HDMIINTENT = "android.intent.action.HDMI_PLUGGED";
        if (action.equals(HDMIINTENT)){
            state = intent.getBooleanExtra("state",false);
            if (state){
                Log.d("HDMI", "Connected");
                PreferenceHelper.setValueBoolean(context,AppConfig.HDMI_STATUS, true);
                Toast.makeText(context,"HDMI connected",Toast.LENGTH_LONG).show();
            }else {
                Log.d("HDMI", "Disconnected");
                PreferenceHelper.setValueBoolean(context,AppConfig.HDMI_STATUS, false);
                Toast.makeText(context,"HDMI disconnected",Toast.LENGTH_LONG).show();
            }
            deviceInformation.setHdmi_status(state);
            deviceInformation.setDevice_name(deviceName);
            taglockDeviceInfo.updateDevice(deviceInformation);
        }
    }
}
