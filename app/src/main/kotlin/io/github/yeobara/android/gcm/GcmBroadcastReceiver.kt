package io.github.yeobara.android.gcm

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.content.WakefulBroadcastReceiver

class GcmBroadcastReceiver : WakefulBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val comp = ComponentName(context.packageName, YeoGcmListenerService::class.java.name)
        startWakefulService(context, (intent.setComponent(comp)))
        resultCode = Activity.RESULT_OK
    }
}
