package io.github.yeobara.android

import android.content.Context
import android.content.res.Configuration

public object UiUtils {

    public fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        return configuration.smallestScreenWidthDp >= 600
    }

    public fun isLandscape(context: Context): Boolean {
        val configuration = context.resources.configuration
        return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}

