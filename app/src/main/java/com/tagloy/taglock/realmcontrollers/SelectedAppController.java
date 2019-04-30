package com.tagloy.taglock.realmcontrollers;

import com.tagloy.taglock.realm.RealmController;
import com.tagloy.taglock.realmmodels.SelectedAllAppDetails;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class SelectedAppController {
    public void addAppData(SelectedAllAppDetails selectedAllAppDetails){
        Realm realm = RealmController.getInstance().getRealm();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        realm.copyFromRealm(selectedAllAppDetails);
        realm.commitTransaction();
    }

    public RealmResults<SelectedAllAppDetails> getApkData() {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<SelectedAllAppDetails> query = realm.where(SelectedAllAppDetails.class);
        RealmResults<SelectedAllAppDetails> selectedAllAppDetails = query.findAll();
        return selectedAllAppDetails;
    }

    public boolean isApkAvailable(String id) {
        Realm realm = RealmController.getInstance().getRealm();
        RealmQuery<SelectedAllAppDetails> query = realm.where(SelectedAllAppDetails.class);
        query.equalTo("id", id);
        RealmResults<SelectedAllAppDetails> selectedAllAppDetails = query.findAll();

        if (selectedAllAppDetails.size() > 0)
            return true;
        else
            return false;
    }

    public void updateApkData(String id, SelectedAllAppDetails selectedAllAppDetails ) {

        Realm realm = RealmController.getInstance().getRealm();
        SelectedAllAppDetails mSelectedAllAppDetails = new SelectedAllAppDetails();

        if (!realm.isInTransaction())
            realm.beginTransaction();

        mSelectedAllAppDetails.setApp_package_name(selectedAllAppDetails.getApp_package_name());
        mSelectedAllAppDetails.setApp_local_storage_path(selectedAllAppDetails.getApp_local_storage_path());
        mSelectedAllAppDetails.setApp_visibility(selectedAllAppDetails.isApp_visibility());
        mSelectedAllAppDetails.setIs_app_enable(selectedAllAppDetails.isIs_app_enable());
        mSelectedAllAppDetails.setIs_Launcher_app(selectedAllAppDetails.isIs_Launcher_app());
        realm.copyToRealm(mSelectedAllAppDetails);
        realm.commitTransaction();
    }
}
