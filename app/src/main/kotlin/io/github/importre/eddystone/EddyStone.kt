package io.github.importre.eddystone

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import java.util.*

public class EddyStone(activity: Activity, val cb: EddyStoneCallback, val requestCodeForBT: Int) {

    companion object {
        public val UUID: ParcelUuid = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
    }

    private val scanFilterList: ArrayList<ScanFilter> = arrayListOf()
    private var scanner: BluetoothLeScanner? = null
    private val scanHandler: ScanHandler
    private val settings: ScanSettings

    init {
        scanHandler = ScanHandler(cb)
        settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()

        val ctx = activity.applicationContext
        val blManager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        blManager.adapter?.let {
            initScanner(activity, it, requestCodeForBT)
            initScanFilter()
        }
    }

    private fun initScanner(activity: Activity, btAdapter: BluetoothAdapter, requestCode: Int) {
        if (!btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, requestCode)
        } else {
            scanner = btAdapter.bluetoothLeScanner
        }
    }

    private fun initScanFilter() {
        ScanFilter.Builder().setServiceUuid(UUID)?.build()?.let {
            scanFilterList.add(it)
        }
    }

    public fun start() {
        try {
            scanner?.let {
                it.startScan(scanFilterList, settings, scanHandler)
                scanHandler.start()
            }
        } catch (e: Exception) {
        }
    }

    public fun stop() {
        try {
            scanner?.let {
                it.stopScan(scanHandler)
                scanHandler.stop()
            }
        } catch (e: Exception) {
        }
    }
}