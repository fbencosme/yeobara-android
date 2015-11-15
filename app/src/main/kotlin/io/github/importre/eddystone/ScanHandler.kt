package io.github.importre.eddystone

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

internal class ScanHandler(val eddyStoneCallback: EddyStoneCallback) : ScanCallback() {

    private val TAG = "[EDDYSTONE] ScanHandler"
    private val LOST_TIME: Long = 3000L
    private val deviceToBeaconMap = HashMap<String /* device address */, Beacon>()
    private val beacons = arrayListOf<Beacon>()
    private var lostSub: Subscription? = null

    override fun onScanFailed(errorCode: Int) {
        when (errorCode) {
            ScanCallback.SCAN_FAILED_ALREADY_STARTED ->
                Log.e(TAG, "SCAN_FAILED_ALREADY_STARTED")
            ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED ->
                eddyStoneCallback.onFailure("SCAN_FAILED_APPLICATION_REGISTRATION_FAILED")
            ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED ->
                eddyStoneCallback.onFailure("SCAN_FAILED_FEATURE_UNSUPPORTED")
            ScanCallback.SCAN_FAILED_INTERNAL_ERROR ->
                eddyStoneCallback.onFailure("SCAN_FAILED_INTERNAL_ERROR")
            else ->
                eddyStoneCallback.onFailure("Scan failed, unknown error code")
        }
    }

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        if (result == null) return
        val scanRecord = result.scanRecord ?: return

        val deviceAddress = result.device.address
        var beacon: Beacon
        if (!deviceToBeaconMap.containsKey(deviceAddress)) {
            beacon = Beacon(deviceAddress, result.rssi)
            deviceToBeaconMap.put(deviceAddress, beacon)
            beacons.add(beacon)
        } else {
            deviceToBeaconMap[deviceAddress]?.lastSeenTimestamp = System.currentTimeMillis()
            deviceToBeaconMap[deviceAddress]?.rssi = result.rssi
        }

        val serviceData = scanRecord.getServiceData(EddyStone.UUID)
        validateServiceData(deviceAddress, serviceData)
    }

    private fun validateServiceData(deviceAddress: String, serviceData: ByteArray?) {
        val beacon = deviceToBeaconMap[deviceAddress] as? Beacon ?: return
        if (serviceData == null) {
            val err = "Null Eddystone service data"
            beacon.frameStatus.nullServiceData = err
            eddyStoneCallback.onFailure(err, deviceAddress)
            return
        }

        when (serviceData[0]) {
            Constants.UID_FRAME_TYPE ->
                UidValidator.validate(deviceAddress, serviceData, beacon)
            Constants.TLM_FRAME_TYPE ->
                TlmValidator.validate(deviceAddress, serviceData, beacon)
            Constants.URL_FRAME_TYPE ->
                UrlValidator.validate(deviceAddress, serviceData, beacon)
            else -> {
                val err = "Invalid frame type byte %02X".format(serviceData[0])
                beacon.frameStatus.invalidFrameType = err
                eddyStoneCallback.onFailure(err, deviceAddress)
                return
            }
        }
        eddyStoneCallback.onSuccess(beacons)
    }

    private fun newLostObserver(): Observable<Long> {
        return Observable
                .interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
    }

    public fun start() {
        lostSub?.unsubscribe()
        lostSub = newLostObserver().subscribe({
            val time = System.currentTimeMillis()
            val iter = deviceToBeaconMap.entries.iterator()
            var notify = false
            while (iter.hasNext()) {
                val beacon = iter.next().value
                if ((time - beacon.lastSeenTimestamp) > LOST_TIME) {
                    iter.remove()
                    notify = beacons.remove(beacon)
                }
            }
            if (notify) {
                eddyStoneCallback.onSuccess(beacons)
            }
        }, {
        })
    }

    public fun stop() {
        lostSub?.unsubscribe()
    }
}

