package idv.wennyli.workoutlog.utils

/**
 * 集中管理所有 Firestore 集合與文件路徑。
 * 未來若路徑結構變動，只需修改此檔案。
 */
object FirestorePaths {
    fun workouts(appId: String, userId: String) =
        "artifacts/$appId/users/$userId/workouts"

    fun measurements(appId: String, userId: String) =
        "artifacts/$appId/users/$userId/measurements"

    fun userProfileDoc(appId: String, userId: String) =
        "artifacts/$appId/users/$userId/profile/data"
}
