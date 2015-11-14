package io.github.yeobara.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import io.github.yeobara.android.meetup.MeetupAdapter
import io.github.yeobara.android.meetup.UpdateListener
import io.github.yeobara.android.utils.NetworkUtils
import io.github.yeobara.android.utils.UiUtils
import kotlinx.android.synthetic.activity_main.progress
import kotlinx.android.synthetic.activity_main.toolbar
import kotlinx.android.synthetic.content_main.recyclerView

class MainActivity : AppCompatActivity(), UpdateListener {

    private val adapter: MeetupAdapter by lazy {
        MeetupAdapter(this, this)
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
