package idv.wennyli.workoutlog.data.repository

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import idv.wennyli.workoutlog.data.model.AiCoachException
import kotlinx.coroutines.tasks.await

interface AiCoachDataSource {
    suspend fun fetchFeedback(appId: String): Map<String, Any>
}

class FirebaseAiCoachDataSource(
    private val functions: FirebaseFunctions
) : AiCoachDataSource {

    override suspend fun fetchFeedback(appId: String): Map<String, Any> {
        try {
            val result = functions
                .getHttpsCallable("getAiCoachFeedback")
                .call(mapOf("appId" to appId))
                .await()
            @Suppress("UNCHECKED_CAST")
            return result.data as Map<String, Any>
        } catch (e: FirebaseFunctionsException) {
            throw when (e.code) {
                FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> AiCoachException.QuotaExceeded
                FirebaseFunctionsException.Code.UNAUTHENTICATED -> AiCoachException.Unauthenticated
                FirebaseFunctionsException.Code.UNAVAILABLE -> AiCoachException.Unavailable
                FirebaseFunctionsException.Code.DEADLINE_EXCEEDED -> AiCoachException.Timeout
                FirebaseFunctionsException.Code.INTERNAL -> AiCoachException.Internal
                else -> AiCoachException.Unknown(e)
            }
        } catch (e: Exception) {
            throw AiCoachException.Unknown(e)
        }
    }
}
