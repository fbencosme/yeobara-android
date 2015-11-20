package io.github.yeobara.android.utils

import android.app.Activity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.github.yeobara.android.app.Const

public object AppUtils {

    fun checkPlayServices(activity: Activity): Boolean {
        val api = GoogleApiAvailability.getInstance();
        val resultCode = api.isGooglePlayServicesAvailable(activity)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(resultCode)) {
                api.getErrorDialog(activity, resultCode,
                        Const.REQUEST_PLAY_SERVICES_RESOLUTION).show()
            }
            return false
        }
        return true
    }
}