package com.tagloy.taglock.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tagloy.taglock.R;
import com.tagloy.taglock.adapters.WifiListAdapter;
import com.tagloy.taglock.utils.SuperClass;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

import java.util.List;

public class WifiActivity extends AppCompatActivity {

    WifiManager wifiManager;
    WifiInfo wifiInfo;
    ListView wifiListView;
    WifiScanReceiver wifiScanReceiver;
    WifiListAdapter wifiListAdapter;
    String wifi[];
    EditText pass;
    Context mContext;
    SuperClass superClass;
    List<ScanResult> wifiScanResult;
    TaglockDeviceInfo taglockDeviceInfo;

    public ScanResult getItem(int position){
        return wifiScanResult.get(position);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        mContext = this;
        superClass = new SuperClass(this);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        if (Build.VERSION.SDK_INT>=23)
            taglockDeviceInfo.hideStatusBar();
        wifiListView = findViewById(R.id.wifiListView);
        wifiScanReceiver = new WifiScanReceiver();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ssid = getItem(position).SSID;
                String bssid = getItem(position).BSSID;
                if (wifiInfo.getBSSID().equals(bssid)){
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setTitle("Forget Network?");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            superClass.forgetNetwork(wifiInfo.getNetworkId());
                            wifiManager.removeNetwork(wifiInfo.getNetworkId());
                            wifiListAdapter.notifyDataSetChanged();
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                }else{
                    connectToWifi(ssid);
                }
                String frequency = String.valueOf(getItem(position).frequency);
                String level = String.valueOf(getItem(position).level);
                Log.d( "Frequency: ", frequency + " Level: " + level);
            }
        });
        wifiManager.startScan();
        if (!wifiManager.isWifiEnabled()){
            Toast.makeText(this,"WiFi is disabled, Turning it on", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addCategory(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver,intentFilter);
    }

    class WifiScanReceiver extends BroadcastReceiver{
        @SuppressLint("UseValueOf")
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success =  wifiManager.startScan();
            if (success){
                wifiManager.getConnectionInfo().getSupplicantState().describeContents();
                wifiScanResult = wifiManager.getScanResults();
                wifi = new String[wifiScanResult.size()];
                for(int i = 0; i < wifiScanResult.size(); i++){
                    wifi[i] = ((wifiScanResult.get(i)).toString());
                }
                String filtered[] = new String[wifiScanResult.size()];
                int counter = 0;
                for (String wifis: wifi) {
                    String[] temp = wifis.split(",");
                    filtered[counter] = temp[0].substring(5).trim();
                    counter++;
                }
                wifiListAdapter = new WifiListAdapter(context,wifiScanResult);
                wifiListView.setAdapter(wifiListAdapter);
            }else {
                Toast.makeText(mContext,"No wifi results found", Toast.LENGTH_LONG).show();
            }
            String action  = intent.getAction();
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,-1);
                if (error == WifiManager.ERROR_AUTHENTICATING){
                    Toast.makeText(context,"Wrong Password",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        registerReceiver(wifiScanReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(wifiScanReceiver);
        super.onPause();
    }

    private void finallyConnect(String networkPass,final String networkSSID) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

        // remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        if(!wifiManager.reconnect()){
            Toast.makeText(mContext,"Wrong password", Toast.LENGTH_LONG).show();
        }
        wifiListAdapter.notifyDataSetChanged();
    }

    private void connectToWifi(final String wifiSSID) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect);
        dialog.setTitle("Connect to Network");
        TextView textSSID = dialog.findViewById(R.id.textSSID1);

        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final String ssid = (wifiInfo.getSSID()).replace("\"","");
        Button dialogButton = dialog.findViewById(R.id.okButton);
        pass = dialog.findViewById(R.id.textPassword);
        textSSID.setText(wifiSSID);

        // if button is clicked, connect to the network;
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkPassword = pass.getText().toString();
                finallyConnect(checkPassword, wifiSSID);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
