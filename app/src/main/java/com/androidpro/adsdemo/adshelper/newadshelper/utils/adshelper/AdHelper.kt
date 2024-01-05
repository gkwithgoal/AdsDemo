package com.app.mytipsjob.utils.adshelper

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.app.mytipsjob.databinding.NativeAdLayout1Binding
import com.app.mytipsjob.databinding.NativeBannerAdLayoutBinding
import com.androidpro.adsdemo.adshelper.newadshelper.model.AdConfigModel
import com.app.mytipsjob.utils.Const
import com.app.mytipsjob.utils.ForegroundListener
import com.app.mytipsjob.utils.MyApp
import com.app.mytipsjob.utils.Pref
import com.app.mytipsjob.utils.checkForInternet
import com.app.mytipsjob.utils.nativeads.TemplateView
import com.app.mytipsjob.utils.visible
import com.facebook.ads.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import net.khirr.library.foreground.Foreground

class AdHelper {

    companion object {
        val TAG = "CommonAdHelper"

        lateinit var myApp: MyApp
        var adConfigModel = AdConfigModel()
        var foregroundListener: ForegroundListener? = null

        fun init(application: MyApp) {
            myApp = application
            initializeSdk(myApp)
            setForegroundListener(myApp)
            try {
                val jsonConfig = Pref.getString(Const.AD_CONFIG)
                if (jsonConfig.isNotEmpty()) {
                    adConfigModel = Gson().fromJson(jsonConfig, AdConfigModel::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            fetchLatestAdConfig()
        }

        var clickCount = 1

        var admobInterstitialAd: InterstitialAd? = null
        var admobAppOpenAd: AppOpenAd? = null

        var isAdmobAppOpenAdShowing = false

        var admobBannerMoreLoadTry = true
        var admobInterMoreLoadTry = true
        var admobAppOpenMoreLoadTry = true
        var admobNativeMoreLoadTry = true

        var admobBannerTimerRunning = false
        var admobInterTimerRunning = false
        var admobAppOpenTimerRunning = false
        var admobNativeTimerRunning = false

        var isAdmobInterLoading = false
        var isAdmobAppOpenLoading = false

        var fbInterstitialAd: com.facebook.ads.InterstitialAd? = null

        var fbInterMoreLoadTry = true
        var fbBannerMoreLoadTry = true
        var fbNativeBannerMoreLoadTry = true
        var fbNativeMoreLoadTry = true

        var fbBannerTimerRunning = false
        var fbInterTimerRunning = false
        var fbNativeBannerTimerRunning = false
        var fbNativeTimerRunning = false

        var isFbInterLoading = false

        var onFbAdClicked: (() -> Unit)? = null
        var onFbAdDismiss: (() -> Unit)? = null

        fun showAdmobBannerAd(
            adContainer: LinearLayout,
            adSize: AdSize,
            onAdClicked: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null
        ) {
            if (!checkForInternet(myApp.applicationContext) || adConfigModel.isAdmobEnabled != true || adConfigModel.admobBanner.isNullOrEmpty() || !admobBannerMoreLoadTry) {
                onAdFailedToLoad?.invoke()
                return
            }

            if (adContainer.childCount > 0) {
                if (adContainer[0] is AdView) {
                    return
                } else {
                    if (adContainer[0] is com.facebook.ads.AdView) {
                        (adContainer[0] as com.facebook.ads.AdView).destroy()
                    }
                    adContainer.removeAllViews()
                }
            }

            val adView = AdView(adContainer.context)
            adView.setAdSize(adSize)
            adView.adUnitId = adConfigModel.admobBanner ?: "ca-app-pub-3940256099942544/6300978111"
            adContainer.addView(adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            adView.adListener = object : AdListener() {
                override fun onAdClicked() {
                    onAdClicked?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    admobBannerMoreLoadTry = false
                    if (!admobBannerTimerRunning) {
                        admobBannerTimerRunning = true
                        startTimerForAdEnable {
                            Log.d(TAG, "onAdFailedToLoad:showAdmobBannerAd ====> true")
                            admobBannerMoreLoadTry = true
                            admobBannerTimerRunning = false
                        }
                    }
                    onAdFailedToLoad?.invoke()
                    Log.d(TAG, "onAdFailedToLoad:showAdmobBannerAd ====>" + loadAdError.message)
                }

                override fun onAdLoaded() {
                    Log.d(TAG, "onAdLoaded:showAdmobBannerAd ====>")
                }
            }

            (adView.context as AppCompatActivity).listenActivityDestroyState {
                adView.destroy()
                Log.d(TAG, "showAdmobBannerAd:listenActivityDestroyState ====>")
            }
        }

        fun showFbBannerAd(
            adContainer: LinearLayout,
            adSize: com.facebook.ads.AdSize,
            onAdClicked: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null
        ) {
            if (!checkForInternet(myApp.applicationContext) || adConfigModel.isFbEnabled != true || adConfigModel.fbBanner.isNullOrEmpty() || !fbBannerMoreLoadTry) {
                onAdFailedToLoad?.invoke()
                return
            }
            if (adContainer.childCount > 0) {
                if (adContainer[0] is com.facebook.ads.AdView) {
                    return
                } else {
                    if (adContainer[0] is AdView) {
                        (adContainer[0] as AdView).destroy()
                    }
                    adContainer.removeAllViews()
                }
            }

            val adView = AdView(
                adContainer.context,
                adConfigModel.fbBanner,
                adSize
            )
            adContainer.addView(adView)
            adView.loadAd(adView.buildLoadAdConfig()?.withAdListener(object :
                com.facebook.ads.AdListener {
                override fun onError(p0: Ad?, p1: com.facebook.ads.AdError) {
                    fbBannerMoreLoadTry = false

                    if (!fbBannerTimerRunning) {
                        fbBannerTimerRunning = true
                        startTimerForAdEnable {
                            Log.d(TAG, "onError:showFbBannerAd ====> true")
                            fbBannerMoreLoadTry = true
                            fbBannerTimerRunning = false
                        }
                    }
                    onAdFailedToLoad?.invoke()
                    Log.d(TAG, "onError:showFbBannerAd ====>" + p1.errorMessage)
                }

                override fun onAdLoaded(p0: Ad?) {
                    Log.d(TAG, "onAdLoaded:showFbBannerAd ====>")
                }

                override fun onAdClicked(p0: Ad?) {
                    onAdClicked?.invoke()
                }

                override fun onLoggingImpression(p0: Ad?) {
                }
            })?.build())

            (adView.context as AppCompatActivity).listenActivityDestroyState {
                adView.destroy()
                Log.d(TAG, "showFbBannerAd:listenActivityDestroyState ====>")
            }
        }

        fun loadAdmobInterAd() {
            if (!checkForInternet(myApp.applicationContext) || adConfigModel.isAdmobEnabled != true || adConfigModel.admobInter.isNullOrEmpty() || !admobInterMoreLoadTry || admobInterstitialAd != null || isAdmobInterLoading) {
                return
            }

            isAdmobInterLoading = true

            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(
                myApp.applicationContext,
                adConfigModel.admobInter ?: "ca-app-pub-3940256099942544/1033173712",
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        isAdmobInterLoading = false
                        admobInterstitialAd = interstitialAd
                        Log.d(TAG, "onAdLoaded:loadAdmobInterAd ====>")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(TAG, "onAdFailedToLoad:loadAdmobInterAd ====>" + loadAdError.message)
                        isAdmobInterLoading = false
                        admobInterstitialAd = null
                        admobInterMoreLoadTry = false

                        if (!admobInterTimerRunning) {
                            admobInterTimerRunning = true
                            startTimerForAdEnable {
                                Log.d(TAG, "onAdFailedToLoad:loadAdmobInterAd ====> true")
                                admobInterMoreLoadTry = true
                                admobInterTimerRunning = false
                            }
                        }
                    }
                })
        }

        fun showAdmobInterAd(
            activity: AppCompatActivity,
            onAdClicked: (() -> Unit)? = null,
            onAdDismiss: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null
        ) {
            if (admobInterstitialAd != null) {
                admobInterstitialAd?.show(activity)


                admobInterstitialAd?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            onAdClicked?.invoke()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            onAdDismiss?.invoke()
                            admobInterstitialAd = null
                            loadAdmobInterAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.d(
                                TAG,
                                "onAdFailedToShowFullScreenContent:showAdmobInterAd ====>" + adError.message
                            )
                            onAdFailedToLoad?.invoke()
                            admobInterstitialAd = null
                            loadAdmobInterAd()
                        }
                    }
            } else {
                loadAdmobInterAd()
                onAdFailedToLoad?.invoke()
            }
        }

        fun loadFbInterAd(onAdLoad: (() -> Unit)? = null, onError: (() -> Unit)? = null) {
            if (!checkForInternet(myApp.applicationContext) || adConfigModel.isFbEnabled != true || adConfigModel.fbInter.isNullOrEmpty() || !fbInterMoreLoadTry || fbInterstitialAd != null || isFbInterLoading) {
                if (fbInterstitialAd != null) {
                    onAdLoad?.invoke()
                } else {
                    onError?.invoke()
                }
                return
            }

            isFbInterLoading = true

            val interstitialAd = InterstitialAd(myApp.applicationContext, adConfigModel.fbInter)

            val interstitialAdListener = object : InterstitialAdListener {
                override fun onAdLoaded(ad: Ad) {
                    isFbInterLoading = false
                    fbInterstitialAd = interstitialAd
                    onAdLoad?.invoke()
                    Log.d(TAG, "onAdLoaded:loadFbInterAd ====>")
                }

                override fun onInterstitialDismissed(ad: Ad) {
                    onFbAdDismiss?.invoke()
                    fbInterstitialAd = null
                    loadFbInterAd()
                }

                override fun onError(ad: Ad?, adError: com.facebook.ads.AdError) {
                    Log.d(TAG, "onError:loadFbInterAd ====>" + adError.errorMessage)
                    onError?.invoke()
                    isFbInterLoading = false
                    fbInterstitialAd = null
                    fbInterMoreLoadTry = false

                    if (!fbInterTimerRunning) {
                        fbInterTimerRunning = true
                        startTimerForAdEnable {
                            Log.d(TAG, "onError:loadFbInterAd ====> true")
                            fbInterMoreLoadTry = true
                            fbInterTimerRunning = false
                        }
                    }
                }

                override fun onInterstitialDisplayed(ad: Ad) {
                }

                override fun onAdClicked(ad: Ad) {
                    onFbAdClicked?.invoke()
                }

                override fun onLoggingImpression(ad: Ad) {
                }
            }

            interstitialAd.loadAd(
                interstitialAd.buildLoadAdConfig()
                    .withAdListener(interstitialAdListener)
                    .build()
            )
        }

        fun showFbInterAd(
            onAdClicked: (() -> Unit)? = null,
            onAdDismiss: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null
        ) {
            if (fbInterstitialAd != null) {
                onFbAdClicked = onAdClicked
                onFbAdDismiss = onAdDismiss
                fbInterstitialAd?.show()
            } else {
                loadFbInterAd()
                onAdFailedToLoad?.invoke()
            }
        }

        fun loadAdmobAppOpenAd(onAdLoad: (() -> Unit)? = null, onError: (() -> Unit)? = null) {
            if (!checkForInternet(myApp.applicationContext) || adConfigModel.isAdmobEnabled != true || adConfigModel.admobAppOpen.isNullOrEmpty() || !admobAppOpenMoreLoadTry || admobAppOpenAd != null || isAdmobAppOpenLoading) {
                if (admobAppOpenAd != null) {
                    onAdLoad?.invoke()
                } else {
                    onError?.invoke()
                }
                return
            }

            isAdmobAppOpenLoading = true

            val adRequest = AdRequest.Builder().build()

            AppOpenAd.load(
                myApp.applicationContext,
                adConfigModel.admobAppOpen ?: "ca-app-pub-3940256099942544/9257395921",
                adRequest,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        isAdmobAppOpenLoading = false
                        admobAppOpenAd = ad
                        onAdLoad?.invoke()
                        Log.d(TAG, "onAdLoaded:loadAdmobAppOpenAd ====>")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(
                            TAG,
                            "onAdFailedToLoad:loadAdmobAppOpenAd ====>" + loadAdError.message
                        )
                        onError?.invoke()
                        isAdmobAppOpenLoading = false
                        admobAppOpenAd = null
                        admobAppOpenMoreLoadTry = false

                        if (!admobAppOpenTimerRunning) {
                            admobAppOpenTimerRunning = true
                            startTimerForAdEnable {
                                Log.d(
                                    TAG,
                                    "onAdFailedToLoad:loadAdmobAppOpenAd ====> true"
                                )
                                admobAppOpenMoreLoadTry = true
                                admobAppOpenTimerRunning = false
                            }
                        }
                    }
                }
            )
        }

        fun showAdmobAppOpenAd(
            activity: AppCompatActivity,
            onAdClicked: (() -> Unit)? = null,
            onAdDismiss: (() -> Unit)? = null,
            onAdFailedToLoad: ((AdError) -> Unit)? = null
        ) {
            if (admobAppOpenAd != null && !isAdmobAppOpenAdShowing) {
                activity.checkActivityResumeState {
                    admobAppOpenAd?.show(activity)

                    admobAppOpenAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                onAdClicked?.invoke()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                onAdDismiss?.invoke()
                                isAdmobAppOpenAdShowing = false
                                admobAppOpenAd = null
                                loadAdmobAppOpenAd()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.d(
                                    TAG,
                                    "onAdFailedToShowFullScreenContent:showAdmobAppOpenAd ====>" + adError.message
                                )
                                onAdFailedToLoad?.invoke(adError)
                                admobAppOpenAd = null
                                loadAdmobAppOpenAd()
                            }

                            override fun onAdShowedFullScreenContent() {
                                isAdmobAppOpenAdShowing = true
                            }
                        }
                }
            } else {
                loadAdmobAppOpenAd()
            }
        }

        fun showAdmobNativeAd(
            nativeAdView: TemplateView,
            onAdClicked: (() -> Unit)? = null,
            onAdLoad: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null,
            nativeAd: ((NativeAd) -> Unit)? = null
        ) {
            if (!checkForInternet(myApp.applicationContext) || adConfigModel.isAdmobEnabled != true || adConfigModel.admobNative.isNullOrEmpty() || !admobNativeMoreLoadTry) {
                onAdFailedToLoad?.invoke()
                return
            }
            val adLoader = AdLoader.Builder(
                nativeAdView.context,
                adConfigModel.admobNative ?: "ca-app-pub-3940256099942544/2247696110"
            )
                .forNativeAd { ad: NativeAd ->
                    nativeAd?.invoke(ad)
                    (nativeAdView.context as AppCompatActivity).listenActivityDestroyState {
                        ad.destroy()
                    }
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        admobNativeMoreLoadTry = false

                        if (!admobNativeTimerRunning) {
                            admobNativeTimerRunning = true
                            startTimerForAdEnable {
                                Log.d(TAG, "onAdFailedToLoad:showAdmobNativeAd ====> true")
                                admobNativeMoreLoadTry = true
                                admobNativeTimerRunning = false
                            }
                        }
                        onAdFailedToLoad?.invoke()
                        Log.d(TAG, "onAdFailedToLoad:showAdmobNativeAd ====>" + adError.message)
                    }

                    override fun onAdClicked() {
                        onAdClicked?.invoke()
                    }

                    override fun onAdLoaded() {
                        onAdLoad?.invoke()
                        Log.d(TAG, "onAdLoaded:showAdmobNativeAd ====>")
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder().build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }

        fun showFbNativeBannerAd(
            nativeAdContainer: NativeAdLayout,
            onAdClicked: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null,
            loadedNativeAd: ((NativeBannerAd) -> Unit)? = null
        ) {
            if (!checkForInternet(myApp.applicationContext) || adConfigModel.isFbEnabled != true || adConfigModel.fbNativeBanner.isNullOrEmpty() || !fbNativeBannerMoreLoadTry) {
                onAdFailedToLoad?.invoke()
                return
            }

            val nativeBannerAd =
                NativeBannerAd(nativeAdContainer.context, adConfigModel.fbNativeBanner)

            val nativeAdListener = object : NativeAdListener {
                override fun onMediaDownloaded(ad: Ad) {
                }

                override fun onError(ad: Ad?, adError: com.facebook.ads.AdError) {
                    fbNativeBannerMoreLoadTry = false

                    if (!fbNativeBannerTimerRunning) {
                        fbNativeBannerTimerRunning = true
                        startTimerForAdEnable {
                            Log.d(TAG, "onError:showFbNativeBannerAd ====> true")
                            fbNativeBannerMoreLoadTry = true
                            fbNativeBannerTimerRunning = false
                        }
                    }
                    onAdFailedToLoad?.invoke()
                    Log.d(TAG, "onError:showFbNativeBannerAd ====>" + adError.errorMessage)
                }

                override fun onAdLoaded(ad: Ad) {
                    loadedNativeAd?.invoke(nativeBannerAd)
                    (nativeAdContainer.context as AppCompatActivity).listenActivityDestroyState {
                        ad.destroy()
                    }
                    Log.d(TAG, "onAdLoaded:showFbNativeBannerAd ====>")
                }

                override fun onAdClicked(ad: Ad) {
                    onAdClicked?.invoke()
                }

                override fun onLoggingImpression(ad: Ad) {
                }
            }

            nativeBannerAd.loadAd(
                nativeBannerAd.buildLoadAdConfig()
                    .withAdListener(nativeAdListener)
                    .build()
            )
        }

        fun inflateNativeBannerAd(
            nativeBannerAd: NativeBannerAd,
            nativeAdLayout: NativeAdLayout
        ) {
            nativeAdLayout.visible()
            nativeBannerAd.unregisterView()

            val binding =
                NativeBannerAdLayoutBinding.inflate(LayoutInflater.from(nativeAdLayout.context))
            nativeAdLayout.addView(binding.root)

            val adOptionsView =
                AdOptionsView(nativeAdLayout.context, nativeBannerAd, nativeAdLayout)
            binding.apply {
                adChoicesContainer.removeAllViews()
                adChoicesContainer.addView(adOptionsView, 0)

                nativeAdTitle.text = nativeBannerAd.advertiserName
                nativeAdSocialContext.text = nativeBannerAd.adSocialContext
                nativeAdCallToAction.visibility =
                    if (nativeBannerAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
                nativeAdCallToAction.text = nativeBannerAd.adCallToAction
                nativeAdSponsoredLabel.text = nativeBannerAd.sponsoredTranslation

                val clickableViews: MutableList<View> = ArrayList()
                clickableViews.add(nativeAdTitle)
                clickableViews.add(nativeAdCallToAction)

                nativeBannerAd.registerViewForInteraction(
                    binding.root, nativeIconView, clickableViews
                )
            }
        }

        fun showFbNativeAd(
            nativeAdContainer: NativeAdLayout,
            onAdClicked: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null,
            loadedNativeAd: ((com.facebook.ads.NativeAd) -> Unit)? = null
        ) {
            if (!checkForInternet(myApp.applicationContext) || adConfigModel.isFbEnabled != true || adConfigModel.fbNative.isNullOrEmpty() || !fbNativeMoreLoadTry) {
                onAdFailedToLoad?.invoke()
                return
            }

            val nativeAd = NativeAd(nativeAdContainer.context, adConfigModel.fbNative)

            val nativeAdListener = object : NativeAdListener {
                override fun onMediaDownloaded(ad: Ad) {
                }

                override fun onError(ad: Ad?, adError: com.facebook.ads.AdError) {
                    onAdFailedToLoad?.invoke()
                    fbNativeMoreLoadTry = false

                    if (!fbNativeTimerRunning) {
                        fbNativeTimerRunning = true
                        startTimerForAdEnable {
                            Log.e(TAG, "onError:showFbNativeAd ====> true")
                            fbNativeMoreLoadTry = true
                            fbNativeTimerRunning = false
                        }
                    }
                    Log.e(TAG, "onError:showFbNativeAd ====>" + adError.errorMessage)
                }

                override fun onAdLoaded(ad: Ad) {
                    loadedNativeAd?.invoke(nativeAd)
                    (nativeAdContainer.context as AppCompatActivity).listenActivityDestroyState {
                        ad.destroy()
                    }
                    Log.d(TAG, "onAdLoaded:showFbNativeAd ====>")
                }

                override fun onAdClicked(ad: Ad) {
                    onAdClicked?.invoke()
                }

                override fun onLoggingImpression(ad: Ad) {
                }
            }

            nativeAd.loadAd(
                nativeAd.buildLoadAdConfig()
                    .withAdListener(nativeAdListener)
                    .build()
            )
        }

        fun inflateNativeAd(
            nativeAd: com.facebook.ads.NativeAd,
            nativeAdLayout: NativeAdLayout
        ) {
            nativeAdLayout.visible()
            nativeAd.unregisterView()

            val binding =
                NativeAdLayout1Binding.inflate(LayoutInflater.from(nativeAdLayout.context))
            nativeAdLayout.addView(binding.root)

            val adOptionsView = AdOptionsView(nativeAdLayout.context, nativeAd, nativeAdLayout)
            binding.apply {
                adChoicesContainer.removeAllViews()
                adChoicesContainer.addView(adOptionsView, 0)

                nativeAdTitle.text = nativeAd.advertiserName
                nativeAdBody.text = nativeAd.adBodyText
                nativeAdSocialContext.text = nativeAd.adSocialContext
                nativeAdCallToAction.visibility =
                    if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
                nativeAdCallToAction.text = nativeAd.adCallToAction
                nativeAdSponsoredLabel.text = nativeAd.sponsoredTranslation

                val clickableViews: MutableList<View> = ArrayList()
                clickableViews.add(nativeAdTitle)
                clickableViews.add(nativeAdCallToAction)

                nativeAd.registerViewForInteraction(
                    binding.root, nativeAdMedia, nativeAdIcon, clickableViews
                )
            }
        }

        private fun startTimerForAdEnable(onFinish: () -> Unit) {
            object : CountDownTimer(adConfigModel.timeForAdEnable ?: 20000, 1000) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    onFinish.invoke()
                }
            }.start()
        }

        private fun fetchLatestAdConfig() {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()

            val remoteConfig = FirebaseRemoteConfig.getInstance()
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val jsonConfig = if (BuildConfig.DEBUG) {
                            remoteConfig.getString("testAdInfo")
                        } else {
                            remoteConfig.getString("v${BuildConfig.VERSION_CODE}")
                        }
                        if (jsonConfig.isNotEmpty()) {
                            try {
                                Pref.saveString(Const.AD_CONFIG, jsonConfig)
                                adConfigModel =
                                    Gson().fromJson(jsonConfig, AdConfigModel::class.java)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        Log.d("TAG", "onCreate: ====>$jsonConfig")
                    }
                }
        }

        private fun initializeSdk(context: Context) {
            AudienceNetworkAds
                .buildInitSettings(context)
                .initialize()

            MobileAds.initialize(context)

            if (BuildConfig.DEBUG) {
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
                            AdRequest.DEVICE_ID_EMULATOR
                        )
                    ).build()
            )
        }

        private fun setFbTestDevice(context: Context) {
            //for fb
            AdSettings.turnOnSDKDebugger(context)

            AdSettings.addTestDevices(
                listOf(
                    "a770aabf-11b3-4090-bed3-7453717c365f"
                )
            )
        }

        private fun setForegroundListener(myApp: MyApp) {
            Foreground.init(myApp)

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
}

