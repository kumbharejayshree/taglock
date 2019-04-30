package com.tagloy.taglock.realmcontrollers;

import com.tagloy.taglock.realm.RealmController;
import com.tagloy.taglock.realmmodels.DeviceInformation;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class DeviceInfoController {

    public void addDeviceData(DeviceInformation deviceInformation){
        Realm realm = RealmController.getInstance().getRealm();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        realm.copyToRealm(deviceInformation);
        realm.commitTransaction();
    }

    public DeviceInformation getDeviceData(String device_name) {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<DeviceInformation> query = realm.where(DeviceInformation.class).equalTo("device_name",device_name);
        DeviceInformation deviceInformations = query.findFirst();
        return deviceInformations;
    }

    public boolean isDeviceAvailable(String device_name) {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<DeviceInformation> query = realm.where(DeviceInformation.class);
        query.equalTo("device_name", device_name);
        RealmResults<DeviceInformation> deviceInformations = query.findAll();

        if (deviceInformations.size() > 0)
            return true;
        else
            return false;
    }

    public void updateHDMI(String device_name,boolean HDMIflag){
        Realm realm = RealmController.getInstance().getRealm();
        if (!realm.isInTransaction())
            realm.beginTransaction();
        DeviceInformation deviceInformation = getDeviceData(device_name);
        deviceInformation.setHdmi_status(HDMIflag);
        realm.copyToRealm(deviceInformation);
        realm.commitTransaction();
    }

    public void updateTaglockStatus(String device_name,boolean taglock_status){
        Realm realm = RealmController.getInstance().getRealm();
        if (!realm.isInTransaction())
            realm.beginTransaction();
        DeviceInformation deviceInformation = getDeviceData(device_name);
        deviceInformation.setDevice_locked_status(taglock_status);
        realm.copyToRealm(deviceInformation);
        realm.commitTransaction();
    }

    public void updateDeviceData(String device_name,DeviceInformation deviceInformation ) {

        Realm realm = RealmController.getInstance().getRealm();
        if (!realm.isInTransaction())
            realm.beginTransaction();

        DeviceInformation mDeviceInformation = getDeviceData(device_name);
        //realm.where(DeviceInformation.class).equalTo("device_name",device_name).findFirst();
        mDeviceInformation.setDevice_name(deviceInformation.getDevice_name());
        mDeviceInformation.setLatitudes(deviceInformation.getLatitudes());
        mDeviceInformation.setLongitudes(deviceInformation.getLongitudes());
        mDeviceInformation.setDevice_group(deviceInformation.getDevice_group());
        mDeviceInformation.setAndroid_version(deviceInformation.getAndroid_version());
        mDeviceInformation.setDevice_Api_version(deviceInformation.getDevice_Api_version());
        mDeviceInformation.setDevice_locked_status(deviceInformation.getDevice_locked_status());
        mDeviceInformation.setHdmi_status(deviceInformation.getHdmi_status());
        mDeviceInformation.setDefault_apk_version(deviceInformation.getDefault_apk_version());
        mDeviceInformation.setTaglock_version(deviceInformation.getTaglock_version());
        mDeviceInformation.setApp_download_status(deviceInformation.getApp_download_status());
        mDeviceInformation.setTaglock_download_status(deviceInformation.getTaglock_download_status());
        mDeviceInformation.setIp_Address(deviceInformation.getIp_Address());
        mDeviceInformation.setMac_Address(deviceInformation.getMac_Address());
        mDeviceInformation.setDevice_Token(deviceInformation.getDevice_Token());
        mDeviceInformation.setStorage_memory(deviceInformation.getStorage_memory());
        mDeviceInformation.setRam(deviceInformation.getRam());
        mDeviceInformation.setDevice_expiry_date(deviceInformation.getDevice_expiry_date());
        mDeviceInformation.setBox_Name(deviceInformation.getBox_Name());
        mDeviceInformation.setWifi_status(deviceInformation.getWifi_status());
        mDeviceInformation.setUpdated_at(deviceInformation.getUpdated_at());
        realm.copyToRealm(mDeviceInformation);
        realm.commitTransaction();
    }
}
