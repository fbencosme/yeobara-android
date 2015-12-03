package io.github.yeobara.android.sign

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.firebase.client.AuthData
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.AccountPicker
import com.google.android.gms.common.GoogleApiAvailability
import io.github.yeobara.android.R
import io.github.yeobara.android.app.Const
import io.github.yeobara.android.app.YeobaraApp
import io.github.yeobara.android.meetup.MeetupActivity
import io.github.yeobara.android.utils.AppUtils
import io.github.yeobara.android.utils.PrefUtils
import kotlinx.android.synthetic.activity_signin.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class SignInActivity : AppCompatActivity(), SignInView {

    @Inject lateinit var homeRef: Firebase

    var email: String? = null
    val presenter: SignInPresenter by lazy {
        SignInPresenter(this, homeRef)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        YeobaraApp.app.component.inject(this)
        presenter.attach(this)
        initUi()
        tryToSignIn()
    }

    override fun onDestroy() {
        presenter.detach(this)
        super.onDestroy()
    }

    private fun tryToSignIn() {
        val token = PrefUtils.getAccessToken(this)
        if (token.isNotEmpty()) {
            presenter.loginGooglePlus(token, AuthHandler(token))
        }
    }

    private fun initUi() {
        val click: ((View) -> Unit) = { presenter.showAccountsDialog() }
        logo.setOnClickListener(click)
        signin.setOnClickListener(click)
    }

    private fun getRequest(email: String?): Observable<String>? {
        return Observable.just(email)
                .map { mail ->
                    val account = Account(mail, "com.google")
                    GoogleAuthUtil.getToken(this, account, Const.SIGNIN_SCOPE)
                }
    }

    private fun startMeetupActivity() {
        val intent = Intent(this, MeetupActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Const.REQUEST_PICK_ACCOUNT -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                        presenter.signIn(getRequest(email))
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // nothing
                }
            }
            Const.REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR -> {
                presenter.signIn(getRequest(email))
            }
        }
    }

    override fun showAccountsDialog() {
        val email = email
        if (email == null) {
            presenter.pickUserAccount()
            return
        }

        val request = getRequest(email)
        presenter.signIn(request)
    }

    override fun showAccountChooser() {
        if (!AppUtils.checkPlayServices(this)) {
            return
        }

        try {
            val accountTypes = arrayOf("com.google")
            val intent = AccountPicker.newChooseAccountIntent(
                    null, null, accountTypes, false, null, null, null, null)
            startActivityForResult(intent, Const.REQUEST_PICK_ACCOUNT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun showLoading(show: Boolean) {
        logo.isEnabled = !show
        signin.isEnabled = !show
        progress.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun showResult(result: String) {
        presenter.loginGooglePlus(result, AuthHandler(result))
    }

    override fun showError(error: Throwable) {
        val api = GoogleApiAvailability.getInstance()
        if (error is GooglePlayServicesAvailabilityException) {
            val statusCode = error.connectionStatusCode
            val dialog = api.getErrorDialog(this, statusCode,
                    Const.REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR)
            dialog.show()
        } else if (error is UserRecoverableAuthException) {
            val intent = error.intent
            startActivityForResult(intent,
                    Const.REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR)
        } else {
            error.printStackTrace()
        }
    }

    override fun showComplete() {
    }

    override fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun startYeobara(token: String) {
        PrefUtils.setAccessToken(this, token)
        startMeetupActivity()
    }

    override fun clearToken(token: String) {
        PrefUtils.clearAll(this)
        Observable.just(token)
                .map { token -> GoogleAuthUtil.clearToken(this, token) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    inner class AuthHandler(val token: String) : Firebase.AuthResultHandler {
        override fun onAuthenticationError(error: FirebaseError?) {
            clearToken(token)
            showLoading(false)
        }

        override fun onAuthenticated(authData: AuthData?) {
            if (authData != null) {
                startYeobara(token)
            }
            showLoading(false)
        }
    }
}
