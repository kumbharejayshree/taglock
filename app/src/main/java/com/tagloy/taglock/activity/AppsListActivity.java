package com.tagloy.taglock.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tagloy.taglock.models.Item;
import com.tagloy.taglock.R;

import java.util.ArrayList;
import java.util.List;

public class AppsListActivity extends AppCompatActivity {

    PackageManager manager;
    List<Item> apps;
    ListView apps_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list);
        manager = getPackageManager();
        apps = new ArrayList<>();
        loadApps();
        loadList();
        addClickListener();
    }

    public void loadApps(){
        manager = getPackageManager();
        apps = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableApps = manager.queryIntentActivities(intent,0);
        for (ResolveInfo ri : availableApps){
            Item app = new Item();
            app.label = ri.activityInfo.packageName;
            app.name = ri.loadLabel(manager);
            apps.add(app);
        }
    }
    private void loadList(){
        apps_list = findViewById(R.id.apps_list);
        ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(this,R.layout.item,apps){
            @NonNull
            @Override
            public View getView(int position,@NonNull View convertView,@NonNull ViewGroup parent) {
                if (convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.item,null);
                }
                ImageView appIcon = convertView.findViewById(R.id.icon_view);
                appIcon.setImageDrawable(apps.get(position).icon);
                TextView appName = convertView.findViewById(R.id.name_view);
                appName.setText(apps.get(position).name);
                return convertView;
            }
        };
        apps_list.setAdapter(adapter);
    }
    private void addClickListener(){
        apps_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = manager.getLaunchIntentForPackage(apps.get(position).label.toString());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try{
            if (!hasFocus){
                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(closeDialog);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent intent = new Intent(AppsListActivity.this,MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        finish();
//    }
}
