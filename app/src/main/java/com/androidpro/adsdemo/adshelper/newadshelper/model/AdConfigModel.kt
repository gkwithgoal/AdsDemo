package com.androidpro.adsdemo.adshelper.newadshelper.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AdConfigModel(
	@field:SerializedName("isAdmobEnabled")
	val isAdmobEnabled: Boolean? = false,
	@field:SerializedName("isFbEnabled")
	val isFbEnabled: Boolean? = false,
	@field:SerializedName("isAdLimitEnabled")
	val isAdLimitEnabled: Boolean? = true,

	@field:SerializedName("admobAppOpen")
	val admobAppOpen: String? = "ca-app-pub-3940256099942544/9257395921",
	@field:SerializedName("admobInterReward")
	val admobInterReward: String? = "ca-app-pub-3940256099942544/5354046379",
	@field:SerializedName("admobBanner")
	val admobBanner: String? = "ca-app-pub-3940256099942544/6300978111",
	@field:SerializedName("admobNative")
	val admobNative: String? = "ca-app-pub-3940256099942544/2247696110",
	@field:SerializedName("admobInter")
	val admobInter: String? = "ca-app-pub-3940256099942544/1033173712",
	@field:SerializedName("admobReward")
	val admobReward: String? = "ca-app-pub-3940256099942544/5224354917",

	@field:SerializedName("fbReward")
	val fbReward: String? = "YOUR_PLACEMENT_ID",
	@field:SerializedName("fbInterReward")
	val fbInterReward: String? = "YOUR_PLACEMENT_ID",
	@field:SerializedName("fbBanner")
	val fbBanner: String? = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID",
	@field:SerializedName("fbNativeBanner")
	val fbNativeBanner: String? = "YOUR_PLACEMENT_ID",
	@field:SerializedName("fbInter")
	val fbInter: String? = "YOUR_PLACEMENT_ID",
	@field:SerializedName("fbNative")
	val fbNative: String? = "YOUR_PLACEMENT_ID",

	@field:SerializedName("isPurchaseEnabled")
	val isPurchaseEnabled: Boolean? = false,
	@field:SerializedName("timeForAdEnable")
	val timeForAdEnable: Long? = 20000,
	@field:SerializedName("isReviewEnabled")
	val isReviewEnabled: Boolean? = true,
	@field:SerializedName("rewardTimer")
	val rewardTimer: Long? = 15000,
	@field:SerializedName("isUnityEnabled")
	val isUnityEnabled: Boolean? = false,
	@field:SerializedName("unityAppKey")
	val unityAppKey: String? = "1a1ee2585",
	@field:SerializedName("appVersion")
	val appVersion: Int? = 12,
	@field:SerializedName("maxAdShowClickCount")
	val maxAdShowClickCount: Int? = 3,
	@field:SerializedName("privacyLink")
	val privacyLink: String? = "https://mytradetipsapp.blogspot.com/2023/05/privacy-policy.html",
	@field:SerializedName("termsLink")
	val termsLink: String? = "https://mytradetipsapp.blogspot.com/2023/05/terms-conditions.html"
	)
