package com.app.mytipsjob.utils

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.app.mytipsjob.BuildConfig
import com.app.mytipsjob.utils.adshelper.AdSdkInitializeHelper
import com.app.mytipsjob.utils.adshelper.CommonAdHelper
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onesignal.OneSignal
import net.khirr.library.foreground.Foreground

class MyApp : Application() {

    companion object {
        var pref: SharedPreferences? = null
        var foregroundListener: ForegroundListener? = null
    }

    override fun onCreate() {
        super.onCreate()
        pref = getSharedPreferences(Const.USER_PREF, MODE_PRIVATE)
        CommonAdHelper.init(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        setForegroundListener()
        OneSignal.initWithContext(this)
        OneSignal.setAppId("fabd08ff-f97f-42f8-b846-bb49ca7851aa")
    }

    private fun setForegroundListener() {
        Foreground.init(this)

        val foregroundListener = object : Foreground.Listener {
            override fun background() {
                foregroundListener?.background()
            }

            override fun foreground() {
                foregroundListener?.foreground()
            }
        }

        Foreground.addListener(foregroundListener)
    }

}

interface ForegroundListener {
    fun background()
    fun foreground()
}