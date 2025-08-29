package idv.wennyli.workoutlog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import idv.wennyli.workoutlog.data.model.BodyMeasurement
import idv.wennyli.workoutlog.data.model.UserProfile
import idv.wennyli.workoutlog.data.model.Workout
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

// Repository 介面，定義資料操作的合約
interface WorkoutRepository {
    // 訓練記錄相關
    fun getWorkouts(): Flow<List<Workout>>
    suspend fun addWorkout(workout: Workout)
    suspend fun deleteWorkout(workoutId: String)
}

// Repository 的實作，處理 Firebase Firestore 的資料互動
class WorkoutRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @Named("appId") private val appId: String
) : WorkoutRepository {

    private val userId: String?
        get() = auth.currentUser?.uid

    override fun getWorkouts(): Flow<List<Workout>> = callbackFlow {
        val collectionPath = userId?.let { "artifacts/$appId/users/$it/workouts" } ?: run {
            trySend(emptyList())
            awaitClose()
            return@callbackFlow
        }
        val listener = firestore.collection(collectionPath)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val workouts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Workout::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(workouts)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addWorkout(workout: Workout) {
        userId?.let {
            val collectionPath = "artifacts/$appId/users/$it/workouts"
            val newWorkoutRef = firestore.collection(collectionPath).document()
            val finalMeasurement = workout.copy(id = newWorkoutRef.id, userId = it)
            newWorkoutRef.set(finalMeasurement).await()
        }
    }

    override suspend fun deleteWorkout(workoutId: String) {
        userId?.let {
            val collectionPath = "artifacts/$appId/users/$it/workouts"
            firestore.collection(collectionPath).document(workoutId).delete().await()
        }
    }
}