package io.github.yeobara.android.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

public object StringUtils {

    public fun getDate(epoch: Long): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd EEEE", Locale.getDefault())
        return dateFormat.format(Date(epoch))
    }

    public fun createdAt(created: Long): String {
        return DateUtils.getRelativeTimeSpanString(
                created, System.currentTimeMillis(), 0,
                DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    }
}

