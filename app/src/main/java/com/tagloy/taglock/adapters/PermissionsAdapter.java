package com.tagloy.taglock.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.tagloy.taglock.R;
import com.tagloy.taglock.activity.AdminActivity;
import com.tagloy.taglock.models.Permissions;
import com.tagloy.taglock.utils.PermissionsClass;
import com.tagloy.taglock.utils.SuperClass;
import com.tagloy.taglock.receiver.TaglockAdminReceiver;
import com.topjohnwu.superuser.Shell;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.REQUEST_INSTALL_PACKAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_SECURE_SETTINGS;

public class PermissionsAdapter extends BaseAdapter implements ActivityCompat.OnRequestPermissionsResultCallback {

    DevicePolicyManager devicePolicyManager;
    ComponentName devicePolicyAdmin;
    private static final int REQUEST_ENABLE = 15;
    private static final int REQUEST_USAGE_ACCESS = 101;
    private static final int REQUEST_APP_NOTIFICATION = 103;
    private static final int REQUEST_SYSTEM_ALERT = 105;




    private static final int REQUEST_WRITE_SETTING = 106;
    private static final int REQUEST_UNKNOWN_SOURCE = 20;
    private static final int REQUEST_PHONE_CODE = 200;
    private static final int REQUEST_LOCATION_CODE = 201;
    private static final int REQUEST_WRITE_CODE = 202;
    //private static final int WRITE_SECURE_SETTINGS = 204;

