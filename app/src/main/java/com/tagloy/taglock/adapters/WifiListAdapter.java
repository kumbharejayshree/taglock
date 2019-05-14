package com.tagloy.taglock.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tagloy.taglock.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class WifiListAdapter extends BaseAdapter {
    List<ScanResult> list;
    Context context;

    public WifiListAdapter(Context context, List<ScanResult> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (list!=null){
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item,null);
            viewHolder.strengthImage = convertView.findViewById(R.id.strengthImage);
            viewHolder.wifiNameText = convertView.findViewById(R.id.wifiNameText);
            viewHolder.statusText = convertView.findViewById(R.id.wifiStatus);
            viewHolder.forgetButton = convertView.findViewById(R.id.forgetButton);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (list.get(position).level >= -50){
            Picasso.get().load(R.drawable.wifi1).into(viewHolder.strengthImage);
        }else if (list.get(position).level < -50 && list.get(position).level >= -70){
            Picasso.get().load(R.drawable.wifi2).into(viewHolder.strengthImage);
        }else if (list.get(position).level < -70 && list.get(position).level >= -90){
            Picasso.get().load(R.drawable.wifi3).into(viewHolder.strengthImage);
        }else if (list.get(position).level < -90){
            Picasso.get().load(R.drawable.wifi4).into(viewHolder.strengthImage);
        }
        viewHolder.wifiNameText.setText(list.get(position).SSID);
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = (wifiInfo.getSSID()).replace("\"","");
        if (list.get(position).SSID.equals(ssid)){
            viewHolder.statusText.setText("Connected");
            viewHolder.forgetButton.setVisibility(View.VISIBLE);
            viewHolder.forgetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Forget " + list.get(position).SSID + "!");
                    alert.setMessage("Are you sure?");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            wifiManager.disableNetwork(wifiInfo.getNetworkId());
                            if (Build.VERSION.SDK_INT < 26)
                                wifiManager.saveConfiguration();
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alert.create();
                    alert.show();
                }
            });
        }else {
            viewHolder.statusText.setText("");
        }
        return convertView;
    }
     class ViewHolder{
        ImageView strengthImage;
        TextView statusText, wifiNameText,forgetButton;
     }
}
