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
        return query.findAll();
    }

    public boolean isAvailablProfileData() {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<DefaultProfile> query = realm.where(DefaultProfile.class);
//        query.equalTo("group_name", group_name);
        RealmResults<DefaultProfile> mProfileRealmData = query.findAll();
        return mProfileRealmData.size() > 0;
    }

    public void updateProfileDataContent(DefaultProfile mProfileData ) {

        Realm realm = RealmController.getInstance().getRealm();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        DefaultProfile mProfiledataRealmClass = realm.where(DefaultProfile.class).findFirst();//equalTo("group_name",group_name).findFirst();
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setGroup_name(mProfileData.getGroup_name());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setApp_package_name(mProfileData.getApp_package_name());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setPasscode(mProfileData.getPasscode());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setNavigationbar_status(mProfileData.isNavigationbar_status());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setClear_data_passcode(mProfileData.getClear_data_passcode());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setTaglock_exited_status(mProfileData.isTaglock_exited_status());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setDefault_apk_call_duration(mProfileData.getDefault_apk_call_duration());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setDefault_apk_version(mProfileData.getDefault_apk_version());
        }
        if (mProfiledataRealmClass != null) {
            realm.copyToRealm(mProfiledataRealmClass);
        }
        realm.commitTransaction();

    }

    public void updateProfile(DefaultProfile mProfileData){
        Realm realm = RealmController.getInstance().getRealm();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        DefaultProfile mProfiledataRealmClass = realm.where(DefaultProfile.class).findFirst();//equalTo("group_name",group_name).findFirst();
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setPasscode(mProfileData.getPasscode());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setClear_data_passcode(mProfileData.getClear_data_passcode());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setGroup_name(mProfileData.getGroup_name());
        }
        if (mProfiledataRealmClass != null) {
            realm.copyToRealm(mProfiledataRealmClass);
        }
        realm.commitTransaction();
    }

    public void updateProfileData(DefaultProfile mProfileData){
        Realm realm = RealmController.getInstance().getRealm();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        DefaultProfile mProfiledataRealmClass = realm.where(DefaultProfile.class).findFirst();//equalTo("group_name",group_name).findFirst();
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setDefault_apk_call_duration(mProfileData.getDefault_apk_call_duration());
        }
        if (mProfiledataRealmClass != null) {
            mProfiledataRealmClass.setApp_package_name(mProfileData.getApp_package_name());
        }
        if (mProfiledataRealmClass != null) {
            realm.copyToRealm(mProfiledataRealmClass);
        }
        realm.commitTransaction();
    }
}
