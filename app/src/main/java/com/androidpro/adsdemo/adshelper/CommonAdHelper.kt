package com.app.mytipsjob.utils.adshelper

import android.app.Activity
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.get
import com.androidpro.adsdemo.adshelper.checkForInternet
import com.androidpro.adsdemo.adshelper.nativeads.TemplateView
import com.androidpro.adsdemo.databinding.NativeAdLayout1Binding
import com.androidpro.adsdemo.databinding.NativeBannerAdLayoutBinding
import com.app.mytipsjob.utils.MyApp
import com.facebook.ads.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

class CommonAdHelper {

    companion object {
        val TAG = "CommonAdHelper"

        lateinit var myApp: MyApp

        var isAdmobEnabled = false
        var isFbEnabled = false
        var isUnityEnabled = false

        var timeForAdEnable = 60000L

        fun init(application: MyApp) {
            if (this::myApp.isInitialized) {
                return
            }

            myApp = application
        }

        //for unity
        var unityAppKey = ""

//        //for admob
//        var admobBannerId = "ca-app-pub-3940256099942544/6300978111"
//        var admobInterId = "ca-app-pub-3940256099942544/1033173712"
//        var admobOpenAppId = "ca-app-pub-3940256099942544/3419835294"
//        var admobRewardedId = "ca-app-pub-3940256099942544/5224354917"
//        var admobInterRewardedId = "ca-app-pub-3940256099942544/5354046379"
//        var admobNativeId = "ca-app-pub-3940256099942544/2247696110"

        var admobBannerId = ""
        var admobInterId = ""
        var admobOpenAppId = ""
        var admobNativeId = ""

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

        var admobBannerEnabled = false
        var admobInterEnabled = false
        var admobAppOpenEnabled = false
        var admobNativeEnabled = false

        var isAdmobInterLoading = false
        var isAdmobAppOpenLoading = false

        //for fb
//        var fbBannerId = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"
//        var fbInterId = "YOUR_PLACEMENT_ID"
//        var fbRewardedId = "YOUR_PLACEMENT_ID"
//        var fbInterRewardedId = "YOUR_PLACEMENT_ID"
//        var fbNativeId = "YOUR_PLACEMENT_ID"
//        var fbBannerNativeId = "YOUR_PLACEMENT_ID"

        var fbBannerId = ""
        var fbInterId = ""
        var fbNativeId = ""
        var fbBannerNativeId = ""

        var fbInterstitialAd: com.facebook.ads.InterstitialAd? = null

        var fbInterMoreLoadTry = true
        var fbBannerMoreLoadTry = true
        var fbNativeBannerMoreLoadTry = true
        var fbNativeMoreLoadTry = true

        var fbBannerTimerRunning = false
        var fbInterTimerRunning = false
        var fbNativeBannerTimerRunning = false
        var fbNativeTimerRunning = false

        var fbInterEnabled = false
        var fbBannerEnabled = false
        var fbNativeEnabled = false
        var fbNativeBannerEnabled = false

        var isFbInterLoading = false

        var onFbAdClicked: (() -> Unit)? = null
        var onFbAdDismiss: (() -> Unit)? = null

        fun showAdmobBannerAd(
            adContainer: LinearLayout,
            adSize: AdSize,
            onAdClicked: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null
        ) {
            if (!checkForInternet(myApp.applicationContext) || !isAdmobEnabled || !admobBannerEnabled || !admobBannerMoreLoadTry) {
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
            adView.adUnitId = admobBannerId
            adContainer.addView(adView)
            val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
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
        }

        fun showFbBannerAd(
            adContainer: LinearLayout,
            adSize: com.facebook.ads.AdSize,
            onAdClicked: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null
        ) {
            if (!checkForInternet(myApp.applicationContext) || !isFbEnabled || !fbBannerEnabled || !fbBannerMoreLoadTry) {
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

            val adView = com.facebook.ads.AdView(
                adContainer.context,
                fbBannerId,
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
        }

        fun loadAdmobInterAd() {
            if (!checkForInternet(myApp.applicationContext) || !isAdmobEnabled || !admobInterEnabled || !admobInterMoreLoadTry || admobInterstitialAd != null || isAdmobInterLoading) {
                return
            }

            isAdmobInterLoading = true

            val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()

            InterstitialAd.load(
                myApp.applicationContext, admobInterId, adRequest,
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
            activity: Activity,
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

        fun loadFbInterAd(onAdLoad: (() -> Unit)? = null,onError: (() -> Unit)? = null) {
            if (!checkForInternet(myApp.applicationContext) || !isFbEnabled || !fbInterEnabled || !fbInterMoreLoadTry || fbInterstitialAd != null || isFbInterLoading) {
                onError?.invoke()
                return
            }

            isFbInterLoading = true

            val interstitialAd = InterstitialAd(myApp.applicationContext, fbInterId)

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

        fun loadAdmobAppOpenAd(onAdLoad: (() -> Unit)? = null,onError: (() -> Unit)? = null) {
            if (!checkForInternet(myApp.applicationContext) || !isAdmobEnabled || !admobAppOpenEnabled || !admobAppOpenMoreLoadTry || admobAppOpenAd != null || isAdmobAppOpenLoading) {
                onError?.invoke()
                return
            }

            isAdmobAppOpenLoading = true

            val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()

            AppOpenAd.load(
                myApp.applicationContext, admobOpenAppId, adRequest,
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
            activity: Activity,
            onAdClicked: (() -> Unit)? = null,
            onAdDismiss: (() -> Unit)? = null,
            onAdFailedToLoad: ((AdError) -> Unit)? = null
        ) {
            if (admobAppOpenAd != null && !isAdmobAppOpenAdShowing) {
                admobAppOpenAd?.show(activity)

                admobAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
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
            if (!checkForInternet(myApp.applicationContext) || !isAdmobEnabled || !admobNativeEnabled || !admobNativeMoreLoadTry) {
                onAdFailedToLoad?.invoke()
                return
            }
            val adLoader = AdLoader.Builder(nativeAdView.context, admobNativeId)
                .forNativeAd { ad: NativeAd ->
                    nativeAd?.invoke(ad)
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

            adLoader.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
        }

        fun showFbNativeBannerAd(
            nativeAdContainer: NativeAdLayout,
            onAdClicked: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null,
            loadedNativeAd: ((NativeBannerAd) -> Unit)? = null
        ) {
            if (!checkForInternet(myApp.applicationContext) || !isFbEnabled || !fbNativeBannerEnabled || !fbNativeBannerMoreLoadTry) {
                onAdFailedToLoad?.invoke()
                return
            }

            val nativeBannerAd = NativeBannerAd(nativeAdContainer.context, fbBannerNativeId)

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
            if (!checkForInternet(myApp.applicationContext) || !isFbEnabled || !fbNativeEnabled || !fbNativeMoreLoadTry) {
                onAdFailedToLoad?.invoke()
                return
            }

            val nativeAd = NativeAd(nativeAdContainer.context, fbNativeId)

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

//        fun showUnityInterAd(
//            adClose: () -> Unit,
//            adShowFailed: () -> Unit
//        ){
//            IronSource.setLevelPlayInterstitialListener(object : LevelPlayInterstitialListener {
//                override fun onAdReady(adInfo: AdInfo) {
//                    Log.d(TAG, "onAdReady: ====>")
//                }
//
//                override fun onAdLoadFailed(error: IronSourceError) {
//                    Log.d(TAG, "onAdLoadFailed: ====>" + error.errorMessage)
//                }
//
//                override fun onAdOpened(adInfo: AdInfo) {}
//                override fun onAdClosed(adInfo: AdInfo) {
//                    adClose.invoke()
//                    loadUnityInterAd()
//                }
//
//                override fun onAdShowFailed(error: IronSourceError, adInfo: AdInfo) {
//                    adShowFailed.invoke()
//                }
//
//                override fun onAdClicked(adInfo: AdInfo) {}
//                override fun onAdShowSucceeded(adInfo: AdInfo) {}
//            })
//
//            if (IronSource.isInterstitialReady()){
//                IronSource.showInterstitial()
//            }else{
//                adShowFailed.invoke()
//                loadUnityInterAd()
//            }
//        }

        fun destroyAds(
            bannerContainer: LinearLayout? = null,
            fbNativeAd: com.facebook.ads.NativeAd? = null,
            fbNativeBannerAd: NativeBannerAd? = null,
            nativeAd: NativeAd? = null
        ) {
            bannerContainer?.childCount?.let {
                if (it > 0) {
                    if (bannerContainer[0] is com.facebook.ads.AdView) {
                        (bannerContainer[0] as com.facebook.ads.AdView).destroy()
                    } else if (bannerContainer[0] is AdView) {
                        (bannerContainer[0] as AdView).destroy()
                    }
                }
            }

            fbNativeAd?.destroy()
            nativeAd?.destroy()
            fbNativeBannerAd?.destroy()
        }

//        fun loadUnityInterAd(){
//            if (!checkForInternet(myApp.applicationContext) || !isUnityEnabled || IronSource.isInterstitialReady()) {
//                return
//            }
//            IronSource.loadInterstitial()
//        }

        fun startTimerForAdEnable(onFinish: () -> Unit) {
            object : CountDownTimer(timeForAdEnable, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    onFinish.invoke()
                }
            }.start()
        }
    }

    /*fun loadAdmobInterRewardedAd() {
            if (!checkForInternet(myApp.applicationContext) || !isAdmobEnabled || !admobInterRewardedEnabled || !admobInterRewardedMoreLoadTry || admobInterRewardedAd != null || isAdmobInterRewardedLoading) {
                return
            }

            isAdmobInterRewardedLoading = true

            val adRequest = AdRequest.Builder().build()

            RewardedInterstitialAd.load(
                myApp.applicationContext, admobInterRewardedId, adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                        isAdmobInterRewardedLoading = false
                        admobInterRewardedAd = rewardedInterstitialAd
                        Log.d(TAG, "onAdLoaded:loadAdmobInterRewardedAd ====>")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(TAG, "onAdFailedToLoad:loadAdmobInterRewardedAd ====>" + loadAdError.message)
                        isAdmobInterRewardedLoading = false
                        admobInterRewardedAd = null
                        admobInterRewardedMoreLoadTry = false

                        if (!admobInterRewardedTimerRunning) {
                            admobInterRewardedTimerRunning = true
                            startTimerForAdEnable {
                                Log.d(TAG, "onAdFailedToLoad:loadAdmobInterRewardedAd ====> true")
                                admobInterRewardedMoreLoadTry = true
                                admobInterRewardedTimerRunning = false
                            }
                        }
                    }
                })
        }

        fun showAdmobInterRewardedAd(
            activity: Activity,
            onAdClicked: (() -> Unit)? = null,
            onAdDismiss: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null,
            onEarnedInterRewarded: (() -> Unit)? = null
        ) {
            if (admobInterRewardedAd != null) {
                admobInterRewardedAd?.show(activity) {
                    onEarnedInterRewarded?.invoke()
                }

                admobInterRewardedAd?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            onAdClicked?.invoke()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            onAdDismiss?.invoke()
                            admobInterRewardedAd = null
                            loadAdmobInterRewardedAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.d(
                                TAG,
                                "onAdFailedToShowFullScreenContent:showAdmobInterRewardedAd ====>" + adError.message
                            )
                            onAdFailedToLoad?.invoke()
                            admobInterRewardedAd = null
                            loadAdmobInterRewardedAd()
                        }
                    }
            } else {
                loadAdmobInterRewardedAd()
                onAdFailedToLoad?.invoke()
            }
        }*/

    /*fun loadFbRewardedAd() {
            if (!checkForInternet(myApp.applicationContext) || !isFbEnabled || !fbRewardedEnabled || !fbRewardedMoreLoadTry || fbRewardedAd != null || isFbRewardedLoading) {
                return
            }

            isFbRewardedLoading = true

            val rewardedVideoAd = RewardedVideoAd(myApp.applicationContext, fbRewardedId)

            val rewardedVideoAdListener = object : RewardedVideoAdListener {
                override fun onAdLoaded(ad: Ad) {
                    isFbRewardedLoading = false
                    fbRewardedAd = rewardedVideoAd
                    Log.d(TAG, "onAdLoaded:loadFbRewardedAd ====>")
                }

                override fun onError(ad: Ad?, adError: com.facebook.ads.AdError) {
                    Log.d(TAG, "onError:loadFbRewardedAd ====>" + adError.errorMessage)
                    isFbRewardedLoading = false
                    fbRewardedAd = null
                    fbRewardedMoreLoadTry = false

                    if (!fbRewardedTimerRunning) {
                        fbRewardedTimerRunning = true
                        startTimerForAdEnable {
                            Log.d(TAG, "onError:loadFbRewardedAd ====> true")
                            fbRewardedMoreLoadTry = true
                            fbRewardedTimerRunning = false
                        }
                    }
                }

                override fun onAdClicked(ad: Ad) {
                    onFbAdClicked?.invoke()
                }

                override fun onLoggingImpression(ad: Ad) {

                }

                override fun onRewardedVideoCompleted() {
                    onFbEarnReward?.invoke()
                    fbRewardedAd = null
                    loadFbRewardedAd()
                }

                override fun onRewardedVideoClosed() {
                    fbRewardedAd = null
                    loadFbRewardedAd()
                }
            }

            rewardedVideoAd.loadAd(
                rewardedVideoAd.buildLoadAdConfig()
                    .withAdListener(rewardedVideoAdListener)
                    .build()
            )
        }

        fun showFbRewardedAd(
            onAdClicked: (() -> Unit)? = null,
            onAdFailedToLoad: (() -> Unit)? = null,
            onEarnReward: (() -> Unit)? = null
        ) {
            if (fbRewardedAd != null) {
                onFbAdClicked = onAdClicked
                onFbEarnReward = onEarnReward
                fbRewardedAd?.show()
            } else {
                loadFbRewardedAd()
                onAdFailedToLoad?.invoke()
            }
        }*/
}