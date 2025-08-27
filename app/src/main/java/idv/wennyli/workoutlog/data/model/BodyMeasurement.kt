package idv.wennyli.workoutlog.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class BodyMeasurement(
    val id: String = "",
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val bodyFat: Double? = null, // 新增：體脂肪率 (可選)
    @ServerTimestamp
    val timestamp: Date? = null,
    val userId: String = ""
)
