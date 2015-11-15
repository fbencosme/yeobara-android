package io.github.yeobara.android.meetup

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attendee(val id: String,
                      val lastUpdate: Long,
                      val nickname: String,
                      var status: String) {

    constructor() : this("", 0L, "", "") {
    }
}
