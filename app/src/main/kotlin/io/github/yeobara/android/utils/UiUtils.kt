package io.github.yeobara.android.utils

import android.content.Context
import android.content.res.Configuration
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.WindowManager

public object UiUtils {

    public fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        return configuration.smallestScreenWidthDp >= 600
    }

    public fun isLandscape(context: Context): Boolean {
        val configuration = context.resources.configuration
        return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    public fun getDisplayWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    public fun getColor(context: Context, @ColorRes color: Int): Int {
        return ContextCompat.getColor(context, color)
    }
}

