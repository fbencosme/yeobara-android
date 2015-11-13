package io.github.yeobara.android.meetup

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties("attendees")
public class Meetup {

    val attendees: ArrayList<Attendee> = arrayListOf()
    val date: String = ""
    val description: String = ""
    val friendlyName: String = ""
    val host: String = ""
}
