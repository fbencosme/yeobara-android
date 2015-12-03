package io.github.yeobara.android.sign

import com.firebase.client.Firebase
import io.github.yeobara.android.app.RxPresenter
import rx.Observable

class SignInPresenter(private val view: SignInView,
                      private val homeRef: Firebase) : RxPresenter<String, SignInView>() {

    fun showAccountsDialog() {
        view.showAccountsDialog()
    }

    fun pickUserAccount() {
        view.showAccountChooser()
    }

    fun signIn(request: Observable<String>?) {
        if (request == null) {
            view.showErrorMessage("Failed to get sign-in token")
            return
        }

        start(request.cache())
    }

    fun loginGooglePlus(token: String, handler: Firebase.AuthResultHandler) {
        view.showLoading(true)
        homeRef.authWithOAuthToken("google", token, handler)
    }
}