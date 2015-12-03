package io.github.yeobara.android.sign

import io.github.yeobara.android.app.RxView

interface SignInView : RxView<String> {

    fun showAccountsDialog()

    fun showAccountChooser()

    fun showErrorMessage(message: String)

    fun startYeobara(token: String)

    fun clearToken(token: String)
}

