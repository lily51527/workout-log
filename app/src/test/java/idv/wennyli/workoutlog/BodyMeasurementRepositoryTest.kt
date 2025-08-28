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
import idv.wennyli.workoutlog.data.model.BodyMeasurement
import idv.wennyli.workoutlog.data.repository.BodyMeasurementRepository
import idv.wennyli.workoutlog.data.repository.BodyMeasurementRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.sql.Date

@OptIn(ExperimentalCoroutinesApi::class)
class BodyMeasurementRepositoryTest {

    // 宣告 mock 物件
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockCollectionReference: CollectionReference
    private lateinit var mockDocumentReference: DocumentReference

    private lateinit var repository: BodyMeasurementRepository

    private val fakeAppId = "fake-app-id"
    private val fakeUserId = "fake-user-id"

    private val fakeMeasurements = listOf(
        BodyMeasurement(id = "1", timestamp = Date(200L)),
        BodyMeasurement(id = "2", timestamp = Date(100L))
    )

    @Before
    fun setup() {
        mockAuth = mockk()
        mockFirestore = mockk()
        mockUser = mockk()
        mockCollectionReference = mockk()
        mockDocumentReference = mockk()

        // 設定通用的模擬行為：使用者已登入
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns fakeUserId

        repository = BodyMeasurementRepositoryImpl(
            auth = mockAuth,
            firestore = mockFirestore,
            appId = fakeAppId
        )
    }

    // getBodyMeasurements()
    @Test
    fun `getBodyMeasurements should emit measurements on successful fetch`() = runTest {
        // 模擬 Firestore 呼叫路徑
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                any<String>(),
                any<Query.Direction>()
            )
        } returns mockCollectionReference

        // 捕捉 addSnapshotListener 的 Listener
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockk()

        // 模擬 QuerySnapshot
        val mockQuerySnapshot: QuerySnapshot = mockk {
            every { documents } returns fakeMeasurements.map { measurement ->
                mockk<DocumentSnapshot> {
                    every { id } returns measurement.id
                    every { toObject(BodyMeasurement::class.java) } returns measurement
                }
            }
        }

        // 使用 Turbine 測試 Flow
        repository.getBodyMeasurements().test {
            // 模擬 Firestore 發送資料
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)

            // 檢查發出的值是否正確
            assertThat(awaitItem()).isEqualTo(fakeMeasurements)

            // 確保沒有額外的值發出
            expectNoEvents()

            // 取消訂閱
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getBodyMeasurements should emit empty list if user is not logged in`() = runTest {
        // 模擬使用者未登入
        every { mockAuth.currentUser } returns null

        // 使用 Turbine 測試 Flow
        repository.getBodyMeasurements().test {
            // 檢查發出的第一個值是否為空列表
            assertThat(awaitItem()).isEmpty()

            // 確保 Firestore 的任何方法都未被呼叫
            verify(exactly = 0) { mockFirestore.collection(any()) }

            // 取消訂閱
            cancelAndIgnoreRemainingEvents()
        }
    }

    //測試 addBodyMeasurement() 函式
    @Test
    fun `addBodyMeasurement should call add with correct measurement`() = runTest {
        // Arrange: 準備階段
        // 1. 模擬 Firestore 呼叫路徑
        every { mockFirestore.collection(any()) } returns mockCollectionReference

        // 2. 模擬 document() 函式，並設定一個假 ID
        val fakeGeneratedId = "generated_id_123"
        every { mockCollectionReference.document() } returns mockDocumentReference
        every { mockDocumentReference.id } returns fakeGeneratedId

        // 3. 模擬 set() 函式，並設定為成功
        every { mockDocumentReference.set(any()) } returns Tasks.forResult(null)

        val newMeasurement = BodyMeasurement(timestamp = Date(3L), userId = "")

        // Act: 行動階段
        // 呼叫要測試的 suspend 函式
        repository.addBodyMeasurement(newMeasurement)

        // Assert: 斷言階段
        // 驗證 collection() 函式被呼叫，且路徑正確
        verify(exactly = 1) { mockCollectionReference.document() }

        // 捕捉並驗證傳遞給 add() 的參數
        val measurementSlot = slot<BodyMeasurement>()
        verify(exactly = 1) { mockDocumentReference.set(capture(measurementSlot)) }

        // 3. 驗證最終寫入的物件包含了 userId 和自動生成的 id
        val captureMeasurement = measurementSlot.captured
        assertThat(captureMeasurement.userId).isEqualTo(fakeUserId)
        assertThat(captureMeasurement.id).isEqualTo(fakeGeneratedId)
    }

    @Test
    fun `addBodyMeasurement does nothing if user is not logged in`() = runTest {
        // 模擬使用者未登入
        every { mockAuth.currentUser } returns null

        val newMeasurement = BodyMeasurement(timestamp = Date(3L))
        repository.addBodyMeasurement(newMeasurement)

        // 驗證 Firestore 的任何方法都未被呼叫
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }

    // 測試 deleteBodyMeasurement() 函式
    @Test
    fun `deleteBodyMeasurement should call delete on correct document`() = runTest {
        // 模擬 Firestore 呼叫路徑
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every { mockCollectionReference.document(any()) } returns mockDocumentReference
        every { mockDocumentReference.delete() } returns Tasks.forResult(null)

        val measurementIdToDelete = "21"

        repository.deleteBodyMeasurement(measurementIdToDelete)

        verify(exactly = 1) { mockCollectionReference.document(measurementIdToDelete) }

        verify(exactly = 1) { mockDocumentReference.delete() }
    }

    @Test
    fun `deleteBodyMeasurement does nothing if user is not logged in`() = runTest {
        every { mockAuth.currentUser } returns null

        val measurementIdToDelete = "21"

        repository.deleteBodyMeasurement(measurementIdToDelete)

        verify(exactly = 0) { mockFirestore.collection(any()) }
    }
}