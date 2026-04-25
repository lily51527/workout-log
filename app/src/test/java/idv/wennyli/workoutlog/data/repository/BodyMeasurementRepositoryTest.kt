package idv.wennyli.workoutlog.data.repository

import app.cash.turbine.test
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseException
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
import idv.wennyli.workoutlog.data.model.BodyMeasurement
import idv.wennyli.workoutlog.data.repository.BodyMeasurementRepository
import idv.wennyli.workoutlog.data.repository.BodyMeasurementRepositoryImpl
import io.mockk.awaits
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
import java.sql.Date

@OptIn(ExperimentalCoroutinesApi::class)
class BodyMeasurementRepositoryTest {

    // 宣告 mock 物件
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockCollectionReference: CollectionReference
    private lateinit var mockDocumentReference: DocumentReference
    private lateinit var mockListenerRegistration: ListenerRegistration

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
        mockListenerRegistration = mockk()

        // 設定通用的模擬行為：使用者已登入
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns fakeUserId

        // 新增：當呼叫 remove 時，什麼都不做 (just runs)
        every { mockListenerRegistration.remove() } just runs

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
                "timestamp",
                Query.Direction.DESCENDING
            )
        } returns mockCollectionReference

        // 捕捉 addSnapshotListener 的 Listener
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

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

        verify(exactly = 1) {
            mockCollectionReference.orderBy("timestamp", Query.Direction.DESCENDING)
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

            // 取消訂閱
            cancelAndIgnoreRemainingEvents()
        }
        // 確保 Firestore 的任何方法都未被呼叫
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }

    // 當權限不足時，應該優雅地關閉 Flow
    @Test
    fun `getBodyMeasurements should close flow gracefully when permission denied`() = runTest {
        // Arrange
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            )
        } returns mockCollectionReference

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // 準備一個模擬的 FirebaseFirestoreException
        val permissionDeniedException = mockk<FirebaseFirestoreException> {
            every { code } returns FirebaseFirestoreException.Code.PERMISSION_DENIED
            every { message } returns "Permission denied"
        }

        // Act
        repository.getBodyMeasurements().test {
            // 觸發錯誤
            listenerSlot.captured.onEvent(null, permissionDeniedException)

            // Assert
            // 驗證 Flow 是否 "完成" (Complete) 而不是 "報錯" (Error)
            awaitComplete()

            // 確保沒有其他未預期的事件 (因為 Flow 已經結束，不能用 cancelAndIgnoreRemainingEvents)
            ensureAllEventsConsumed()
        }

        // 額外驗證：確認有呼叫 remove 來清理資源
        verify(exactly = 1) { mockListenerRegistration.remove() }
    }

    // 其他一般錯誤 (General Error)
    @Test
    fun `getBodyMeasurements should close flow gracefully when occur general Error`() = runTest {
        // Arrange
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            )
        } returns mockCollectionReference

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // 1. 準備一個 "其他" 錯誤 (例如 UNAVAILABLE)
        // 修正：使用【真實】的 Exception 物件，而不是 Mock。
        // 因為我們已經加了 SparseArray stub，所以這裡可以直接 new 出來，不會報 Stub! 錯。
        // 這樣 Coroutines 內部讀取 getCause() 時才不會崩潰。
        val generalException =
            FirebaseFirestoreException("Unavailable", FirebaseFirestoreException.Code.UNAVAILABLE)

        // Act
        repository.getBodyMeasurements().test {
            // 2. 觸發錯誤
            listenerSlot.captured.onEvent(null, generalException)

            // Assert
            // 3. 驗證收到的是 awaitError()，而且拋出的異常就是我們準備的那一個
            val error = awaitError()
            assertThat(error).isEqualTo(generalException)
        }

        // 驗證即使發生錯誤，資源依然有被清理 (remove 仍會被呼叫)
        verify(exactly = 1) { mockListenerRegistration.remove() }
    }

    // 新增測試：資料轉換容錯 (Parsing Error)
    @Test
    fun `getBodyMeasurements should filter out invalid documents when parsing fails`() = runTest {
        // Arrange
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            )
        } returns mockCollectionReference

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // 準備 3 個 DocumentSnapshot
        // Doc 1: 正常資料
        val doc1 = mockk<DocumentSnapshot> {
            every { id } returns "1"
            every { toObject(BodyMeasurement::class.java) } returns fakeMeasurements[0]
        }

        // Doc 2: 壞掉的資料 (模擬 toObject 回傳 null)
        val doc2 = mockk<DocumentSnapshot> {
            every { id } returns "invalid_doc"
            every { toObject(BodyMeasurement::class.java) } returns null
        }

        // Doc 3: 正常資料
        val doc3 = mockk<DocumentSnapshot> {
            every { id } returns "2"
            every { toObject(BodyMeasurement::class.java) } returns fakeMeasurements[1]
        }

        // 模擬 QuerySnapshot 包含這 3 筆文件
        val mockQuerySnapshot: QuerySnapshot = mockk {
            every { documents } returns listOf(doc1, doc2, doc3)
        }

        // Act
        repository.getBodyMeasurements().test {
            // 觸發事件
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)

            // Assert
            // 驗證我們只收到 2 筆資料 (壞掉的那筆被濾掉了)
            val result = awaitItem()
            assertThat(result).hasSize(2)
            // 驗證內容確實是 doc1 和 doc3
            assertThat(result).containsExactly(fakeMeasurements[0], fakeMeasurements[1])

            cancelAndIgnoreRemainingEvents()
        }
    }

    // 新增測試：驗證 Flow 取消時會移除監聽器
    @Test
    fun `getBodyMeasurements should remove listener when flow is cancelled`() = runTest {
        // Arrange
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every {
            mockCollectionReference.orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            )
        } returns mockCollectionReference

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        // 重要：一定要回傳我們控制的 mockListenerRegistration
        every { mockCollectionReference.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // Act
        repository.getBodyMeasurements().test {
            // 這裡我們甚至不需要發送任何資料，只要開啟 Flow
            // 然後立刻取消訂閱
            cancelAndIgnoreRemainingEvents()
        }

        // Assert
        // 驗證：當 Flow 被取消後，remove() 必須被呼叫
        verify(exactly = 1) { mockListenerRegistration.remove() }
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