package idv.wennyli.workoutlog.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import idv.wennyli.workoutlog.data.model.BodyMeasurement
import idv.wennyli.workoutlog.utils.FirestorePaths
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

    // 集中管理集合參照，避免路徑字串重複出現
    private val measurementsCollection: CollectionReference?
        get() = userId?.let { firestore.collection(FirestorePaths.measurements(appId, it)) }

    override fun getBodyMeasurements(): Flow<List<BodyMeasurement>> = callbackFlow {
        val collection = measurementsCollection ?: run {
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
                val measurements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(BodyMeasurement::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(measurements)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addBodyMeasurement(measurement: BodyMeasurement) {
        val uid = userId ?: return
        val collection = measurementsCollection ?: return
        val newRef = collection.document()
        newRef.set(measurement.copy(id = newRef.id, userId = uid)).await()
    }

    override suspend fun deleteBodyMeasurement(measurementId: String) {
        val collection = measurementsCollection ?: return
        collection.document(measurementId).delete().await()
    }
}
