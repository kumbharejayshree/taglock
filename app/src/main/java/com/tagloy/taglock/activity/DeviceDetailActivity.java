package com.tagloy.taglock.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tagloy.taglock.R;
import com.tagloy.taglock.models.Groups;
import com.tagloy.taglock.utils.AppConfig;
import com.tagloy.taglock.utils.PreferenceHelper;
import com.tagloy.taglock.utils.SuperClass;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceDetailActivity extends AppCompatActivity implements View.OnClickListener {

    Button submitGroupBtn;
    List<String> list = new ArrayList<>();
    SuperClass superClass;
    EditText deviceNameEdit, groupIdEdit, groupKeyEdit;
    ArrayAdapter<String> arrayAdapter;
    TaglockDeviceInfo taglockDeviceInfo;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        submitGroupBtn = findViewById(R.id.submitGroupBtn);
        deviceNameEdit = findViewById(R.id.deviceNameEdit);
        groupIdEdit = findViewById(R.id.groupIdEdit);
        groupKeyEdit = findViewById(R.id.groupKeyEdit);
        sharedPreferences = getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        submitGroupBtn.setOnClickListener(this);
        superClass = new SuperClass(this);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        PreferenceHelper.setValueBoolean(this, AppConfig.APK_DOWN_STATUS,false);
        PreferenceHelper.setValueBoolean(this,AppConfig.TAGLOCK_DOWN_STATUS,true);
        if (Build.VERSION.SDK_INT>=23)
            taglockDeviceInfo.hideStatusBar();
        taglockDeviceInfo.getLauncher();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submitGroupBtn:
                if (TextUtils.isEmpty(groupKeyEdit.getText())) {
                    groupKeyEdit.setError("Please enter group key");
                }else if (TextUtils.isEmpty(deviceNameEdit.getText())){
                    deviceNameEdit.setError("Please enter device name");
                }else if (TextUtils.isEmpty(groupIdEdit.getText())){
                    groupIdEdit.setError("Please enter group id");
                }else {
                    PreferenceHelper.setValueBoolean(this, AppConfig.IS_ACTIVE, true);
                    String groupKey = groupKeyEdit.getText().toString();
                    String deviceName = deviceNameEdit.getText().toString();
                    String groupId = groupIdEdit.getText().toString();
                    taglockDeviceInfo.getGroup(groupId);
                    taglockDeviceInfo.getCreds();
                    taglockDeviceInfo.checkGroupKey(deviceName,groupId, groupKey);
                }
        }
    }
}
