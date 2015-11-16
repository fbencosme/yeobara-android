package io.github.yeobara.android.utils

import android.content.Context
import android.preference.PreferenceManager

public object PrefUtils {

    private val PREF_ACCESS_TOKEN: String = "pref_access_token"

    public fun clearAll(context: Context) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp.edit()
                .remove(PREF_ACCESS_TOKEN)
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
}
