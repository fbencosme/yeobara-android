package io.github.yeobara.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.firebase.client.Firebase
import io.github.yeobara.android.meetup.MeetupAdapter
import io.github.yeobara.android.utils.NetworkUtils
import io.github.yeobara.android.utils.UiUtils
import kotlinx.android.synthetic.activity_main.progress
import kotlinx.android.synthetic.activity_main.toolbar
import kotlinx.android.synthetic.content_main.recyclerView

class MainActivity : AppCompatActivity() {

    val fb: Firebase by lazy {
        Firebase("https://yeobara.firebaseio.com")
    }

    val meetups: Firebase by lazy {
        fb.child("meetups")
    }

    private val adapter: MeetupAdapter by lazy {
        MeetupAdapter(this, meetups.orderByChild("date"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.clear()
    }

    private fun initUi() {
        val span = if (UiUtils.isLandscape(this)) 2 else 1
        val layoutManager = StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnChildAttachStateChangeListener(
                object : RecyclerView.OnChildAttachStateChangeListener {
                    override fun onChildViewAttachedToWindow(view: View?) {
                        progress.visibility = View.GONE
                    }

                    override fun onChildViewDetachedFromWindow(view: View?) {
                    }
                })

        if (NetworkUtils.isNetworkConnected(this)) {
            progress.visibility = View.VISIBLE
        } else {
            progress.visibility = View.GONE
        }
        recyclerView.adapter = adapter
    }
}
