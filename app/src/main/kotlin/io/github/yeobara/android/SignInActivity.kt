package io.github.yeobara.android

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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.github.yeobara.android.utils.PrefUtils
import kotlinx.android.synthetic.activity_signin.progress
import kotlinx.android.synthetic.activity_signin.signin
import kotlinx.android.synthetic.activity_signin.toolbar
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
        setSupportActionBar(toolbar)
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
        signin.setOnClickListener {
            signIn()
        }
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
                .filter {
                    it != null
                }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ token ->
                    loginGooglePlus(token)
                }, { error ->
                    showProgress(false)
                    val api = GoogleApiAvailability.getInstance();
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
                })
    }

    private fun showProgress(show: Boolean) {
        signin.isEnabled = !show
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun pickUserAccount() {
        if (!checkPlayServices()) {
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
                    val msg = "${error.message} : ${error.details}"
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                }
                showProgress(false)
            }

            override fun onAuthenticated(authData: AuthData?) {
                if (authData != null) {
                    Toast.makeText(this@SignInActivity, R.string.yeobara, Toast.LENGTH_SHORT).show()
                    saveToken(accessToken)
                    startMainActivity()
                } else {
                    showProgress(false)
                }
            }
        });
    }

    private fun saveToken(accessToken: String) {
        PrefUtils.setAccessToken(this, accessToken)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MeetupActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkPlayServices(): Boolean {
        val api = GoogleApiAvailability.getInstance();
        val resultCode = api.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(resultCode)) {
                api.getErrorDialog(this, resultCode, Const.REQUEST_PLAY_SERVICES_RESOLUTION).show()
            }
            return false
        }
        return true
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
        }
    }
}
