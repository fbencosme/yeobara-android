package io.github.yeobara.android.app

import android.app.Application
import com.firebase.client.Firebase

public class YeobaraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.setAndroidContext(this)
    }
}