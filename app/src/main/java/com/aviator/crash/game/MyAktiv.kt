package com.aviator.crash.game

import android.app.Application
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.onesignal.OneSignal

class MyAktiv: Application() {

    override fun onCreate() {
        super.onCreate()
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        OneSignal.initWithContext(this)
        OneSignal.setAppId("0c102005-d25b-40f8-b331-bafcc0404bdb")
    }
}