package io.github.importre.eddystone

import java.util.*

public interface EddyStoneCallback {

    fun onSuccess(beacons: ArrayList<Beacon>)

    fun onFailure(message: String, deviceAddress: String? = null)
}

