package io.github.yeobara.android.meetup

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties("attendees", ignoreUnknown = true)
public class Meetup() {

    var nearest: Boolean = false

    val attendees: ArrayList<User> = arrayListOf()
    val created: Long = 0L
    val date: String = ""
    val description: String = ""
    val friendlyName: String = ""
    val hashcode: String = ""
    val host: String = ""
    val latLng: LatLong = LatLong()
    val formattedAddress: String = ""
}
