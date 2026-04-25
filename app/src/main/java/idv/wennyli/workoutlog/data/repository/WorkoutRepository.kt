package idv.wennyli.workoutlog.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.utils.FirestorePaths
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "WorkoutRepository"

// Repository 介面，定義資料操作的合約
interface WorkoutRepository {
    // 訓練記錄相關
    fun getWorkouts(): Flow<List<Workout>>
    suspend fun addWorkout(workout: Workout)
    suspend fun updateWorkout(workout: Workout)
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

    // 集中管理集合參照，避免路徑字串重複出現
    private val workoutsCollection: CollectionReference?
        get() = userId?.let { firestore.collection(FirestorePaths.workouts(appId, it)) }

    override fun getWorkouts(): Flow<List<Workout>> = callbackFlow {
        val collection = workoutsCollection ?: run {
            trySend(emptyList())
            awaitClose()
            return@callbackFlow
        }
        val listener = collection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "SnapshotListener Error : $error")
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                    } else {
                        close(error)
                    }
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
        val uid = userId ?: return
        val collection = workoutsCollection ?: return
        val newRef = collection.document()
        newRef.set(workout.copy(id = newRef.id, userId = uid)).await()
    }

    override suspend fun updateWorkout(workout: Workout) {
        val collection = workoutsCollection ?: return
        collection.document(workout.id).set(workout).await()
    }

    override suspend fun deleteWorkout(workoutId: String) {
        val collection = workoutsCollection ?: return
        collection.document(workoutId).delete().await()
    }
}