    private static final int REQUEST_INSTALLED_UNKNOWN_PACKGE = 205;
    private static final int REQUESR_READ_STORAGE = 206;


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
        } else if (position == 1) {
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
        } else if (position == 2) {
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
        } else if (position == 3) {
            if(Shell.rootAccess()){
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            }else {
                if (AdminActivity.b == true) {
                    finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                    finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                    finalMyViewHolder.permissionCheck.setChecked(true);
                    finalMyViewHolder.permissionCheck.setClickable(false);
                } else {
                    finalMyViewHolder.permissionGrant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            permissionsClass.getPermission(context, (Activity) context, REQUEST_INSTALL_PACKAGES, REQUEST_INSTALLED_UNKNOWN_PACKGE);
                        }
                    });
                }
            }

        } else if (position == 4) {
            if(Shell.rootAccess()){
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
            }else {
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
            }



        } else if (position == 5) {
            if(Shell.rootAccess()){
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            }else {
                if (Build.VERSION.SDK_INT >= 23 && Settings.System.canWrite(context)) {
                    finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                    finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                    finalMyViewHolder.permissionCheck.setChecked(true);
                    finalMyViewHolder.permissionCheck.setClickable(false);
                } else {
                    finalMyViewHolder.permissionGrant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            permissionsClass.getPermission(context, (Activity) context, Manifest.permission.WRITE_SECURE_SETTINGS, REQUEST_WRITE_SETTING);
                        }
                    });
                }
            }


        } else if (position == 6) {
            if (Shell.rootAccess()) {
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            } else {
                SuperClass superClass = new SuperClass(context);
                if ((superClass.checkPermission(READ_EXTERNAL_STORAGE))) {
                    finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                    finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                    finalMyViewHolder.permissionCheck.setChecked(true);
                    finalMyViewHolder.permissionCheck.setClickable(false);
                }

            }
        } else if (position == 7) {
            if (Shell.rootAccess()) {
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            } else {
                SuperClass superClass = new SuperClass(context);
                if ((superClass.checkPermission(READ_PHONE_STATE))) {
                    finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                    finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                    finalMyViewHolder.permissionCheck.setChecked(true);
                    finalMyViewHolder.permissionCheck.setClickable(false);
                }

            }

        } else if (position == 8) {
            if (Shell.rootAccess()) {
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            } else {
                SuperClass superClass = new SuperClass(context);
                if ((superClass.checkPermission(WRITE_EXTERNAL_STORAGE))) {
                    finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                    finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                    finalMyViewHolder.permissionCheck.setChecked(true);
                    finalMyViewHolder.permissionCheck.setClickable(false);
                }

            }
        } else if (position == 9) {
            if (Shell.rootAccess()) {
                finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                finalMyViewHolder.permissionCheck.setChecked(true);
                finalMyViewHolder.permissionCheck.setClickable(false);
            } else {
                SuperClass superClass = new SuperClass(context);
                if ((superClass.checkPermission(ACCESS_COARSE_LOCATION))) {
                    finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
                    finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
                    finalMyViewHolder.permissionCheck.setChecked(true);
                    finalMyViewHolder.permissionCheck.setClickable(false);
                }

            }
        } else if (position == 10) {
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        } else if (position == 11) {
            finalMyViewHolder.permissionGrant.setVisibility(View.GONE);
            finalMyViewHolder.permissionCheck.setVisibility(View.VISIBLE);
            finalMyViewHolder.permissionCheck.setChecked(true);
            finalMyViewHolder.permissionCheck.setClickable(false);
        }
        myViewHolder.permissionName.setOnClickListener(v -> {
            Log.d("Pos", String.valueOf(position));
            if (position == 0) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devicePolicyAdmin);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.admin_explanation));
                ((Activity) context).startActivityForResult(intent, REQUEST_ENABLE);
            } else if (position == 1) {
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(context, (Activity) context, Manifest.permission.PACKAGE_USAGE_STATS, REQUEST_USAGE_ACCESS);
                }
            } else if (position == 2) {
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(context, (Activity) context, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, REQUEST_APP_NOTIFICATION);
                }
            } else if (position == 4) {
                if (Build.VERSION.SDK_INT >= 23) {
                    permissionsClass.getPermission(context, (Activity) context, Manifest.permission.SYSTEM_ALERT_WINDOW, REQUEST_SYSTEM_ALERT);
                }
            }
        });
        myViewHolder.permissionGrant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (position == 0) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devicePolicyAdmin);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.admin_explanation));
                    ((Activity) context).startActivityForResult(intent, REQUEST_ENABLE);
                } else if (position == 1) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.PACKAGE_USAGE_STATS, REQUEST_USAGE_ACCESS);
                    }
                } else if (position == 2) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, REQUEST_APP_NOTIFICATION);
                    }
                } else if (position == 3) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, REQUEST_INSTALL_PACKAGES, REQUEST_INSTALLED_UNKNOWN_PACKGE);

                    }
                } else if (position == 4) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.SYSTEM_ALERT_WINDOW, REQUEST_SYSTEM_ALERT);
                    }
                } else if (position == 5) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, Manifest.permission.WRITE_SECURE_SETTINGS, REQUEST_WRITE_SETTING);
                    }
                } else if (position == 6) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, READ_EXTERNAL_STORAGE, REQUESR_READ_STORAGE);
                    }
                }else if (position == 7) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        requestPermission();
                    }
                } else if (position == 8) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_CODE);
                    }
                } else if (position == 9) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsClass.getPermission(context, (Activity) context, ACCESS_COARSE_LOCATION, REQUEST_LOCATION_CODE);

                    }
                }
            }
        });
        return convertView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHONE_CODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted)
                        Toast.makeText(context, "Permission Granted, Now you can access location data and camera.", Toast.LENGTH_SHORT).show();
                    else {

                        Toast.makeText(context, "PPermission Denied, You cannot access location data and camera", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, READ_PHONE_STATE)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    ActivityCompat.requestPermissions((Activity) context, new String[]{READ_PHONE_STATE}, REQUEST_PHONE_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted)
                        Toast.makeText(context, "Permission Granted, Now you can access location data and camera.", Toast.LENGTH_SHORT).show();
                    else {

                        Toast.makeText(context, "PPermission Denied, You cannot access location data and camera", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, ACCESS_COARSE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    ActivityCompat.requestPermissions((Activity) context, new String[]{ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
            case REQUEST_WRITE_CODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted)
                        Toast.makeText(context, "Permission Granted, Now you can access location data and camera.", Toast.LENGTH_SHORT).show();
                    else {

                        Toast.makeText(context, "PPermission Denied, You cannot access location data and camera", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, WRITE_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    ActivityCompat.requestPermissions((Activity) context, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }

            case REQUEST_INSTALLED_UNKNOWN_PACKGE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted)
                        Toast.makeText(context, "Permission Granted, Now you can access location data and camera.", Toast.LENGTH_SHORT).show();
                    else {

                        Toast.makeText(context, "PPermission Denied, You cannot access location data and camera", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, REQUEST_INSTALL_PACKAGES)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    ActivityCompat.requestPermissions((Activity) context, new String[]{REQUEST_INSTALL_PACKAGES}, REQUEST_INSTALLED_UNKNOWN_PACKGE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;

        }

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

    private boolean isNotificationAllowed() {
        ContentResolver contentResolver = context.getContentResolver();
        String getEnabledListener = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();
        Log.e("INSTALL",getEnabledListener);
        if (getEnabledListener == null || !getEnabledListener.contains(packageName)) {
            return false;
        } else {
            return true;
        }
    }
    private boolean isInstall() {
        ContentResolver contentResolver = context.getContentResolver();
        String getEnabledListener = Settings.System.getString(contentResolver, "enabled_install_listeners");
        //Log.e("INSTALL",getEnabledListener);
        String packageName = context.getPackageName();
        if (getEnabledListener == null || !getEnabledListener.contains(packageName)) {
            return false;
        } else {
            return true;
        }
    }


    private void requestPermission() {

        ActivityCompat.requestPermissions((Activity) context, new String[]{READ_PHONE_STATE}, REQUEST_PHONE_CODE);

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean checkPermission() {
        int result = ActivityCompat.checkSelfPermission(context, WRITE_SECURE_SETTINGS);
        return result == PackageManager.PERMISSION_GRANTED;
    }


}
