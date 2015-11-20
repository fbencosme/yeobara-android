package io.github.yeobara.android.utils

import android.content.Context
import android.preference.PreferenceManager

public object PrefUtils {

    private val PREF_ACCESS_TOKEN: String = "pref_access_token"
    private val PREF_SENT_TOKEN_TO_SERVER = "pref_sent_token_to_server"

    public fun clearAll(context: Context) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp.edit()
                .remove(PREF_ACCESS_TOKEN)
                .remove(PREF_SENT_TOKEN_TO_SERVER)
                .apply()
    }

    public fun getAccessToken(context: Context): String {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getString(PREF_ACCESS_TOKEN, "")
    }

    public fun setAccessToken(context: Context, token: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp.edit().putString(PREF_ACCESS_TOKEN, token).apply()
    }

    public fun setSentGcmTokenToServer(context: Context, checked: Boolean) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp.edit().putBoolean(PREF_SENT_TOKEN_TO_SERVER, checked).apply()
    }
}
