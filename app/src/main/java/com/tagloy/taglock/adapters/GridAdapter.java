package com.tagloy.taglock.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tagloy.taglock.models.Item;
import com.tagloy.taglock.R;

import java.util.List;

public class GridAdapter extends BaseAdapter {
    Context context;
    List<Item> itemList;
    public GridAdapter(Context context,List<Item> itemList){
        this.context = context;
        this.itemList = itemList;
    }
    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PackageManager manager = context.getPackageManager();
        ViewHolder viewHolder = null;
        if (itemList!=null){
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (layoutInflater != null) {
                convertView = layoutInflater.inflate(R.layout.item,null);
            }
            viewHolder.appImage = convertView.findViewById(R.id.icon_view);
            viewHolder.appName = convertView.findViewById(R.id.name_view);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.appImage.setImageDrawable(itemList.get(position).icon);
        viewHolder.appName.setText(itemList.get(position).name);

        viewHolder.appImage.setOnClickListener(v -> {
            Intent intent = manager.getLaunchIntentForPackage(itemList.get(position).label.toString());
            context.startActivity(intent);
        });
        return convertView;
    }
    static class ViewHolder{
        ImageView appImage;
        TextView appName;
    }
}
