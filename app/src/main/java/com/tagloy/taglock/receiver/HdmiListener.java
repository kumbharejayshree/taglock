package com.tagloy.taglock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.tagloy.taglock.realmcontrollers.DeviceInfoController;
import com.tagloy.taglock.realmmodels.DeviceInformation;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

public class HdmiListener extends BroadcastReceiver {


    TaglockDeviceInfo taglockDeviceInfo;
    boolean state = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        final DeviceInfoController deviceInfoController = new DeviceInfoController();
        final DeviceInformation deviceInformation = new DeviceInformation();
        taglockDeviceInfo = new TaglockDeviceInfo(context);
        String action = intent.getAction();
        String HDMIINTENT = "android.intent.action.HDMI_PLUGGED";
        if (action.equals(HDMIINTENT)){
            state = intent.getBooleanExtra("state",false);
            if (state){
                Log.d("HDMI", "Connected");
                Toast.makeText(context,"HDMI connected",Toast.LENGTH_LONG).show();
            }else {
                Log.d("HDMI", "Disconnected");
                Toast.makeText(context,"HDMI disconnected",Toast.LENGTH_LONG).show();
            }
            deviceInformation.setHdmi_status(state);
            deviceInfoController.updateHDMI(PreferenceHelper.getValueString(context,AppConfig.DEVICE_NAME),state);
            taglockDeviceInfo.updateDevice(deviceInformation);
        }
    }
}
