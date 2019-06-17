package com.tagloy.taglock.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;

public class UpdateService extends Service {

    CountDownTimer updateCountDownTimer;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public class updateData extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}
