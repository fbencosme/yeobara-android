package io.github.yeobara.android.meetup

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
public class User(val id: String,
                  val nickname: String,
                  val email: String,
                  val profileImageURL: String? = null) {

    constructor() : this("", "", "") {
    }
}
