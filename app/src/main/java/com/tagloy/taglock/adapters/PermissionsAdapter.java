package com.tagloy.taglock.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.tagloy.taglock.R;
import com.tagloy.taglock.models.Permissions;
import com.tagloy.taglock.utils.PermissionsClass;
import com.tagloy.taglock.utils.SuperClass;
import com.tagloy.taglock.receiver.TaglockAdminReceiver;

import java.util.List;

public class PermissionsAdapter extends BaseAdapter {

    DevicePolicyManager devicePolicyManager;
    ComponentName devicePolicyAdmin;
    private static final int REQUEST_ENABLE = 15;
    private static final int REQUEST_USAGE_ACCESS = 101;
    private static final int REQUEST_APP_NOTIFICATION = 103;
    private static final int REQUEST_SYSTEM_ALERT = 105;
    private static final int REQUEST_WRITE_SETTING = 106;

    public Context context;
    private List<Permissions> permissionsList;
    private SuperClass superClass;
    private PermissionsClass permissionsClass;

    public PermissionsAdapter(Context context, List<Permissions> permissionsList) {
        this.context = context;
        this.permissionsList = permissionsList;
    }

    @Override
    public int getCount() {
        return permissionsList.size();
    }

    @Override
    public Object getItem(int position) {
        return permissionsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        superClass = new SuperClass(context);
        permissionsClass = new PermissionsClass(context);
        MyViewHolder myViewHolder = null;
        if (permissionsList != null) {
            myViewHolder = new MyViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.permission_list_row, null);
            myViewHolder.permissionName = convertView.findViewById(R.id.permissionName);
            myViewHolder.permissionDesc = convertView.findViewById(R.id.permissionDesc);
            myViewHolder.permissionIcon = convertView.findViewById(R.id.permissionIcon);
            myViewHolder.permissionGrant = convertView.findViewById(R.id.permissionGrant);
            myViewHolder.permissionCheck = convertView.findViewById(R.id.permissionCheck);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder) convertView.getTag();
        }
        myViewHolder.permissionName.setText(permissionsList.get(position).name);
        myViewHolder.permissionDesc.setText(permissionsList.get(position).description);
        myViewHolder.permissionIcon.setImageDrawable(permissionsList.get(position).icon);
        final MyViewHolder finalMyViewHolder = myViewHolder;
        if (position == 0) {
            if (isMyPolicyActive()) {
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            } else {
                finalMyViewHolder.permissionGrant.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devicePolicyAdmin);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.admin_explanation));
                    ((Activity) context).startActivityForResult(intent, REQUEST_ENABLE);
                });
            }
        }else if (position == 1) {
            if (isAccessGranted()) {
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            } else {
                finalMyViewHolder.permissionGrant.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (Build.VERSION.SDK_INT >= 23)
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.PACKAGE_USAGE_STATS, REQUEST_USAGE_ACCESS);
                });
            }
        }else if (position == 2) {
            if (isNotificationAllowed()) {
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            } else {
                finalMyViewHolder.permissionGrant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, REQUEST_APP_NOTIFICATION);
                    }
                });
            }
        }else if (position == 3){
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        }else if (position == 4) {
            if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(context)) {
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            } else {
                finalMyViewHolder.permissionGrant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.SYSTEM_ALERT_WINDOW, REQUEST_SYSTEM_ALERT);
                    }
                });
            }
        }else if (position == 5){
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        }else if (position==6){
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        }else if (position==7){
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        }else if (position==8){
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        }else if (position==9){
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        }else if (position==10){
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        }else if (position==11){
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        }
        myViewHolder.permissionName.setOnClickListener(v-> {
            Log.d("Pos", String.valueOf(position));
            if (position == 0){
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devicePolicyAdmin);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.admin_explanation));
                ((Activity) context).startActivityForResult(intent, REQUEST_ENABLE);
            }else if (position == 1){
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(context, (Activity) context, Manifest.permission.PACKAGE_USAGE_STATS, REQUEST_USAGE_ACCESS);
                }
            }else if (position == 2){
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(context, (Activity) context, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, REQUEST_APP_NOTIFICATION);
                }
            }else if (position == 4){
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(context, (Activity) context, Manifest.permission.SYSTEM_ALERT_WINDOW, REQUEST_SYSTEM_ALERT);
                }
            }
        });
        myViewHolder.permissionGrant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (position == 0){
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devicePolicyAdmin);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.admin_explanation));
                    ((Activity) context).startActivityForResult(intent, REQUEST_ENABLE);
                }else if (position == 1){
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.PACKAGE_USAGE_STATS, REQUEST_USAGE_ACCESS);
                    }
                }else if (position == 2){
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, REQUEST_APP_NOTIFICATION);
                    }
                }else if (position == 4){
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.SYSTEM_ALERT_WINDOW, REQUEST_SYSTEM_ALERT);
                    }
                }
            }
        });
        return convertView;
    }

    public class MyViewHolder {
        public TextView permissionName, permissionDesc;
        public ImageView permissionIcon;
        public Switch permissionGrant;
        public CheckBox permissionCheck;
    }

    public boolean isMyPolicyActive() {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyAdmin = new ComponentName(context, TaglockAdminReceiver.class);
        return devicePolicyManager.isAdminActive(devicePolicyAdmin);
    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isNotificationAllowed(){
        ContentResolver contentResolver = context.getContentResolver();
        String getEnabledListener = Settings.Secure.getString(contentResolver,"enabled_notification_listeners");
        String packageName = context.getPackageName();
        if (getEnabledListener == null || !getEnabledListener.contains(packageName)){
            return false;
        }else {
            return true;
        }
    }

}
