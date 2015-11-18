package io.github.yeobara.android.meetup

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.firebase.client.*
import com.squareup.picasso.Picasso
import io.github.importre.eddystone.Beacon
import io.github.importre.eddystone.EddyStoneCallback
import io.github.yeobara.android.R
import io.github.yeobara.android.app.Const
import io.github.yeobara.android.utils.ImageUtils
import io.github.yeobara.android.utils.StringUtils
import io.github.yeobara.android.utils.UiUtils
import java.util.*

public class MeetupAdapter(val activity: Activity,
                           val listener: UpdateListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), EddyStoneCallback {

    public val keys: ArrayList<String> = arrayListOf()
    public val meetups: ArrayList<Meetup> = arrayListOf()

    private var hasNearest: Boolean = false
    private var user: User? = null
    private var eventListener: ChildEventListener
    private val childEventListener: ChildEventListener

    private val meetupsRef: Firebase by lazy {
        Firebase("${Const.FB_BASE}/meetups")
    }

    private val query: Query by lazy {
        meetupsRef.orderByChild("created")
    }

    init {
        childEventListener = initChildEventListener()
        eventListener = query.addChildEventListener(childEventListener)
    }

    public fun clear() {
        keys.clear()
        meetups.clear()
        query.removeEventListener(eventListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is MeetupHolder) {
            val key = keys[position]
            val meetup = meetups[position]
            holder.setItem(key, meetup)
        }
    }

    override fun getItemCount(): Int = meetups.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        if (parent == null) {
            return null
        }

        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.meetup_card, parent, false)
        view.setTag(R.id.card_toolbar, view.findViewById(R.id.card_toolbar))
        view.setTag(R.id.attendees, view.findViewById(R.id.attendees))
        view.setTag(R.id.attendees_count, view.findViewById(R.id.attendees_count))
        view.setTag(R.id.date, view.findViewById(R.id.date))
        view.setTag(R.id.location, view.findViewById(R.id.location))
        view.setTag(R.id.description, view.findViewById(R.id.description))
        view.setTag(R.id.rvsp, view.findViewById(R.id.rvsp))
        view.setTag(R.id.checkin, view.findViewById(R.id.checkin))
        view.setTag(R.id.map_frame, view.findViewById(R.id.map_frame))
        view.setTag(R.id.map, view.findViewById(R.id.map))
        return MeetupHolder(view)
    }

    private fun initChildEventListener(): ChildEventListener {
        return object : ChildEventListener {
            override fun onChildRemoved(snapshot: DataSnapshot?) {
                if (snapshot == null) return
                val key = snapshot.key
                val index = keys.indexOf(key)
                listener.onUpdate()

                if (index >= 0 && meetups.size > index) {
                    keys.removeAt(index)
                    meetups.removeAt(index)
                    notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot?, prevKey: String?) {
                if (snapshot == null) return
                val key = snapshot.key
                val meetup = snapshot.getValue(Meetup::class.java)
                val index = keys.indexOf(key)
                listener.onUpdate()

                if (index >= 0 && meetups.size > index) {
                    addAttendees(meetup, snapshot)
                    meetups.set(index, meetup)
                    notifyItemChanged(index)
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot?, prevKey: String?) {
                if (snapshot == null) return
                val key = snapshot.key
                val meetup = snapshot.getValue(Meetup::class.java)
                val index = keys.indexOf(key)
                listener.onUpdate()

                if (index >= 0 && meetups.size > index) {
                    addAttendees(meetup, snapshot)
                    keys.removeAt(index)
                    meetups.removeAt(index)
                    val newIndex = if (prevKey == null) 0 else index + 1
                    keys.add(newIndex, key)
                    meetups.add(newIndex, meetup)
                    notifyDataSetChanged()
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot?, prevKey: String?) {
                if (snapshot == null) return
                val key = snapshot.key
                val meetup = snapshot.getValue(Meetup::class.java)

                addAttendees(meetup, snapshot)
                keys.add(key)
                meetups.add(meetup)
                notifyDataSetChanged()
                listener.onUpdate()
            }

            override fun onCancelled(snapshot: FirebaseError?) {
                listener.onUpdate()
            }

            private fun addAttendees(meetup: Meetup, snapshot: DataSnapshot) {
                snapshot.child("attendees").children.forEach {
                    meetup.attendees.add(it.getValue(Attendee::class.java))
                }
            }
        }
    }

    fun setUser(user: User?) {
        this.user = user
        notifyDataSetChanged()
    }

    override fun onSuccess(beacons: ArrayList<Beacon>) {
        hasNearest = false
        meetups.forEachIndexed { i, meetup ->
            val nearest = containsHashcode(beacons, meetup.hashcode)
            if (meetup.nearest != nearest) {
                meetup.nearest = nearest
                notifyItemChanged(i)
                hasNearest = true
            }
        }
    }

    private fun containsHashcode(beacons: ArrayList<Beacon>, hashcode: String): Boolean {
        for (beacon in beacons) {
            val url = beacon.urlStatus.url()
            if (url.endsWith(hashcode)) {
                return true
            }
        }
        return false
    }

    override fun onFailure(message: String, deviceAddress: String?) {
    }

    inner class MeetupHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun setItem(key: String, meetup: Meetup) {
            initToolbar(meetup)
            initContents(meetup)
            initCheckBoxButtons(key, meetup)
            initAttendeesButton(meetup)
            initMap(meetup)
        }

        private fun initMap(meetup: Meetup) {
            val w = UiUtils.getDisplayWidth(activity)
            val h = activity.resources.getDimension(R.dimen.list_item_height_xlarge).toInt()
            val lat = meetup.latLng.lat
            val lng = meetup.latLng.lng
            val zoom = 15
            val url = ImageUtils.getGoogleMapUrl(w, h, lat, lng, zoom)
            val iv = view.getTag(R.id.map) as ImageView
            Picasso.with(activity).load(url).into(iv)

            val v = view.getTag(R.id.map_frame) as View
            v.setOnClickListener {
                showGoogleMap(lat, lng, zoom, meetup)
            }
        }

        private fun initAttendeesButton(meetup: Meetup) {
            val attendeeCount = view.getTag(R.id.attendees_count) as TextView
            attendeeCount.text = meetup.attendees.size.toString()

            val attendees = view.getTag(R.id.attendees) as View
            attendees.setOnClickListener {
                val attendeeAdapter = AttendeeAdapter(activity, meetup.attendees)
                AlertDialog.Builder(activity)
                        .setTitle(R.string.dialog_title_attendees)
                        .setAdapter(attendeeAdapter, null)
                        .show()
            }
        }

        private fun initCheckBoxButtons(key: String, meetup: Meetup) {
            val rvsp = view.getTag(R.id.rvsp) as CheckBox
            rvsp.isEnabled = user != null
            setCheckListener(key, rvsp, Const.RVSP)

            val checkin = view.getTag(R.id.checkin) as CheckBox
            setCheckListener(key, checkin, Const.CHECKIN)

            val uid = meetupsRef.auth.uid ?: return
            meetupsRef.child("$key/attendees/$uid")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(data: DataSnapshot?) {
                            rvsp.isChecked = data?.value != null
                            rvsp.visibility = if (meetup.nearest && rvsp.isChecked) {
                                View.GONE
                            } else {
                                View.VISIBLE
                            }

                            data?.getValue(Attendee::class.java)?.let { attendee ->
                                when (attendee.status) {
                                    Const.CHECKED -> {
                                        checkin.isChecked = true
                                        checkin.isEnabled = false
                                        checkin.setText(R.string.checked)
                                    }
                                    Const.CHECKIN -> {
                                        checkin.isChecked = true
                                        checkin.isEnabled = true
                                        checkin.setText(R.string.checkin)
                                    }
                                    else -> {
                                    }
                                }
                            }

                            checkin.visibility = if (meetup.nearest && rvsp.isChecked) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                        }

                        override fun onCancelled(error: FirebaseError?) {
                        }
                    })
        }

        private fun initContents(meetup: Meetup) {
            val dateView = view.getTag(R.id.date) as TextView
            dateView.text = meetup.date

            val location = view.getTag(R.id.location) as TextView
            location.text = meetup.formattedAddress
            if (TextUtils.isEmpty(location.text)) {
                location.text = meetup.latLng.toString()
            }

            val descriptionView = view.getTag(R.id.description) as TextView
            descriptionView.text = meetup.description
        }

        private fun initToolbar(meetup: Meetup) {
            val toolbar = view.getTag(R.id.card_toolbar) as Toolbar
            toolbar.title = meetup.friendlyName
            toolbar.subtitle = "${meetup.host} · ${StringUtils.createdAt(meetup.created)}"
            toolbar.setNavigationIcon(if (meetup.nearest) {
                R.drawable.oval_location_on
            } else {
                R.drawable.oval_location_off
            })

            toolbar.setNavigationOnClickListener {
                if (meetup.nearest) {
                    Toast.makeText(activity, R.string.toast_nearby, Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun setCheckListener(key: String, cb: CheckBox, currentStatus: String) {
            cb.setOnCheckedChangeListener { button, checked ->
                if (button.isPressed) {
                    val status = if (checked) currentStatus
                    else if (currentStatus.equals(Const.CHECKIN)) Const.RVSP
                    else null

                    meetupsRef.auth.uid?.let { id ->
                        val attendee = if (status != null) Attendee(id, status) else null
                        meetupsRef.child("$key/attendees/$id").setValue(attendee)
                    }
                }
            }
        }
    }

    private fun showGoogleMap(lat: Float, lng: Float, zoom: Int, meetup: Meetup) {
        val label = meetup.friendlyName
        val uriBegin = "geo:$lat,$lng"
        val query = "$lat,$lng($label)"
        val encodedQuery = Uri.encode(query)
        val uriString = "$uriBegin?q=$encodedQuery&z=$zoom"
        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        activity.startActivity(intent)
    }
}

