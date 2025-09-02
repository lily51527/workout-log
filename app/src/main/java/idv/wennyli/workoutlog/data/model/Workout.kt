package idv.wennyli.workoutlog.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// 代表單筆訓練紀錄的資料類別
data class Workout(
    val id: String = "",
    val date: String = "",
    val exercise: String = "",
    val muscleGroup: String = "",
    val weight: Double = 0.0,
    val sets: Int = 0,
    val reps: Int = 0,
    val repsUnit: String = "次數",
    val muscleFeel: Int = 3,
    val control: Int = 3,
    val notes: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val userId: String = ""
)

// 代表訓練動作與其對應肌群的資料類別
data class Exercise(
    val name: String,
    val muscleGroup: String
)

