package idv.wennyli.workoutlog.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import idv.wennyli.workoutlog.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "UserProfileRepository"

interface UserProfileRepository {
    // 使用者個人資料相關
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun updateUserProfile(userProfile: UserProfile)
}

class UserProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @Named("appId") private val appId: String
) : UserProfileRepository {

    private val userId: String?
        get() = auth.currentUser?.uid

    override fun getUserProfile(): Flow<UserProfile?> = callbackFlow {
        val docPath = userId?.let { "artifacts/$appId/users/$it/profile/data" } ?: run {
            trySend(null)
            awaitClose()
            return@callbackFlow
        }

        val listener = firestore.document(docPath)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "SnapshotListener Error : $error")
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                    } else {
                        error(error)
                    }
                    return@addSnapshotListener
                }
                val userProfile = snapshot?.toObject(UserProfile::class.java)
                trySend(userProfile)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateUserProfile(userProfile: UserProfile) {
        userId?.let {
            val docPath = "artifacts/$appId/users/$it/profile/data"
            firestore.document(docPath).set(userProfile).await()
        }
    }
}