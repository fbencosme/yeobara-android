package io.github.yeobara.android.utils

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils

public object AppUtils {

    public fun getFingerprint(): String {
        val address = NetworkUtils.getBluetoothMacAddress()
        return String(Hex.encodeHex(DigestUtils.sha1(address)))
    }
}
