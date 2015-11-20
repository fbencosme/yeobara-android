package io.github.yeobara.android.gcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.gcm.GcmListenerService
import io.github.yeobara.android.R
import io.github.yeobara.android.sign.SignInActivity
import io.github.yeobara.android.utils.UiUtils

class YeoGcmListenerService : GcmListenerService() {

    companion object {
        private val TAG = "SledgeGcmListenerService"
    }

    /**
     * Called when message is received.

     * @param from SenderID of the sender.
     * *
     * @param data Data bundle containing message data as key/value pairs.
     * *             For Set of keys use data.keySet().
     */
    override fun onMessageReceived(from: String?, data: Bundle?) {
        if (from == null || data == null) return
        val title = data.getString("title")
        val body = data.getString("body")
        Log.d(TAG, "From: $from")
        Log.d(TAG, "Title: $title")
        Log.d(TAG, "Message: $body")

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         * - Store message in local database.
         * - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(title, body)
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param title GCM title
     * @param message GCM message received.
     */
    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT)

        val color = UiUtils.getColor(applicationContext, R.color.primary)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setLights(color, 1000, 3000)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(color)

        val notification = notificationBuilder.build();
        notification.defaults = notification.defaults or NotificationCompat.DEFAULT_SOUND
        notification.defaults = notification.defaults or NotificationCompat.DEFAULT_VIBRATE

        val systemService = getSystemService(Context.NOTIFICATION_SERVICE)
        val notificationManager = systemService as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notification)
    }
}

