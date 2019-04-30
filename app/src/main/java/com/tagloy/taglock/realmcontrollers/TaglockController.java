package com.tagloy.taglock.realmcontrollers;

import com.tagloy.taglock.realm.RealmController;
import com.tagloy.taglock.realmmodels.TaglockApkManagement;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class TaglockController {
    public void addTaglockData(TaglockApkManagement taglockApkManagement){
        Realm realm = RealmController.getInstance().getRealm();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        realm.copyFromRealm(taglockApkManagement);
        realm.commitTransaction();
    }

    public RealmResults<TaglockApkManagement> getApkData() {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<TaglockApkManagement> query = realm.where(TaglockApkManagement.class);
        RealmResults<TaglockApkManagement> taglockApkManagements = query.findAll();
        return taglockApkManagements;
    }

    public boolean isTaglockAvailable(String id) {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<TaglockApkManagement> query = realm.where(TaglockApkManagement.class);
        query.equalTo("id", id);
        RealmResults<TaglockApkManagement> apkManagements = query.findAll();

        if (apkManagements.size() > 0)
            return true;
        else
            return false;
    }

    public void updateTaglockData(String id, TaglockApkManagement taglockApkManagement ) {

        Realm realm = RealmController.getInstance().getRealm();
        TaglockApkManagement mTaglockApkManagement = new TaglockApkManagement();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        mTaglockApkManagement.setApp_name(taglockApkManagement.getApp_name());
        mTaglockApkManagement.setApp_package_name(taglockApkManagement.getApp_package_name());
        mTaglockApkManagement.setApp_version(taglockApkManagement.getApp_version());
        mTaglockApkManagement.setApp_local_storage_path(taglockApkManagement.getApp_local_storage_path());
        mTaglockApkManagement.setIs_latest_apk_installed(taglockApkManagement.getIs_latest_apk_installed());
        realm.copyToRealm(mTaglockApkManagement);
        realm.commitTransaction();
    }
}
