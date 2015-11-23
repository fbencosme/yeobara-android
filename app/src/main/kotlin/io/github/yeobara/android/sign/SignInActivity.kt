package io.github.yeobara.android.sign

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
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
import io.github.yeobara.android.meetup.MeetupActivity
import io.github.yeobara.android.utils.AppUtils
import io.github.yeobara.android.utils.PrefUtils
import kotlinx.android.synthetic.activity_signin.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class SignInActivity : AppCompatActivity() {

    companion object {
        public val SCOPE_PROFILE: String = "https://www.googleapis.com/auth/userinfo.profile"
        public val SCOPE_EMAIL: String = "https://www.googleapis.com/auth/userinfo.email"
        public val SCOPE: String = "oauth2:$SCOPE_PROFILE $SCOPE_EMAIL"
    }

    private var email: String? = null
    public val homeRef: Firebase by lazy {
        Firebase("${Const.FB_BASE}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        initUi()
        trySignIn()
    }

    private fun trySignIn() {
        val token = PrefUtils.getAccessToken(this)
        if (token.isNotEmpty()) {
            showProgress(true)
            loginGooglePlus(token)
        }
    }

    private fun initUi() {
        val click: ((View) -> Unit) = { signIn() }
        logo.setOnClickListener(click)
        signin.setOnClickListener(click)
    }

    private fun signIn() {
        val email = email
        if (email == null) {
            pickUserAccount()
            return
        }

        showProgress(true)
        Observable.just(email)
                .map({ mail ->
                    val account = Account(mail, "com.google")
                    GoogleAuthUtil.getToken(this, account, SCOPE)
                })
                .filter { it != null }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ token ->
                    loginGooglePlus(token)
                }, { error ->
                    handleErrorOfSignIn(error)
                })
    }

    private fun handleErrorOfSignIn(error: Throwable?) {
        showProgress(false)
        if (error == null) return
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

    private fun showProgress(show: Boolean) {
        logo.isEnabled = !show
        signin.isEnabled = !show
        progress.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun pickUserAccount() {
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

    private fun loginGooglePlus(accessToken: String) {
        homeRef.authWithOAuthToken("google", accessToken, object : Firebase.AuthResultHandler {
            override fun onAuthenticationError(error: FirebaseError?) {
                if (error != null) {
                    clearToken(accessToken)
                }
                showProgress(false)
            }

            override fun onAuthenticated(authData: AuthData?) {
                if (authData != null) {
                    saveToken(accessToken)
                    startMainActivity()
                } else {
                    showProgress(false)
                }
            }
        })
    }

    private fun clearToken(accessToken: String) {
        PrefUtils.clearAll(this)
        Observable.just(accessToken)
                .map({ token ->
                    GoogleAuthUtil.clearToken(this, token)
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    private fun saveToken(accessToken: String) {
        PrefUtils.setAccessToken(this, accessToken)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MeetupActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Const.REQUEST_PICK_ACCOUNT == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    signIn()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // nothing
            }
        } else if (Const.REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR == requestCode) {
            signIn()
        }
    }
}
