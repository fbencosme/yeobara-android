package io.github.yeobara.android

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import io.github.importre.eddystone.Beacon
import io.github.importre.eddystone.EddyStone
import io.github.importre.eddystone.EddyStoneCallback
import kotlinx.android.synthetic.activity_main.coordLayout
import kotlinx.android.synthetic.activity_main.toolbar
import kotlinx.android.synthetic.content_main.recyclerView
import java.util.*

class MainActivity : AppCompatActivity(), EddyStoneCallback {

    private val adapter: BeaconsAdapter by lazy {
        BeaconsAdapter(this, arrayListOf())
    }

    private val eddyStone: EddyStone by lazy {
        EddyStone(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        eddyStone.start()
    }

    override fun onStop() {
        super.onStop()
        eddyStone.stop()
    }

    override fun onSuccess(beacons: ArrayList<Beacon>) {
        adapter.update(beacons)
    }

    override fun onFailure(message: String, deviceAddress: String?) {
        Snackbar.make(coordLayout, message, Snackbar.LENGTH_LONG).show()
    }
}
