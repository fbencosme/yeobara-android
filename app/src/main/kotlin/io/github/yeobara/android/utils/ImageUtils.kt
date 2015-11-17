package io.github.yeobara.android.utils

object ImageUtils {

    public fun getGoogleMapUrl(width: Int, height: Int,
                               lat: Float, lng: Float, zoom: Int): String {
        return "http://maps.google.com/maps/api/staticmap?" +
                "center=$lat,$lng&" +
                "zoom=$zoom&size=${width}x$height" +
                "&sensor=false"
    }
}