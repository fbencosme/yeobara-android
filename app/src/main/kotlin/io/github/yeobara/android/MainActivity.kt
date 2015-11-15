package io.github.yeobara.android

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.EditText
import com.firebase.client.DataSnapshot
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.firebase.client.ValueEventListener
import io.github.yeobara.android.meetup.Attendee
import io.github.yeobara.android.meetup.MeetupAdapter
import io.github.yeobara.android.meetup.UpdateListener
import io.github.yeobara.android.utils.AppUtils
import io.github.yeobara.android.utils.NetworkUtils
import io.github.yeobara.android.utils.UiUtils
import kotlinx.android.synthetic.activity_main.progress
import kotlinx.android.synthetic.activity_main.toolbar
import kotlinx.android.synthetic.content_main.recyclerView

class MainActivity : AppCompatActivity(), UpdateListener {

    private val adapter: MeetupAdapter by lazy {
        MeetupAdapter(this, this)
    }

    private val userRef: Firebase by lazy {
        Firebase("https://yeobara.firebaseio.com/users")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initMeetups()
    }

    override fun onStart() {
        super.onStart()
        initUser()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.clear()
    }

    private fun initUser() {
        val id = AppUtils.getFingerprint()
        userRef.child(id).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                if (p0 != null) {
                    val value = p0.value
                    if (value == null) {
                        adapter.setUser(null)
                        try {
                            showSignUpDialog(id)
                        } catch (e: Exception) {
                        }
                    } else {
                        val user = p0.getValue(Attendee::class.java)
                        adapter.setUser(user)
                    }
                }
            }

            override fun onCancelled(p0: FirebaseError?) {
                adapter.setUser(null)
            }
        })
    }

    private fun showSignUpDialog(id: String) {
        val view = layoutInflater.inflate(R.layout.dialog_signup, null)
        val nicknameView = view.findViewById(R.id.nickname) as EditText
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_signup)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, { dialog, which ->
                })
                .setPositiveButton(android.R.string.ok, { dialog, which ->
                    nicknameView.text?.toString()?.let {
                        if (it.isNotEmpty()) {
                            val attendee = Attendee(id, System.currentTimeMillis(), it, "")
                            val map = hashMapOf(Pair(id, attendee))
                            userRef.setValue(map)
                        }
                    }
                })
                .setCancelable(false)
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
}
