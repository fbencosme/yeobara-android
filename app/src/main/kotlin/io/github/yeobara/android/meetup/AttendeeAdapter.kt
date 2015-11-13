package io.github.yeobara.android.meetup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.util.*

public class AttendeeAdapter(context: Context, val attendees: ArrayList<Attendee>) :
        ArrayAdapter<Attendee>(context, AttendeeAdapter.LAYOUT, attendees) {

    companion object {
        val LAYOUT = android.R.layout.simple_list_item_1
    }

    override fun getCount(): Int = attendees.size

    override fun getItem(position: Int): Attendee {
        return attendees[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view = if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            inflater.inflate(LAYOUT, parent, false) as TextView
        } else {
            convertView as TextView
        }

        view.text = getItem(position).nickname
        return view
    }
}