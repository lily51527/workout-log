package idv.wennyli.workoutlog.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toFormattedDate(): String {
    return this.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it)
    }
}

fun Long.toRelativeTimeString(): String {
    val diffMs = System.currentTimeMillis() - this
    val diffMinutes = diffMs / 1000 / 60
    val diffHours = diffMinutes / 60
    return when {
        diffMinutes < 1 -> "剛剛"
        diffMinutes < 60 -> "${diffMinutes} 分鐘前"
        else -> "${diffHours} 小時前"
    }
}