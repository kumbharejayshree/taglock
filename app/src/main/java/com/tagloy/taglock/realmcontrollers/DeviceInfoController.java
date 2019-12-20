package com.tagloy.taglock.realmcontrollers;

import com.tagloy.taglock.models.DeviceInfo;
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

    public RealmResults<DeviceInformation> getDeviceData() {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<DeviceInformation> query = realm.where(DeviceInformation.class);
        return query.findAll();
    }

    public boolean isDeviceAvailable() {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<DeviceInformation> query = realm.where(DeviceInformation.class);
        RealmResults<DeviceInformation> deviceInformations = query.findAll();
        if(deviceInformations.size() > 0) {
            return true;
        }else{
            return false;
        }
    }

    public void updateHDMI(boolean HDMIflag){
        Realm realm = RealmController.getInstance().getRealm();
        if (!realm.isInTransaction())
            realm.beginTransaction();
        DeviceInformation deviceInformation = realm.where(DeviceInformation.class).findFirst();
        deviceInformation.setHdmi_status(HDMIflag);
        realm.copyToRealm(deviceInformation);
        realm.commitTransaction();
    }

    public void updateTaglockStatus(DeviceInformation deviceInformation){
        Realm realm = RealmController.getInstance().getRealm();
        if (!realm.isInTransaction())
            realm.beginTransaction();
        DeviceInformation mDeviceInformation = realm.where(DeviceInformation.class).findFirst();
        if (mDeviceInformation != null) {
            mDeviceInformation.setDevice_locked_status(deviceInformation.getDevice_locked_status());
        }
        realm.copyToRealm(deviceInformation);
        realm.commitTransaction();
    }

    public void updateDevice(DeviceInformation deviceInformation ) {

        Realm realm = RealmController.getInstance().getRealm();
        if (!realm.isInTransaction())
            realm.beginTransaction();

        DeviceInformation mDeviceInformation = realm.where(DeviceInformation.class).findFirst();
        //realm.where(DeviceInformation.class).equalTo("device_name",device_name).findFirst();
        if (mDeviceInformation != null) {
            mDeviceInformation.setDevice_name(deviceInformation.getDevice_name());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setDevice_group(deviceInformation.getDevice_group());
        }
        if (mDeviceInformation != null) {
            realm.copyToRealm(mDeviceInformation);
        }
        realm.commitTransaction();
    }

    public void updateApkDetails(DeviceInformation deviceInformation ) {

        Realm realm = RealmController.getInstance().getRealm();
        if (!realm.isInTransaction())
            realm.beginTransaction();

        DeviceInformation mDeviceInformation = realm.where(DeviceInformation.class).findFirst();
        //realm.where(DeviceInformation.class).equalTo("device_name",device_name).findFirst();
        if (mDeviceInformation != null) {
            mDeviceInformation.setApp_download_status(deviceInformation.getApp_download_status());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setDefault_apk_version(deviceInformation.getDefault_apk_version());
        }
        if (mDeviceInformation != null) {
            realm.copyToRealm(mDeviceInformation);
        }
        realm.commitTransaction();
    }

    public void updateDeviceData(DeviceInformation deviceInformation ) {

        Realm realm = RealmController.getInstance().getRealm();
        if (!realm.isInTransaction())
            realm.beginTransaction();

        DeviceInformation mDeviceInformation = realm.where(DeviceInformation.class).findFirst();
        //realm.where(DeviceInformation.class).equalTo("device_name",device_name).findFirst();
        if (mDeviceInformation != null) {
            mDeviceInformation.setLatitudes(deviceInformation.getLatitudes());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setLongitudes(deviceInformation.getLongitudes());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setAndroid_version(deviceInformation.getAndroid_version());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setDevice_Api_version(deviceInformation.getDevice_Api_version());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setDevice_locked_status(deviceInformation.getDevice_locked_status());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setHdmi_status(deviceInformation.getHdmi_status());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setDefault_apk_version(deviceInformation.getDefault_apk_version());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setTaglock_version(deviceInformation.getTaglock_version());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setApp_download_status(deviceInformation.getApp_download_status());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setTaglock_download_status(deviceInformation.getTaglock_download_status());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setIp_Address(deviceInformation.getIp_Address());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setWifimac_Address(deviceInformation.getWifimac_Address());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setLanimac_Address(deviceInformation.getLanimac_Address());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setDevice_Token(deviceInformation.getDevice_Token());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setStorage_memory(deviceInformation.getStorage_memory());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setRam(deviceInformation.getRam());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setDevice_expiry_date(deviceInformation.getDevice_expiry_date());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setBox_Name(deviceInformation.getBox_Name());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setWifi_status(deviceInformation.getWifi_status());
        }
        if (mDeviceInformation != null) {
            mDeviceInformation.setUpdated_at(deviceInformation.getUpdated_at());
        }
        if (mDeviceInformation != null) {
            realm.copyToRealm(mDeviceInformation);
        }
        realm.commitTransaction();
    }
}
