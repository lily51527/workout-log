package idv.wennyli.workoutlog

import app.cash.turbine.test
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.data.repository.WorkoutRepository
import idv.wennyli.workoutlog.data.repository.WorkoutRepositoryImpl
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutRepositoryTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockCollectionReference: CollectionReference
    private lateinit var mockDocumentReference: DocumentReference
    private lateinit var mockListenerRegistration: ListenerRegistration

    private lateinit var repository: WorkoutRepository

    private val fakeAppId = "fake-app-id"
    private val fakeUserId = "fake-user-id"

    private val fakeWorkouts = listOf(
        Workout(id = "1", timestamp = Date(200L)),
        Workout(id = "2", timestamp = Date(100L))
    )

    @Before
    fun setup() {
        mockAuth = mockk()
        mockFirestore = mockk()
        mockUser = mockk()
        mockCollectionReference = mockk()
        mockDocumentReference = mockk()
        mockListenerRegistration = mockk()

        // 設定通用的模擬行為：使用者已登入
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns fakeUserId

        // 新增：當呼叫 remove 時，什麼都不做 (just runs)
        every { mockListenerRegistration.remove() } just runs

        repository = WorkoutRepositoryImpl(
            auth = mockAuth,
            firestore = mockFirestore,
            appId = fakeAppId
        )
    }

    @Test
    fun `getWorkouts should emit measurements on successful fetch`() = runTest {
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                any<String>(),
                any<Query.Direction>()
            )
        } returns mockCollectionReference

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockQuerySnapshot: QuerySnapshot = mockk {
            every { documents } returns fakeWorkouts.map { workout ->
                mockk<DocumentSnapshot> {
                    every { id } returns workout.id
                    every { toObject(Workout::class.java) } returns workout
                }
            }
        }

        repository.getWorkouts().test {
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)
            assertThat(awaitItem()).isEqualTo(fakeWorkouts)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) {
            mockCollectionReference.orderBy("timestamp", Query.Direction.DESCENDING)
        }
    }

    @Test
    fun `getWorkouts should emit empty list if user is not logged in`() = runTest {
        every { mockAuth.currentUser } returns null
        repository.getWorkouts().test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }

    // 當權限不足時，應該優雅地關閉 Flow
    @Test
    fun `getWorkouts should close flow gracefully when permission denied`() = runTest {
        // Arrange
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                any<String>(),
                any<Query.Direction>()
            )
        } returns mockCollectionReference

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // 準備一個模擬的 FirebaseFirestoreException
        val permissionDeniedException = mockk<FirebaseFirestoreException> {
            every { code } returns FirebaseFirestoreException.Code.PERMISSION_DENIED
            every { message } returns "Permission denied"
        }

        repository.getWorkouts().test {
            listenerSlot.captured.onEvent(null, permissionDeniedException)
            awaitComplete()
            ensureAllEventsConsumed()
        }

        verify(exactly = 1) { mockListenerRegistration.remove() }
    }

    @Test
    fun `getWorkouts should close flow gracefully when occur general Error`() = runTest {
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                any<String>(),
                any<Query.Direction>()
            )
        } returns mockCollectionReference

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val generalException =
            FirebaseFirestoreException("Unavailable", FirebaseFirestoreException.Code.UNAVAILABLE)

        repository.getWorkouts().test {
            listenerSlot.captured.onEvent(null, generalException)

            val error = awaitError()
            assertThat(error).isEqualTo(generalException)
        }

        verify(exactly = 1) { mockListenerRegistration.remove() }
    }

    @Test
    fun `getWorkouts should filter out invalid documents when parsing fails`() = runTest {
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                any<String>(),
                any<Query.Direction>()
            )
        } returns mockCollectionReference

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val doc1 = mockk<DocumentSnapshot> {
            every { id } returns "1"
            every { toObject(Workout::class.java) } returns fakeWorkouts[0]
        }

        val doc2 = mockk<DocumentSnapshot> {
            every { id } returns "invalid_doc"
            every { toObject(Workout::class.java) } returns null
        }

        val doc3 = mockk<DocumentSnapshot> {
            every { id } returns "2"
            every { toObject(Workout::class.java) } returns fakeWorkouts[1]
        }

        val mockQuerySnapshot: QuerySnapshot = mockk {
            every { documents } returns listOf(doc1, doc2, doc3)
        }

        repository.getWorkouts().test {
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)

            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result).containsExactly(fakeWorkouts[0], fakeWorkouts[1])

            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) {
            mockCollectionReference.orderBy("timestamp", Query.Direction.DESCENDING)
        }
    }

    @Test
    fun `getWorkouts should remove listener when flow is cancelled`() = runTest {
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                any<String>(),
                any<Query.Direction>()
            )
        } returns mockCollectionReference

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        repository.getWorkouts().test {
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { mockListenerRegistration.remove() }
    }

    @Test
    fun `addWorkout should call add with correct workout`() = runTest {
        every { mockFirestore.collection(any()) } returns mockCollectionReference

        val fakeGeneratedId = "generated_id_123"
        every { mockCollectionReference.document() } returns mockDocumentReference
        every { mockDocumentReference.id } returns fakeGeneratedId

        every { mockDocumentReference.set(any()) } returns Tasks.forResult(null)

        val newWorkout = Workout(timestamp = Date(200L), userId = "")
        repository.addWorkout(newWorkout)

        verify(exactly = 1) { mockFirestore.collection("artifacts/$fakeAppId/users/$fakeUserId/workouts") }
        verify(exactly = 1) { mockCollectionReference.document() }

        val workoutSlot = slot<Workout>()
        verify(exactly = 1) { mockDocumentReference.set(capture(workoutSlot)) }

        val capturedWorkout = workoutSlot.captured
        assertThat(capturedWorkout.id).isEqualTo(fakeGeneratedId)
        assertThat(capturedWorkout.userId).isEqualTo(fakeUserId)
    }

    @Test
    fun `addWorkout does nothing if user is not logged in`() = runTest {
        every { mockAuth.currentUser } returns null

        val newWorkout = Workout(timestamp = Date(3L))
        repository.addWorkout(newWorkout)

        verify(exactly = 0) { mockFirestore.collection(any()) }
    }

    @Test
    fun `updateWorkout should call update with correct workout`() = runTest {
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every { mockCollectionReference.document(any()) } returns mockDocumentReference
        every { mockDocumentReference.set(any()) } returns Tasks.forResult(null)

        val fakeWorkoutId = "generated_id_123"
        val updateWorkout =
            Workout(timestamp = Date(200L), userId = fakeUserId, id = fakeWorkoutId, exercise = "abc")
        repository.updateWorkout(updateWorkout)

        verify(exactly = 1) { mockFirestore.collection("artifacts/$fakeAppId/users/$fakeUserId/workouts") }

        verify(exactly = 1) { mockCollectionReference.document(fakeWorkoutId) }

        val workoutSlot = slot<Workout>()
        verify(exactly = 1) { mockDocumentReference.set(capture(workoutSlot)) }

        val capturedWorkout = workoutSlot.captured
        assertThat(capturedWorkout).isEqualTo(updateWorkout)
    }

    @Test
    fun `deleteWorkout should call delete on correct document`() = runTest {
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every { mockCollectionReference.document(any()) } returns mockDocumentReference
        every { mockDocumentReference.delete() } returns Tasks.forResult(null)

        val workoutId = "workout_id_123"
        repository.deleteWorkout(workoutId)

        verify(exactly = 1) { mockCollectionReference.document(workoutId) }
        verify(exactly = 1) { mockDocumentReference.delete() }
    }

    @Test
    fun `deleteWorkout does nothing if user is not logged in`() = runTest {
        every { mockAuth.currentUser } returns null

        val workoutIdToDelete = "21"

        repository.deleteWorkout(workoutIdToDelete)

        verify(exactly = 0) { mockFirestore.collection(any()) }
    }
}