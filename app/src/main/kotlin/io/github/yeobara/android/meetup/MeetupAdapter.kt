package io.github.yeobara.android.meetup

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.firebase.client.*
import io.github.yeobara.android.R
import io.github.yeobara.android.utils.AppUtils
import io.github.yeobara.android.utils.StringUtils
import java.util.*

public class MeetupAdapter(val context: Context, val listener: UpdateListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    public val keys: ArrayList<String> = arrayListOf()
    public val meetups: ArrayList<Meetup> = arrayListOf()

    private var user: Attendee? = null
    private var eventListener: ChildEventListener
    private val childEventListener: ChildEventListener

    private val meetupsRef: Firebase by lazy {
        Firebase("https://yeobara.firebaseio.com/meetups")
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

    inner class MeetupHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun setItem(key: String, meetup: Meetup) {
            val toolbar = view.getTag(R.id.card_toolbar) as Toolbar
            toolbar.title = meetup.friendlyName
            toolbar.subtitle = "${meetup.host} · ${StringUtils.createdAt(meetup.created)}"

            val dateView = view.getTag(R.id.date) as TextView
            dateView.text = meetup.date

            val descriptionView = view.getTag(R.id.description) as TextView
            descriptionView.text = meetup.description

            val rvsp = view.getTag(R.id.rvsp) as CheckBox
            rvsp.isEnabled = user != null
            setRvspCheckListener(key, rvsp)
            meetupsRef.child("$key/attendees/${AppUtils.getFingerprint()}")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot?) {
                            rvsp.isChecked = p0?.exists() ?: false
                        }

                        override fun onCancelled(p0: FirebaseError?) {
                        }
                    })

            val attendeeCount = view.getTag(R.id.attendees_count) as TextView
            attendeeCount.text = meetup.attendees.size.toString()

            val attendees = view.getTag(R.id.attendees) as View
            attendees.setOnClickListener {
                val attendeeAdapter = AttendeeAdapter(context, meetup.attendees)
                AlertDialog.Builder(context)
                        .setTitle(R.string.dialog_title_attendees)
                        .setAdapter(attendeeAdapter, object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                val nickname = meetup.attendees[which].nickname
                                Toast.makeText(context, nickname, Toast.LENGTH_SHORT).show()
                            }
                        })
                        .show()
            }
        }

        private fun setRvspCheckListener(key: String, rvsp: CheckBox) {
            rvsp.setOnCheckedChangeListener { button, checked ->
                user?.let {
                    it.status = if (checked) "rvsp" else ""
                    val id = AppUtils.getFingerprint()
                    val map = hashMapOf(Pair(id, if (checked) it else null))
                    meetupsRef.child("$key/attendees").setValue(map)
                }
            }
        }
    }

    private fun initChildEventListener(): ChildEventListener {
        return object : ChildEventListener {
            override fun onChildRemoved(snapshot: DataSnapshot?) {
                if (snapshot == null) return
                val key = snapshot.key
                val index = keys.indexOf(key)

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
                listener.onAdded()
            }

            override fun onCancelled(snapshot: FirebaseError?) {
            }

            private fun addAttendees(meetup: Meetup, snapshot: DataSnapshot) {
                snapshot.child("attendees").children.forEach {
                    meetup.attendees.add(it.getValue(Attendee::class.java))
                }
            }
        }
    }

    fun setUser(user: Attendee?) {
        this.user = user
        notifyDataSetChanged()
    }
}

