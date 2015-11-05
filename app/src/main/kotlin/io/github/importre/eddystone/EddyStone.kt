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

public class EddyStone(activity: Activity, val cb: EddyStoneCallback) {

    companion object {
        public val UUID: ParcelUuid = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
        public val REQUEST_ENABLE_BLUETOOTH: Int = 100
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
            initScanner(activity, it)
            initScanFilter()
        }
    }

    private fun initScanner(activity: Activity, btAdapter: BluetoothAdapter) {
        if (!btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
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
        scanner?.let {
            it.startScan(scanFilterList, settings, scanHandler)
            scanHandler.start()
        }
    }

    public fun stop() {
        scanner?.let {
            it.stopScan(scanHandler)
            scanHandler.stop()
        }
    }
}