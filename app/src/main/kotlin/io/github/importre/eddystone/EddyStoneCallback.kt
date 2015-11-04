package io.github.importre.eddystone

import java.util.*

interface EddyStoneCallback {

    fun onSuccess(beacons: ArrayList<Beacon>)

    fun onFailure(message: String, deviceAddress: String? = null)
}

