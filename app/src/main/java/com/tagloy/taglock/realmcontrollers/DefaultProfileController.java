package com.tagloy.taglock.realmcontrollers;

import com.tagloy.taglock.realm.RealmController;
import com.tagloy.taglock.realmmodels.DefaultProfile;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class DefaultProfileController {

    public void addDefaultProfileData(DefaultProfile mRealmMetaData) {
        Realm realm = RealmController.getInstance().getRealm();
        if (!realm.isInTransaction())
            realm.beginTransaction();

        realm.copyToRealm(mRealmMetaData);
        realm.commitTransaction();

    }

    public RealmResults<DefaultProfile> geDefaultProfileData() {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<DefaultProfile> query = realm.where(DefaultProfile.class);
        RealmResults<DefaultProfile> mRealmDefaultProfile = query.findAll();
        return mRealmDefaultProfile;
    }

    public boolean isAvailablProfileData(String group_name) {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<DefaultProfile> query = realm.where(DefaultProfile.class);
        query.equalTo("group_name", group_name);
        RealmResults<DefaultProfile> mProfileRealmData = query.findAll();
        return mProfileRealmData.size() > 0;
    }

    public void updateProfileDataContent(String group_name, DefaultProfile mProdileData ) {

        Realm realm = RealmController.getInstance().getRealm();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        DefaultProfile mProfiledataRealmClass = realm.where(DefaultProfile.class).equalTo("group_name",group_name).findFirst();
        mProfiledataRealmClass.setGroup_name(mProdileData.getGroup_name());
        mProfiledataRealmClass.setApp_package_name(mProdileData.getApp_package_name());
        mProfiledataRealmClass.setPasscode(mProdileData.getPasscode());
        mProfiledataRealmClass.setNavigationbar_status(mProdileData.isNavigationbar_status());
        mProfiledataRealmClass.setClear_data_passcode(mProdileData.getClear_data_passcode());
        mProfiledataRealmClass.setTaglock_exited_status(mProdileData.isTaglock_exited_status());
        mProfiledataRealmClass.setDefault_apk_call_duration(mProdileData.getDefault_apk_call_duration());
        mProfiledataRealmClass.setDefault_apk_version(mProdileData.getDefault_apk_version());
        realm.copyToRealm(mProfiledataRealmClass);
        realm.commitTransaction();

    }
}
