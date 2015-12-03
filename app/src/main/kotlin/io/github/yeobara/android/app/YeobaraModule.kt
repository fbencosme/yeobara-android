package io.github.yeobara.android.app

import com.firebase.client.Firebase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class YeobaraModule {

    @Provides
    @Singleton
    fun provideSignInPresenter(): Firebase = Firebase(Const.FB_BASE)
}