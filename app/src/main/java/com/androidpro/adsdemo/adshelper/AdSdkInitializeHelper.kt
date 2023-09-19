package com.app.mytipsjob.utils.adshelper

import android.content.Context
import android.util.Log
import com.app.mytipsjob.BuildConfig.DEBUG
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration


class AdSdkInitializeHelper {

    companion object {
        fun initialize(context: Context) {
            AudienceNetworkAds
                .buildInitSettings(context)
                .initialize()

            MobileAds.initialize(context)

            if (DEBUG) {
                setFbTestDevice(context)
                setAdmobTestDevice()
            }
        }

        private fun setAdmobTestDevice() {
            //for admob
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(
                        listOf(
                            AdRequest.DEVICE_ID_EMULATOR,
                            "C4EED87FD0DB4B45CAFA803F01EA40EE"
                        )
                    ).build()
            )
        }

        private fun setFbTestDevice(context: Context) {
            //for fb
            AdSettings.turnOnSDKDebugger(context)

            AdSettings.addTestDevices(
                listOf(
                    "c3d0dbc9-3074-428c-9ff1-38ee15cb9a04"
                )
            )
        }
    }
}