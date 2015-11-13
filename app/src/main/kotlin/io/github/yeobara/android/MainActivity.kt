package io.github.yeobara.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import com.firebase.client.Firebase
import io.github.yeobara.android.meetup.MeetupAdapter
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
        MeetupAdapter(this, meetups.limitToLast(100))
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
        recyclerView.adapter = adapter
    }
}
