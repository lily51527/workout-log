package idv.wennyli.workoutlog.data.model

// 代表使用者固定資料的類別
data class UserProfile(
    val gender: String = "", // 儲存格式: "male", "female", "other"
    val birthDate: String = "" // 儲存格式: "yyyy-MM-dd"
)