package io.github.yeobara.android.meetup

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attendee {

    val lastUpdate: String = ""
    val nickname: String = ""
    val status: String = ""
}