fun View.setOnShowInterAdClickListener(onClick: (View) -> Unit) {
    this.setOnClickListener {
        AdHelper.apply {
            var isFailedToShow = false
            Log.d(TAG, "setOnShowInterAdClickListener: ====>$clickCount")
            if (clickCount > adConfigModel.maxAdShowClickCount!! - 1) {
                showFbInterAd(onAdDismiss = {
                    onClick.invoke(this@setOnShowInterAdClickListener)
                }, onAdFailedToLoad = {
                    showAdmobInterAd(
                        (this@setOnShowInterAdClickListener.context as AppCompatActivity),
                        onAdDismiss = {
                            onClick.invoke(this@setOnShowInterAdClickListener)
                        },
                        onAdFailedToLoad = {
                            onClick.invoke(this@setOnShowInterAdClickListener)
                            isFailedToShow = true
                        })
                })
            } else {
                onClick.invoke(this@setOnShowInterAdClickListener)
            }

            if (adConfigModel.maxAdShowClickCount!! <= clickCount) {
                if (!isFailedToShow){
                    clickCount = 1
                }
            } else {
                clickCount++
            }
        }
    }
}

fun AppCompatActivity.checkActivityResumeState(onResume: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        if (this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            onResume.invoke()
        }
    }, 250)
}

fun AppCompatActivity.listenActivityDestroyState(onDestroy: () -> Unit) {
    this.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (Lifecycle.Event.ON_DESTROY == event) {
                onDestroy.invoke()
                this@listenActivityDestroyState.lifecycle.removeObserver(this)
            }
        }
    })
}
