package com.tagloy.taglock.realmcontrollers;

import com.tagloy.taglock.realm.RealmController;
import com.tagloy.taglock.realmmodels.ApkManagement;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ApkController {

    public void addApkData(ApkManagement apkManagement){
        Realm realm = RealmController.getInstance().getRealm();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        realm.copyFromRealm(apkManagement);
        realm.commitTransaction();
    }

    public RealmResults<ApkManagement> getApkData() {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<ApkManagement> query = realm.where(ApkManagement.class);
        RealmResults<ApkManagement> apkManagements = query.findAll();
        return apkManagements;
    }

    public boolean isApkAvailable(String id) {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<ApkManagement> query = realm.where(ApkManagement.class);
        query.equalTo("id", id);
        RealmResults<ApkManagement> apkManagements = query.findAll();

        if (apkManagements.size() > 0)
            return true;
        else
            return false;
    }

    public void updateApkData(String id, ApkManagement apkManagement ) {

        Realm realm = RealmController.getInstance().getRealm();
        ApkManagement mApkManagement = new ApkManagement();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        mApkManagement.setApp_name(apkManagement.getApp_name());
        mApkManagement.setApp_package_name(apkManagement.getApp_package_name());
        mApkManagement.setApp_version(apkManagement.getApp_version());
        mApkManagement.setApp_local_storage_path(apkManagement.getApp_local_storage_path());
        mApkManagement.setIs_latest_apk_installed(apkManagement.getIs_latest_apk_installed());
        realm.copyToRealm(mApkManagement);
        realm.commitTransaction();
    }
}
