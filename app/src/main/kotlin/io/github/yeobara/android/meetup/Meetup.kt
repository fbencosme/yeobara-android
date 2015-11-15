package io.github.yeobara.android.meetup

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties("attendees", ignoreUnknown = true)
public class Meetup() {

    val attendees: ArrayList<Attendee> = arrayListOf()
    val created: Long = 0L
    val date: String = ""
    val description: String = ""
    val friendlyName: String = ""
    val hashcode: String = ""
    val host: String = ""
    var nearest: Boolean = false
}
