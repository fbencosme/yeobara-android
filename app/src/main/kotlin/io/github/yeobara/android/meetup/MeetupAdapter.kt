package io.github.yeobara.android.meetup

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.firebase.client.ChildEventListener
import com.firebase.client.DataSnapshot
import com.firebase.client.FirebaseError
import com.firebase.client.Query
import io.github.yeobara.android.R
import java.util.*

public class MeetupAdapter(val context: Context, val query: Query) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    public val keys: ArrayList<String> = arrayListOf()
    public val meetups: ArrayList<Meetup> = arrayListOf()
    private var eventListener: ChildEventListener

    init {
        eventListener = query.addChildEventListener(object : ChildEventListener {
            override fun onChildRemoved(snapshot: DataSnapshot?) {
                if (snapshot == null) return
                val key = snapshot.key
                val index = keys.indexOf(key)

                if (index >= 0) {
                    keys.removeAt(index)
                    meetups.removeAt(index)
                    notifyDataSetChanged()
                }
            }

            override fun onCancelled(snapshot: FirebaseError?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot?, prevKey: String?) {
                if (snapshot == null) return
                val key = snapshot.key
                val meetup = snapshot.getValue(Meetup::class.java)
                val index = keys.indexOf(key)

                addAttendees(meetup, snapshot)
                meetups.set(index, meetup)
                notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot?, prevKey: String?) {
                if (snapshot == null) return
                val key = snapshot.key
                val meetup = snapshot.getValue(Meetup::class.java)
                val index = keys.indexOf(key)

                addAttendees(meetup, snapshot)
                keys.removeAt(index)
                meetups.removeAt(index)
                notifyDataSetChanged()
            }

            override fun onChildAdded(snapshot: DataSnapshot?, prevKey: String?) {
                if (snapshot == null) return
                val key = snapshot.key
                val meetup = snapshot.getValue(Meetup::class.java)

                addAttendees(meetup, snapshot)
                update(key, meetup, prevKey)
            }

            private fun update(key: String, meetup: Meetup, prevKey: String?) {
                if (prevKey == null) {
                    keys.add(key)
                    meetups.add(meetup)
                    notifyDataSetChanged()
                } else {
                    val prevIndex = keys.indexOf(prevKey)
                    val nextIndex = prevIndex + 1
                    if (nextIndex == keys.size) {
                        keys.add(key)
                        meetups.add(meetup)
                    } else {
                        keys.add(nextIndex, key)
                        meetups.add(nextIndex, meetup)
                    }
                    notifyDataSetChanged()
                }
            }

            private fun addAttendees(meetup: Meetup, snapshot: DataSnapshot) {
                snapshot.child("attendees").children.forEach {
                    meetup.attendees.add(it.getValue(Attendee::class.java))
                }
            }
        })
    }

    public fun clear() {
        keys.clear()
        meetups.clear()
        query.removeEventListener(eventListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is MeetupHolder) {
            val meetup = meetups[position]
            holder.setItem(meetup)
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
        return MeetupHolder(view)
    }

    inner class MeetupHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun setItem(meetup: Meetup) {
            val toolbar = view.getTag(R.id.card_toolbar) as Toolbar
            toolbar.title = meetup.friendlyName
            toolbar.subtitle = meetup.description

            (view.getTag(R.id.attendees_count) as TextView).text = meetup.attendees.size.toString()
            (view.getTag(R.id.attendees) as View).setOnClickListener {
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
    }
}

