package idv.wennyli.workoutlog.data.model

// 代表使用者固定資料的類別
data class UserProfile(
    val gender: String = "", // 例如: "男性", "女性", "不指定"
    val birthDate: String = "" // 新增：出生年月日 (YYYY-MM-DD)
)