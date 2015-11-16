package io.github.yeobara.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import io.github.yeobara.android.setting.SettingAdapter
import kotlinx.android.synthetic.activity_settings.recyclerView
import kotlinx.android.synthetic.activity_settings.toolbar

public class SettingsActivity : AppCompatActivity() {

    private val adapter: SettingAdapter by lazy {
        SettingAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        initUi()
    }

    private fun initUi() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }
}