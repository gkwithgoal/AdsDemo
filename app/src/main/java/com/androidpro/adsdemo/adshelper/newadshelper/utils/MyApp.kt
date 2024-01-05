package com.app.mytipsjob.utils

import android.app.Application
import android.content.SharedPreferences
import com.app.mytipsjob.BuildConfig
import com.app.mytipsjob.utils.adshelper.AdHelper
import com.app.mytipsjob.utils.adshelper.AdHelper.Companion.foregroundListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onesignal.OneSignal
import net.khirr.library.foreground.Foreground

class MyApp : Application() {

    companion object {
        var pref: SharedPreferences? = null
    }

    override fun onCreate() {
        super.onCreate()
        pref = getSharedPreferences(Const.USER_PREF, MODE_PRIVATE)
        AdHelper.init(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        OneSignal.initWithContext(this)
        OneSignal.setAppId("fabd08ff-f97f-42f8-b846-bb49ca7851aa")
    }
}

interface ForegroundListener {
    fun background()
    fun foreground()
}