package io.github.yeobara.android.meetup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import io.github.yeobara.android.R
import java.util.*

public class AttendeeAdapter(context: Context, val attendees: ArrayList<Attendee>) :
        ArrayAdapter<Attendee>(context, AttendeeAdapter.LAYOUT, attendees) {

    companion object {
        val LAYOUT = R.layout.layout_two_line
    }

    override fun getCount(): Int = attendees.size

    override fun getItem(position: Int): Attendee {
        return attendees[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View
        val title: TextView
        val subtitle: TextView

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(LAYOUT, parent, false)
            title = view.findViewById(R.id.title) as TextView
            subtitle = view.findViewById(R.id.subtitle) as TextView
            view.setTag(R.id.title, title)
            view.setTag(R.id.subtitle, subtitle)
        } else {
            view = convertView
            title = view.findViewById(R.id.title) as TextView
            subtitle = view.findViewById(R.id.subtitle) as TextView
        }

        val item = getItem(position)
        title.text = item.nickname
        subtitle.text = item.status
        return view
    }
}