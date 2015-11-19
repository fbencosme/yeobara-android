package io.github.yeobara.android.meetup

import android.content.Context
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.github.yeobara.android.R
import java.util.*

public class AttendeeAdapter(context: Context, val attendees: ArrayList<User>) :
        ArrayAdapter<User>(context, AttendeeAdapter.LAYOUT, attendees) {

    companion object {
        val LAYOUT = R.layout.layout_two_line
    }

    override fun getCount(): Int = attendees.size

    override fun getItem(position: Int): User {
        return attendees[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View
        val icon: ImageView
        val title: TextView
        val subtitle: TextView

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(LAYOUT, parent, false)
            view.background = null

            icon = view.findViewById(R.id.icon) as ImageView
            title = view.findViewById(R.id.title) as TextView
            subtitle = view.findViewById(R.id.subtitle) as TextView
            view.setTag(R.id.icon, icon)
            view.setTag(R.id.title, title)
            view.setTag(R.id.subtitle, subtitle)

            icon.clipToOutline = true
            icon.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    if (view != null && outline != null) {
                        val size = view.resources.getDimension(R.dimen.avatar_icon_size).toInt()
                        outline.setOval(0, 0, size, size)
                    }
                }
            }
        } else {
            view = convertView
            icon = view.getTag(R.id.icon) as ImageView
            title = view.getTag(R.id.title) as TextView
            subtitle = view.getTag(R.id.subtitle) as TextView
        }

        icon.setImageResource(R.mipmap.ic_launcher)
        val user = getItem(position)
        user.profileImageURL?.let { Picasso.with(context).load(it).into(icon) }
        title.text = user.nickname
        subtitle.text = user.status
        return view
    }
}