package io.github.yeobara.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.EditText
import com.firebase.client.DataSnapshot
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.firebase.client.ValueEventListener
import com.tbruyelle.rxpermissions.RxPermissions
import io.github.importre.eddystone.EddyStone
import io.github.yeobara.android.meetup.MeetupAdapter
import io.github.yeobara.android.meetup.UpdateListener
import io.github.yeobara.android.meetup.User
import io.github.yeobara.android.utils.AppUtils
import io.github.yeobara.android.utils.NetworkUtils
import io.github.yeobara.android.utils.UiUtils
import kotlinx.android.synthetic.activity_main.coordLayout
import kotlinx.android.synthetic.activity_main.progress
import kotlinx.android.synthetic.activity_main.toolbar
import kotlinx.android.synthetic.content_main.recyclerView

class MainActivity : AppCompatActivity(), UpdateListener {

    private val adapter: MeetupAdapter by lazy {
        MeetupAdapter(this, this)
    }

    private val userRef: Firebase by lazy {
        Firebase("${Const.FB_BASE}/users")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        checkPermission()
        initMeetups()
    }

    override fun onStart() {
        super.onStart()
        initUser()
        adapter.startEddyStone()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopEddyStone()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.clear()
    }

    private fun initUser() {
        val id = AppUtils.getFingerprint()
        userRef.child(id).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot?) {
                if (data != null) {
                    val value = data.value
                    if (value == null) {
                        adapter.setUser(null)
                        try {
                            showSignUpDialog(id)
                        } catch (e: Exception) {
                        }
                    } else {
                        val user = data.getValue(User::class.java)
                        adapter.setUser(user)
                    }
                }
            }

            override fun onCancelled(error: FirebaseError?) {
                adapter.setUser(null)
            }
        })
    }

    private fun showSignUpDialog(id: String) {
        val view = layoutInflater.inflate(R.layout.dialog_signup, null)
        val nicknameView = view.findViewById(R.id.nickname) as EditText
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_signup)
                .setIcon(R.mipmap.ic_launcher)
                .setView(view)
                .setPositiveButton(android.R.string.ok, { dialog, which ->
                    nicknameView.text?.toString()?.let {
                        if (it.isNotEmpty()) {
                            val attendee = User(id, it)
                            userRef.child(id).setValue(attendee)
                        }
                    }
                })
                .show()
    }

    private fun initMeetups() {
        val span = if (UiUtils.isLandscape(this)) 2 else 1
        val layoutManager = StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        progress.visibility = if (NetworkUtils.isNetworkConnected(this)) {
            View.VISIBLE
        } else {
            View.GONE
        }
        recyclerView.adapter = adapter
    }

    override fun onAdded() {
        progress.visibility = View.GONE
    }

    private fun checkPermission() {
        RxPermissions.getInstance(this)
                .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe({ granted ->
                    if (granted) {
                        adapter.startEddyStone()
                    } else {
                        val message = R.string.error_permission_not_granted
                        showSnackbar(message)
                    }
                }, { error ->
                    showSnackbar(error.message ?: "error")
                })
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(coordLayout, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSnackbar(@StringRes message: Int) {
        Snackbar.make(coordLayout, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (EddyStone.REQUEST_ENABLE_BLUETOOTH == requestCode &&
                Activity.RESULT_OK == resultCode) {
            adapter.startEddyStone()
        }
    }
}
