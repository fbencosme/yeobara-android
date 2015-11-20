package io.github.yeobara.android.gcm

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.firebase.client.Firebase
import com.google.android.gms.gcm.GcmPubSub
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import io.github.yeobara.android.BuildConfig
import io.github.yeobara.android.app.Const
import io.github.yeobara.android.utils.PrefUtils
import java.io.IOException

class RegistrationIntentService : IntentService(RegistrationIntentService.TAG) {

    companion object {
        private val TAG = "RegIntentService"
        private val TOPICS = arrayOf("global")
    }

    override fun onHandleIntent(intent: Intent) {
        try {
            val instanceID = InstanceID.getInstance(this)
            val token = instanceID.getToken(BuildConfig.GCM_SENDER_ID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)
            Log.i(TAG, "GCM Registration Token: " + token)

            sendRegistrationToServer(token)

            // Subscribe to topic channels
            // subscribeTopics(token)

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            PrefUtils.setSentGcmTokenToServer(this, true)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to complete token refresh", e)
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            PrefUtils.setSentGcmTokenToServer(this, false)
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.

     * @param gcmToken The new token.
     */
    private fun sendRegistrationToServer(gcmToken: String) {
        val userRef = Firebase("${Const.FB_BASE}/users")
        val uid = userRef.auth?.uid ?: return
        userRef.child("$uid/gcmToken").setValue(gcmToken)
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.

     * @param token GCM token
     * *
     * @throws IOException if unable to reach the GCM PubSub service
     */
    @Throws(IOException::class)
    private fun subscribeTopics(token: String) {
        val pubSub = GcmPubSub.getInstance(this)
        for (topic in TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null)
        }
    }
}

