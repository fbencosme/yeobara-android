package io.github.yeobara.android.meetup

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attendee(val fingerprint: String,
                      val lastUpdate: Long,
                      val nickname: String,
                      val status: String) {

    constructor() : this("", 0L, "", "") {
    }
}
