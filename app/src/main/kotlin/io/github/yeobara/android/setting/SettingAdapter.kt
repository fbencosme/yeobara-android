package io.github.yeobara.android.setting

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.github.yeobara.android.BuildConfig
import io.github.yeobara.android.R
import io.github.yeobara.android.SignInActivity
import io.github.yeobara.android.utils.PrefUtils

public class SettingAdapter(val activity: Activity) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val LAYOUT = R.layout.layout_two_line
    }

    private val logout: ((View) -> Unit) = {
        PrefUtils.clearAll(activity)
        val intent = Intent(activity, SignInActivity::class.java)
        activity.startActivity(intent)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    private val items = arrayListOf(
            Triple(R.drawable.ic_android, BuildConfig.VERSION_NAME, null),
            Triple(R.drawable.ic_lock_open, activity.getString(R.string.setting_logout), logout)
    )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is SettingHolder) {
            val item = items[position]
            holder.setItem(item)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        if (parent == null) {
            return null
        }

        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(LAYOUT, parent, false)
        view.setTag(R.id.icon, view.findViewById(R.id.icon))
        view.setTag(R.id.title, view.findViewById(R.id.title))
        view.findViewById(R.id.subtitle).visibility = View.GONE
        return SettingHolder(view)
    }

    inner class SettingHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun setItem(item: Triple<Int, String, ((View) -> Unit)?>) {
            val icon = view.getTag(R.id.icon) as ImageView
            val title = view.getTag(R.id.title) as TextView

            icon.setImageResource(item.first)
            title.text = item.second
            view.setOnClickListener {
                item.third?.invoke(it)
            }
        }
    }
}

