package io.github.yeobara.android

import com.firebase.client.Firebase
import io.github.yeobara.android.sign.SignInPresenter
import io.github.yeobara.android.sign.SignInView
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as When

class SignInTest {

    @Mock
    lateinit var view: SignInView

    @Mock
    lateinit var homeRef: Firebase

    lateinit var presenter: SignInPresenter

    @Mock
    lateinit var oauthHandler: Firebase.AuthResultHandler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = SignInPresenter(view, homeRef)
    }

    @Test
    fun showAccountsDialog() {
        presenter.showAccountsDialog()
        verify(view).showAccountsDialog()
    }

    @Test
    fun pickUserAccount() {
        presenter.pickUserAccount()
        verify(view).showAccountChooser()
    }

    @Test
    fun loginGooglePlus() {
        presenter.loginGooglePlus("token", oauthHandler)

        verify(view).showLoading(true)
        verify(homeRef).authWithOAuthToken("google", "token", oauthHandler)
    }
}