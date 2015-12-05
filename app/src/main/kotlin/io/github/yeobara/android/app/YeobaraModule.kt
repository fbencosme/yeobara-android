package io.github.yeobara.android.app

import com.firebase.client.Firebase
import dagger.Module
import dagger.Provides
import io.github.yeobara.android.BuildConfig
import javax.inject.Singleton

@Module
class YeobaraModule {

    @Provides
    @Singleton
    fun provideSignInPresenter(): Firebase = Firebase(BuildConfig.FIREBASE_URL)
}