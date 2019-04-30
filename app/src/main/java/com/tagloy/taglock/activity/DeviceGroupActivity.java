package com.tagloy.taglock.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
import com.tagloy.taglock.utils.SuperClass;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceGroupActivity extends AppCompatActivity implements View.OnClickListener {

    String groupJson;
    Spinner groupSpinner;
    Button submitGroupBtn;
    List<Groups> groupsList = new ArrayList<>();
    List<String> list = new ArrayList<>();
    SuperClass superClass;
    EditText groupKeyEdit;
    ArrayAdapter<String> arrayAdapter;
    TaglockDeviceInfo taglockDeviceInfo;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_group);
        groupSpinner = findViewById(R.id.groupNameSpinner);
        submitGroupBtn = findViewById(R.id.submitGroupBtn);
        groupKeyEdit = findViewById(R.id.groupKeyEdit);
        sharedPreferences = getSharedPreferences(AppConfig.TAGLOCK_PREF,Context.MODE_PRIVATE);
        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,list);
        submitGroupBtn.setOnClickListener(this);
        superClass = new SuperClass(this);
        taglockDeviceInfo = new TaglockDeviceInfo(this);
        taglockDeviceInfo.hideStatusBar();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GROUP_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                groupJson = response;
                new ParseGroups(DeviceGroupActivity.this).execute();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/json");
                return params;
            }
        };
        queue.add(stringRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submitGroupBtn:
                String groupName = groupSpinner.getSelectedItem().toString();
                if (TextUtils.isEmpty(groupKeyEdit.getText())){
                    groupKeyEdit.setError("Please enter group key");
                }else {
                    String groupKey = groupKeyEdit.getText().toString();
                    taglockDeviceInfo.checkGroupKey(groupName,groupKey);
                }
        }
    }

    public class ParseGroups extends AsyncTask<Void,Void,Void>{
        Context context;
        public ParseGroups(Context context){
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (groupsList!=null){
                Groups groups;
                try{
                    JSONArray jsonArray = new JSONArray(groupJson);
                    for(int j=0;j<jsonArray.length();j++){
                        groups = new Groups();
                        JSONObject tagGroup = jsonArray.getJSONObject(j);
                        list.add(tagGroup.getString("group_name"));
                        groups.group_name = tagGroup.getString("group_name");
                        groups.group_id = tagGroup.getString("group_id");
                        groupsList.add(groups);
                    }
                }catch (JSONException je){
                    je.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            groupSpinner.setAdapter(arrayAdapter);
        }
    }
}
