package com.androidpro.adsdemo.adshelper

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.adobwpsit.pdfreadereditor.R
import com.adobwpsit.pdfreadereditor.base.BaseActivity
import com.adobwpsit.pdfreadereditor.databinding.ActivityPurchaseBinding
import com.adobwpsit.pdfreadereditor.utils.GlobalConst
import com.adobwpsit.pdfreadereditor.utils.MyApp
import com.adobwpsit.pdfreadereditor.utils.PrefManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PurchaseActivity : BaseActivity() {

    lateinit var binding: ActivityPurchaseBinding
    lateinit var billingClient: BillingClient
    lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PrefManager(this, GlobalConst.PREFER_OTHER)

        if (prefManager.getString(GlobalConst.IS_PURCHASED) == "yes") {
            binding.tvActivated.visibility = View.VISIBLE
            binding.btBuyNow.isEnabled = false
        }
        initBilling()
        initView()
    }

    private fun initView() {
        binding.apply {
            btBuyNow.setOnClickListener {
                startPurchase()
            }

            btRecovery.setOnClickListener {
                checkPurchaseHistory()
            }

            ivBack.setOnClickListener {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MyApp.foregroundListener = null
    }

    private fun checkPurchaseHistory() {
        val queryPurchasesParams =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        billingClient.queryPurchasesAsync(queryPurchasesParams) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                if (list.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this, "No purchase found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    for (purchase in list) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    private fun startPurchase() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder().setProductList(
                            listOf(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId("product_premium")
                                    .setProductType(BillingClient.ProductType.INAPP).build()
                            )
                        ).build()

                    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billResult, productDetailsList ->
                        val productDetailsParamsList = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetailsList[0])
                                .build()
                        )

                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()

                        // Launch the billing flow
                        billingClient.launchBillingFlow(this@PurchaseActivity, billingFlowParams)
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                runOnUiThread {
                    Toast.makeText(
                        this@PurchaseActivity,
                        getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun initBilling() {
        val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    Log.d("TAG", "USER_CANCELED: ====>")
                    runOnUiThread { Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show() }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@PurchaseActivity,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.d("TAG", "other error: ====>")
                }
            }

        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) {
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                        giveContentToUser()
                    }
                }
            } else {
                giveContentToUser()
            }
        }
    }

    private fun giveContentToUser() {
        runOnUiThread {
            if (prefManager.getString(GlobalConst.IS_PURCHASED).isEmpty()) {
                binding.tvActivated.visibility = View.VISIBLE
                binding.btBuyNow.isEnabled = false
                prefManager.saveString(GlobalConst.IS_PURCHASED, "yes")
                Toast.makeText(this, "Close the app and restart to remove ads", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        if (this::billingClient.isInitialized) {
            billingClient.endConnection()
        }
        super.onDestroy()
    }
}