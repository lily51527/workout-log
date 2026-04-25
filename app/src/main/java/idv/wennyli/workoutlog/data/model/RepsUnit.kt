package idv.wennyli.workoutlog.data.model

enum class RepsUnit(val displayName: String) {
    COUNT("次數"),
    SECONDS("秒數");

    companion object {
        // 從 Firestore 讀回的字串（或 null）還原成 enum，預設為 COUNT
        fun fromDisplayName(name: String?): RepsUnit =
            entries.find { it.displayName == name } ?: COUNT
    }
}
