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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.data.repository.WorkoutRepository
import idv.wennyli.workoutlog.data.repository.WorkoutRepositoryImpl
import io.mockk.every
import io.mockk.mockk
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

        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns fakeUserId

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
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockk()

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

    @Test
    fun `addWorkout should call add with correct workout`() = runTest {
        every { mockFirestore.collection(any()) } returns mockCollectionReference

        val fakeGeneratedId = "generated_id_123"
        every { mockCollectionReference.document() } returns mockDocumentReference
        every { mockDocumentReference.id } returns fakeGeneratedId

        every { mockDocumentReference.set(any()) } returns Tasks.forResult(null)

        val newWorkout = Workout(timestamp = Date(200L), userId = "")
        repository.addWorkout(newWorkout)

        verify(exactly = 1) { mockCollectionReference.document() }

        val measurementSlot = slot<Workout>()
        verify(exactly = 1) { mockDocumentReference.set(capture(measurementSlot)) }

        val capturedWorkout = measurementSlot.captured
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
    fun `deleteWorkout should call delete on correct document`() = runTest {
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every { mockCollectionReference.document(any()) } returns mockDocumentReference
        every { mockDocumentReference.delete() } returns Tasks.forResult(null)

        val workoutId = "workout_id_123"
        repository.deleteWorkout(workoutId)

        verify(exactly = 1) { mockCollectionReference.document(workoutId) }
        verify(exactly = 1) { mockDocumentReference.delete() }
    }
}