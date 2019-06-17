package com.tagloy.taglock;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.github.anrwatchdog.ANRWatchDog;
import com.tagloy.taglock.realm.RealmController;
import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class MyApplication extends Application {
    Realm mrealm;

    @Override
    public void onCreate() {
        super.onCreate();
        new ANRWatchDog(25000).setReportMainThreadOnly().start();
        Fabric.with(this, new Crashlytics());
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        this.mrealm = RealmController.with(this).getRealm();

//for db debugging purpose you can view db structure in chrome
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).withLimit(100000).build())
                        .build());
    }
}
