package com.androidpro.adsdemo.adshelper.newadshelper.base

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.androidpro.adsdemo.R
import com.app.mytipsjob.ui.InitActivity
import com.app.mytipsjob.ui.LoginActivity
import com.app.mytipsjob.ui.RewardActivity
import com.app.mytipsjob.ui.SelectAssetsActivity
import com.app.mytipsjob.utils.ForegroundListener
import com.app.mytipsjob.utils.adshelper.AdHelper
import com.yanzhenjie.loading.dialog.LoadingDialog

open class BaseActivity : AppCompatActivity() {

    lateinit var loadingDialog: LoadingDialog
    var TAG = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        super.onCreate(savedInstanceState)
        TAG = localClassName
    }

    override fun onResume() {
        super.onResume()
        if (this !is InitActivity) {
            AdHelper.apply {
                loadFbInterAd()
                loadAdmobInterAd()
                loadAdmobAppOpenAd()
            }
        }
        setForegroundListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this is InitActivity) {
            AdHelper.foregroundListener = null
        }
    }

    private fun setForegroundListener() {
        AdHelper.foregroundListener = object : ForegroundListener {
            override fun background() {
                AdHelper.loadAdmobAppOpenAd()
            }

            override fun foreground() {
                AdHelper.showAdmobAppOpenAd(this@BaseActivity)
            }
        }
    }

    override fun onBackPressed() {
        var isFailedToShow = false
        if (this !is InitActivity &&
            this !is LoginActivity &&
            this !is SelectAssetsActivity &&
            this !is RewardActivity
        ) {
            AdHelper.apply {
                Log.d(TAG, "onBackPressed: ====>" + clickCount)
                if (adConfigModel.isAdLimitEnabled != true && clickCount > adConfigModel.maxAdShowClickCount!! - 1) {
                    showFbInterAd(onAdDismiss = {
                        super.onBackPressed()
                    }, onAdFailedToLoad = {
                        showAdmobInterAd(this@BaseActivity, onAdDismiss = {
                            super.onBackPressed()
                        }, onAdFailedToLoad = {
                            super.onBackPressed()
                            isFailedToShow = true
                        })
                    })
                } else {
                    super.onBackPressed()
                }
            }
        } else {
            super.onBackPressed()
        }

        if (AdHelper.adConfigModel.isAdLimitEnabled != true) {
            if (AdHelper.adConfigModel.maxAdShowClickCount!! <= AdHelper.clickCount) {
                if (!isFailedToShow) {
                    AdHelper.clickCount = 1
                }
            } else {
                AdHelper.clickCount++
            }
        }
    }

    fun showLoading() {
        if (isDestroyed || isFinishing) {
            return
        }
        if (this::loadingDialog.isInitialized) {
            if (loadingDialog.isShowing) {
                return
            } else {
                loadingDialog.show()
            }
        } else {
            loadingDialog = LoadingDialog(this)
            loadingDialog.setCircleColors(
                ContextCompat.getColor(this, R.color.blue),
                ContextCompat.getColor(this, R.color.blue),
                ContextCompat.getColor(this, R.color.blue)
            )
            loadingDialog.show()
        }
    }

    fun hideLoading() {
        if (isDestroyed || isFinishing) {
            return
        }
        if (this::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

}