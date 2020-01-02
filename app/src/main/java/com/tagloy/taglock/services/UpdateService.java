package com.tagloy.taglock.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.tagloy.taglock.realmmodels.DeviceInformation;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateService extends Service {

    private final IBinder mBinder = new MyBinder();
    TaglockDeviceInfo taglockDeviceInfo = new TaglockDeviceInfo(this);
    DeviceInformation deviceInformation = taglockDeviceInfo.updateDetails();
    Timer updateTimer;
    TimerTask updateTimerTask;
    @Override
    public IBinder onBind(Intent intent) {
        startUpdateTimer();
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startUpdateTimer();
        return Service.START_NOT_STICKY;
    }

    public class MyBinder extends Binder {
        public UpdateService getService() {
            return UpdateService.this;
        }
    }

    //Start timer to update device data
    public void startUpdateTimer(){
        updateTimer = new Timer();
        initializeUpdateTask();
        updateTimer.schedule(updateTimerTask,5*60*1000, 5*60*1000);
    }

    //Initialize update data task
    public void initializeUpdateTask(){
        updateTimerTask = new TimerTask() {
            @Override
            public void run() {
                taglockDeviceInfo.updateDevice(deviceInformation);
            }
        };
    }

}
