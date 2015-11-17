package io.github.yeobara.android.meetup

import android.app.Activity
import android.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.firebase.client.*
import io.github.importre.eddystone.Beacon
import io.github.importre.eddystone.EddyStoneCallback
import io.github.yeobara.android.R
import io.github.yeobara.android.app.Const
import io.github.yeobara.android.utils.StringUtils
import java.util.*

public class MeetupAdapter(val activity: Activity,
                           val listener: UpdateListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), EddyStoneCallback {

    public val keys: ArrayList<String> = arrayListOf()
    public val meetups: ArrayList<Meetup> = arrayListOf()

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
        view.setTag(R.id.description, view.findViewById(R.id.description))
        view.setTag(R.id.rvsp, view.findViewById(R.id.rvsp))
        view.setTag(R.id.checkin, view.findViewById(R.id.checkin))
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
        meetups.forEachIndexed { i, meetup ->
            val nearest = containsHashcode(beacons, meetup.hashcode)
            if (meetup.nearest != nearest) {
                meetup.nearest = nearest
                notifyItemChanged(i)
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
            setCheckListener(key, checkin, Const.CHECK_IN)

            val uid = meetupsRef.auth.uid ?: return
            meetupsRef.child("$key/attendees/$uid")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(data: DataSnapshot?) {
                            rvsp.isChecked = data?.value != null
                            data?.getValue(Attendee::class.java)?.let { attendee ->
                                checkin.isChecked = Const.CHECK_IN.equals(attendee.status)
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

            val descriptionView = view.getTag(R.id.description) as TextView
            descriptionView.text = meetup.description
        }

        private fun initToolbar(meetup: Meetup) {
            val toolbar = view.getTag(R.id.card_toolbar) as Toolbar
            if (meetup.nearest) {
                toolbar.setBackgroundResource(R.color.colorAccent)
                toolbar.setTitleTextAppearance(activity, R.style.ActionBar_TitleText)
                toolbar.setSubtitleTextAppearance(activity, R.style.ActionBar_SubTitleText)
            } else {
                toolbar.background = null
                toolbar.setTitleTextAppearance(activity, R.style.ActionBar_TitleText_Inverse)
                toolbar.setSubtitleTextAppearance(activity, R.style.ActionBar_SubTitleText_Inverse)
            }
            toolbar.title = meetup.friendlyName
            toolbar.subtitle = "${meetup.host} · ${StringUtils.createdAt(meetup.created)}"
        }

        private fun setCheckListener(key: String, cb: CheckBox, currentStatus: String) {
            cb.setOnCheckedChangeListener { button, checked ->
                if (button.isPressed) {
                    val status = if (checked) currentStatus
                    else if (currentStatus.equals(Const.CHECK_IN)) Const.RVSP
                    else null

                    meetupsRef.auth.uid?.let { id ->
                        val attendee = if (status != null) Attendee(id, status) else null
                        meetupsRef.child("$key/attendees/$id").setValue(attendee)
                    }
                }
            }
        }
    }
}

