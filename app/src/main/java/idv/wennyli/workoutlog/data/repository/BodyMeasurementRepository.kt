package idv.wennyli.workoutlog.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import idv.wennyli.workoutlog.data.model.BodyMeasurement
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "BodyMeasurementRepository"

interface BodyMeasurementRepository {
    // 身體測量記錄相關
    fun getBodyMeasurements(): Flow<List<BodyMeasurement>>
    suspend fun addBodyMeasurement(measurement: BodyMeasurement)
    suspend fun deleteBodyMeasurement(measurementId: String)
}

class BodyMeasurementRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @Named("appId") private val appId: String
) : BodyMeasurementRepository {

    private val userId: String?
        get() = auth.currentUser?.uid

    override fun getBodyMeasurements(): Flow<List<BodyMeasurement>> = callbackFlow {
        val collectionPath = userId?.let { "artifacts/$appId/users/$it/measurements" } ?: run {
            trySend(emptyList())
            awaitClose()
            return@callbackFlow
        }
        val listener = firestore.collection(collectionPath)
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
                val measurements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(BodyMeasurement::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(measurements)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addBodyMeasurement(measurement: BodyMeasurement) {
        userId?.let {
            val collectionPath = "artifacts/$appId/users/$it/measurements"
            // 1. 建立一個帶有自動產生 ID 的新文件參照
            val newMeasurementRef = firestore.collection(collectionPath).document()
            // 2. 將自動產生的 ID 和 userId 複製到您的物件中
            val finalMeasurement = measurement.copy(id = newMeasurementRef.id, userId = it)
            // 3. 使用 set 方法將帶有 ID 的物件寫入該文件
            newMeasurementRef.set(finalMeasurement).await()
        }
    }

    override suspend fun deleteBodyMeasurement(measurementId: String) {
        userId?.let {
            val collectionPath = "artifacts/$appId/users/$it/measurements"
            firestore.collection(collectionPath).document(measurementId).delete().await()
        }
    }
}