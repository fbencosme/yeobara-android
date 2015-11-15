package io.github.yeobara.android.utils

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.ConnectivityManager

public object NetworkUtils {

    public fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = cm.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    public fun getBluetoothMacAddress(): String {
        val adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter?.address ?: throw RuntimeException("unsupported device")
    }
}
