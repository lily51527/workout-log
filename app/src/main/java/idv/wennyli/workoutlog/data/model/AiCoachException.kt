package idv.wennyli.workoutlog.data.model

sealed class AiCoachException : Exception() {
    object QuotaExceeded : AiCoachException()
    object Unauthenticated : AiCoachException()
    object Unavailable : AiCoachException()
    object Timeout : AiCoachException()
    object Internal : AiCoachException()
    data class Unknown(override val cause: Throwable? = null) : AiCoachException()
}
