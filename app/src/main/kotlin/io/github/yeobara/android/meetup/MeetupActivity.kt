package io.github.yeobara.android.meetup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.firebase.client.DataSnapshot
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.firebase.client.ValueEventListener
import com.tbruyelle.rxpermissions.RxPermissions
import io.github.importre.eddystone.EddyStone
import io.github.yeobara.android.BuildConfig
import io.github.yeobara.android.R
import io.github.yeobara.android.app.Const
import io.github.yeobara.android.gcm.RegistrationIntentService
import io.github.yeobara.android.setting.SettingsActivity
import io.github.yeobara.android.utils.AppUtils
import io.github.yeobara.android.utils.NetworkUtils
import io.github.yeobara.android.utils.UiUtils
import kotlinx.android.synthetic.activity_meetup.*

class MeetupActivity : AppCompatActivity(), UpdateListener {

    private val adapter: MeetupAdapter by lazy {
        MeetupAdapter(this, this)
    }

    private val eddystone: EddyStone by lazy {
        EddyStone(this, adapter, Const.REQUEST_ENABLE_BLUETOOTH)
    }

    private val userRef: Firebase by lazy {
        Firebase("${BuildConfig.FIREBASE_URL}/users")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meetup)
        setSupportActionBar(toolbar)

        if (userRef.auth == null) {
            finish()
        } else {
            checkPermission()
            initMeetups()
            checkGcm()
        }
    }

    override fun onStart() {
        super.onStart()
        initUser()
        eddystone.start()
    }

    override fun onStop() {
        super.onStop()
        eddystone.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_meetup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_settings -> startSettingsActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(intent, Const.REQUEST_SETTINGS)
    }

    private fun initUser() {
        val auth = userRef.auth ?: return
        val uid = auth.uid ?: return
        val providerData = auth.providerData ?: return

        userRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot?) {
                if (data != null) {
                    val user = getUser(data, uid, providerData)
                    setUser(user)
                }
            }

            override fun onCancelled(error: FirebaseError?) {
                setUser(null)
            }
        })
    }

    private fun getUser(snapshot: DataSnapshot,
                        uid: String, providerData:
                        Map<String, Any>): User? {
        val value = snapshot.value
        return if (value == null) {
            val nickname = providerData["displayName"]?.toString() ?: return null
            val email = providerData["email"]?.toString() ?: return null
            val profile = providerData["profileImageURL"]?.toString()
            val user = User(uid, nickname, email, profileImageURL = profile)
            userRef.child(uid).setValue(user)
            user
        } else {
            snapshot.getValue(User::class.java)
        }
    }

    private fun setUser(user: User?) {
        adapter.setUser(user)
        if (user != null) {
            toolbar.subtitle = "${user.nickname} · ${user.email}"
        }
    }

    private fun initMeetups() {
        val span = if (UiUtils.isLandscape(this)) 2 else 1
        val layoutManager = StaggeredGridLayoutManager(
                span, StaggeredGridLayoutManager.VERTICAL)

        recyclerView.layoutManager = layoutManager
        progress.visibility = if (NetworkUtils.isNetworkConnected(this)) {
            View.VISIBLE
        } else {
            View.GONE
        }
        recyclerView.adapter = adapter
    }

    override fun onUpdate() {
        progress.visibility = View.GONE
    }

    private fun checkPermission() {
        RxPermissions.getInstance(this)
                .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe({ granted ->
                    if (granted) {
                        eddystone.run {
                            init()
                            start()
                        }
                    } else {
                        showPermissionError()
                    }
                }, { error ->
                    showSnackbar(error.message ?: "permission error")
                })
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(coordLayout, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showPermissionError() {
        val message = R.string.error_permission_not_granted
        Snackbar.make(coordLayout, message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(UiUtils.getColor(this, android.R.color.holo_orange_light))
                .setAction(R.string.settings, {
                    val intent = Intent()
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.setData(uri)
                    startActivity(intent)
                })
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK == resultCode) {
            when (requestCode) {
                Const.REQUEST_ENABLE_BLUETOOTH -> {
                    eddystone.run {
                        init()
                        start()
                    }
                }
                Const.REQUEST_SETTINGS -> {
                    finish()
                }
            }
        }
    }

    private fun checkGcm() {
        if (AppUtils.checkPlayServices(this)) {
            // Start IntentService to register this application with GCM.
            val intent = Intent(this, RegistrationIntentService::class.java)
            startService(intent)
        }
    }
}
