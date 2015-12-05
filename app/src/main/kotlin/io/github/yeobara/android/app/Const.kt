package io.github.yeobara.android.app

object Const {

    const public val GITHUB: String = "https://github.com/yeobara"
    const public val GITHUB_ANDROID: String = "https://github.com/yeobara/yeobara-android"

    const public val SCOPE_PROFILE: String = "https://www.googleapis.com/auth/userinfo.profile"
    const public val SCOPE_EMAIL: String = "https://www.googleapis.com/auth/userinfo.email"
    const public val SIGNIN_SCOPE: String = "oauth2:$SCOPE_PROFILE $SCOPE_EMAIL"

    const public val RSVP: String = "rsvp"
    const public val CHECKIN: String = "checkin"
    const public val CHECKED: String = "checked"

    const public val REQUEST_PICK_ACCOUNT: Int = 200
    const public val REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR: Int = 201
    const public val REQUEST_PLAY_SERVICES_RESOLUTION: Int = 202
    const public val REQUEST_SETTINGS: Int = 300
    const public val REQUEST_ENABLE_BLUETOOTH: Int = 400
}