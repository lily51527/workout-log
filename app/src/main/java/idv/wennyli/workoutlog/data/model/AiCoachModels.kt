package idv.wennyli.workoutlog.data.model

data class RecommendedExercise(
    val exercise: String = "",
    val muscleGroup: String = "",
    val intensitySuggestion: String = ""
)

data class AiCoachFeedback(
    val summary: String = "",
    val reasoning: String = "",
    val recommendedExercises: List<RecommendedExercise> = emptyList(),
    val warnings: List<String> = emptyList(),
    val generatedAt: Long = 0L
)