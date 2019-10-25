package com.tagloy.taglock.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

import java.util.List;

public class NetworkActivity extends AppCompatActivity {

    WifiManager wifiManager;
    ListView wifiListView;
    WifiScanReceiver wifiScanReceiver;
    WifiListAdapter wifiListAdapter;
    String wifi[];
    EditText pass;
    List<ScanResult> wifiScanResult;
    TaglockDeviceInfo taglockDeviceInfo;
    Context mContext;
    TextView submitNetwork;

    public ScanResult getItem(int position){
        return wifiScanResult.get(position);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        mContext = this;
        submitNetwork = findViewById(R.id.submitNetwork);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        if (Build.VERSION.SDK_INT>=23)
            taglockDeviceInfo.hideStatusBar();
        wifiListView = findViewById(R.id.wifiListView);
        wifiScanReceiver = new WifiScanReceiver();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ssid = getItem(position).SSID;
                String frequency = String.valueOf(getItem(position).frequency);
                String level = String.valueOf(getItem(position).level);
                Log.d( "Frequency: ", frequency + " Level: " + level);
                connectToWifi(ssid);
            }
        });
        String device_name = PreferenceHelper.getValueString(this,AppConfig.DEVICE_NAME);
        if (device_name != null){
            Intent intent = new Intent(NetworkActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        if (taglockDeviceInfo.isNetworkConnected()){
            submitNetwork.setTextColor(getResources().getColor(R.color.tagColor));
        }
        wifiManager.startScan();
        if (!taglockDeviceInfo.isEthernetConnected()){
            if (!wifiManager.isWifiEnabled()){
                Toast.makeText(this,"WiFi is disabled, Turning it on", Toast.LENGTH_LONG).show();
                wifiManager.setWifiEnabled(true);
                wifiManager.startScan();
            }
        }
        submitNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taglockDeviceInfo.isNetworkConnected()){
                    Intent intent = new Intent(NetworkActivity.this,DeviceDetailActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(mContext,"Please connect to the network", Toast.LENGTH_LONG).show();
                }
            }
        });
        registerReceiver(wifiScanReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    class WifiScanReceiver extends BroadcastReceiver {
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

    private void finallyConnect(String networkPass, String networkSSID) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

        // remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"\"" + networkSSID + "\"\"";
        conf.preSharedKey = "\"" + networkPass + "\"";
        wifiManager.addNetwork(conf);
    }

    private void connectToWifi(final String wifiSSID) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect);
        dialog.setTitle("Connect to Network");
        submitNetwork.setTextColor(getResources().getColor(R.color.tagColor));
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
                Toast.makeText(NetworkActivity.this,"Connected to Network: " + wifiSSID, Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }
}
