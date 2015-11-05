package io.github.yeobara.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.tbruyelle.rxpermissions.RxPermissions
import io.github.importre.eddystone.Beacon
import io.github.importre.eddystone.EddyStone
import io.github.importre.eddystone.EddyStoneCallback
import kotlinx.android.synthetic.activity_main.*
import kotlinx.android.synthetic.activity_main.toolbar
import kotlinx.android.synthetic.content_main.recyclerView
import java.util.*

class MainActivity : AppCompatActivity(), EddyStoneCallback {

    private val adapter: BeaconsAdapter by lazy {
        BeaconsAdapter(this, arrayListOf())
    }

    private var eddyStone: EddyStone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        checkPermission()
        initUi()
    }

    private fun checkPermission() {
        RxPermissions.getInstance(this)
                .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe({ granted ->
                    if (granted) {
                        startEddyStone()
                    } else {
                        val message = R.string.error_permission_not_granted
                        showSnackbar(message)
                    }
                }, { error ->
                    showSnackbar(error.message ?: "error")
                })
    }

    private fun startEddyStone() {
        adapter.clear()
        eddyStone?.stop()
        eddyStone = EddyStone(this, this)
        eddyStone?.start()
    }

    private fun initUi() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        fab.setOnClickListener { startEddyStone() }
    }

    override fun onStart() {
        super.onStart()
        eddyStone?.start()
    }

    override fun onStop() {
        super.onStop()
        eddyStone?.stop()
    }

    override fun onSuccess(beacons: ArrayList<Beacon>) {
        adapter.update(beacons)
    }

    override fun onFailure(message: String, deviceAddress: String?) {
        showSnackbar(message)
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
            startEddyStone()
        }
    }
}
