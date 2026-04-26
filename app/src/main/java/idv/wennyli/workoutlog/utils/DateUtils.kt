package idv.wennyli.workoutlog.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toFormattedDate(): String {
    return this.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it)
    }
}