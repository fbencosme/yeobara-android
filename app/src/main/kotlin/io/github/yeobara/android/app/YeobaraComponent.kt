package io.github.yeobara.android.app

import dagger.Component
import io.github.yeobara.android.meetup.MeetupActivity
import io.github.yeobara.android.sign.SignInActivity
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(YeobaraModule::class))
interface YeobaraComponent {

    fun inject(activity: MeetupActivity)

    fun inject(activity: SignInActivity)
}
