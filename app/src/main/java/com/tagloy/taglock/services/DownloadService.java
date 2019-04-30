package com.tagloy.taglock.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DownloadService extends IntentService {

    public static final String DOWNLOAD_PATH = "Download_Path";
    public static final String DESTINATION_PATH = "Destination_Path";

    public DownloadService(){
        super("DownloadService");
    }

    public static Intent getDownloadService(Context context,final String downloadPath, final String destinationPath){
        return new  Intent(context,DownloadService.class)
                .putExtra(DOWNLOAD_PATH,downloadPath)
                .putExtra(DESTINATION_PATH,destinationPath);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String downloadPath = intent.getStringExtra(DOWNLOAD_PATH);
        String destinationPath = intent.getStringExtra(DESTINATION_PATH);
        startDownload(downloadPath,destinationPath);
    }

    private void startDownload(String downloadPath,String destinationPath){
        Uri uri = Uri.parse(downloadPath);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setTitle("Downloading APK");
        request.setVisibleInDownloadsUi(false);
        request.setDestinationInExternalPublicDir(destinationPath,uri.getLastPathSegment());
        ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
    }
}
