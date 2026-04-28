package idv.wennyli.workoutlog.data.repository

import com.google.firebase.functions.FirebaseFunctions
import idv.wennyli.workoutlog.data.model.AiCoachFeedback
import idv.wennyli.workoutlog.data.model.RecommendedExercise
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

interface AiCoachRepository {
    suspend fun getFeedback(): AiCoachFeedback
}

class AiCoachRepositoryImpl @Inject constructor(
    private val functions: FirebaseFunctions,
    @Named("appId") private val appId: String
) : AiCoachRepository {

    override suspend fun getFeedback(): AiCoachFeedback {
        val data = mapOf("appId" to appId)

        val result = functions
            .getHttpsCallable("getAiCoachFeedback")
            .call(data)
            .await()

        @Suppress("UNCHECKED_CAST")
        val map = result.data as Map<String, Any>

        @Suppress("UNCHECKED_CAST")
        val exercises = (map["recommendedExercises"] as? List<Map<String, Any>>)
            ?.map { ex ->
                RecommendedExercise(
                    exercise = ex["exercise"] as? String ?: "",
                    muscleGroup = ex["muscleGroup"] as? String ?: "",
                    intensitySuggestion = ex["intensitySuggestion"] as? String ?: ""
                )
            } ?: emptyList()

        @Suppress("UNCHECKED_CAST")
        val warnings = (map["warnings"] as? List<String>) ?: emptyList()

        return AiCoachFeedback(
            summary = map["summary"] as? String ?: "",
            reasoning = map["reasoning"] as? String ?: "",
            recommendedExercises = exercises,
            warnings = warnings,
            generatedAt = (map["generatedAt"] as? Number)?.toLong() ?: 0L
        )
    }
}
