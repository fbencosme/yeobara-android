package io.github.yeobara.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import io.github.importre.eddystone.Beacon
import java.util.*

public class BeaconsAdapter(val context: Context, val beacons: ArrayList<Beacon>) :
        RecyclerView.Adapter<BeaconsAdapter.BeaconHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BeaconHolder? {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.beacon_item, parent, false)
        view.setTag(R.id.url, view.findViewById(R.id.url))
        return BeaconHolder(view)
    }

    override fun onBindViewHolder(holder: BeaconHolder?, position: Int) {
        beacons.getOrNull(position)?.let {
            holder?.update(it)
        }
    }

    override fun getItemCount(): Int = beacons.size

    public fun update(beacons: ArrayList<Beacon>) {
        this.beacons.clear()
        this.beacons.addAll(beacons)
        notifyDataSetChanged()
    }

    public fun clear() {
        beacons.clear()
        notifyDataSetChanged()
    }

    inner class BeaconHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun update(beacon: Beacon) {
            val urlView = view.getTag(R.id.url) as TextView
            val url = beacon.urlStatus.url()
            urlView.text = "${beacon.rssi}, $url"

            view.setOnClickListener {
                openUrl(url)
            }
        }

        private fun openUrl(url: String) {
            try {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(uri)
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}

