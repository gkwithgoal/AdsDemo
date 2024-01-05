package com.app.mytipsjob.utils

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.app.mytipsjob.R
import com.app.mytipsjob.adapter.SelectorAdapter
import com.app.mytipsjob.databinding.BottomSelecterBinding
import com.app.mytipsjob.utils.adshelper.AdHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.toastLong(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun checkForInternet(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}

fun showSelectorBottomSheet(context: Context, list: List<String>, click: (String) -> Unit) {
    val loginBottom = BottomSheetDialog(context)

    val bottomBinding =
        BottomSelecterBinding.inflate(LayoutInflater.from(context), null, false)
    loginBottom.setContentView(bottomBinding.root)
    (bottomBinding.root.parent as View).setBackgroundColor(
        ContextCompat.getColor(context, android.R.color.transparent)
    )

    val mBehavior =
        BottomSheetBehavior.from(bottomBinding.root.parent as View)

    loginBottom.setOnShowListener { dialog: DialogInterface ->
        val d = dialog as BottomSheetDialog
        val bottomSheet = d.findViewById<ConstraintLayout>(R.id.bottomdialog)
        BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        mBehavior.setPeekHeight(bottomBinding.root.height)
    }

    bottomBinding.rvItem.adapter = SelectorAdapter(context, list, click = {
        click.invoke(it)
        loginBottom.dismiss()
    })

    loginBottom.show()
}
