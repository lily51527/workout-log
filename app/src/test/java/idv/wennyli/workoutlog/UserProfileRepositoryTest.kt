package idv.wennyli.workoutlog

import app.cash.turbine.test
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import idv.wennyli.workoutlog.data.model.UserProfile
import idv.wennyli.workoutlog.data.repository.UserProfileRepository
import idv.wennyli.workoutlog.data.repository.UserProfileRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileRepositoryTest {

    // 宣告 mock 物件
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockDocumentReference: DocumentReference

    private lateinit var repository: UserProfileRepository

    private val fakeAppId = "fake-app-id"
    private val fakeUserId = "fake-user-id"

    @Before
    fun setup() {
        // 使用 MockK 來創建模擬物件
        mockAuth = mockk()
        mockFirestore = mockk()
        mockUser = mockk()
        mockDocumentReference = mockk()

        // 設定一些通用的模擬行為
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns fakeUserId

        // 創建我們要測試的 Repository 實例
        repository = UserProfileRepositoryImpl(
            auth = mockAuth,
            firestore = mockFirestore,
            appId = fakeAppId
        )
    }

    // 測試 getUserProfile() 函式
    @Test
    fun `getUserProfile emits user profile when data is available`() = runTest {
        // 模擬 Firestore 呼叫路徑
        every { mockFirestore.document(any()) } returns mockDocumentReference

        // 捕捉傳遞給 addSnapshotListener 的 Listener
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        every { mockDocumentReference.addSnapshotListener(capture(listenerSlot)) } returns mockk()

        // 模擬一個 DocumentSnapshot
        val fakeUserProfile = UserProfile("Test User", "test@email.com")
        val mockSnapshot: DocumentSnapshot = mockk {
            every { toObject(UserProfile::class.java) } returns fakeUserProfile
            every { exists() } returns true
        }

        // 使用 Turbine 測試 Flow
        repository.getUserProfile().test {
            // 模擬 Firestore 發送資料
            listenerSlot.captured.onEvent(mockSnapshot, null)

            // 使用 Turbine 的 awaitItem() 等待並取得發出的第一個值
            val userProfile = awaitItem()

            // 使用 Truth 進行斷言，語法更流暢
            assertThat(userProfile).isEqualTo(fakeUserProfile)

            // 確保沒有額外的事件發出
            expectNoEvents()

            // 取消訂閱
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getUserProfile emits null when user is not logged in`() = runTest {
        // 模擬使用者未登入
        every { mockAuth.currentUser } returns null

        // 使用 Turbine 測試 Flow
        repository.getUserProfile().test {
            // 檢查發出的第一個值是否為 null
            val userProfile = awaitItem()
            assertThat(userProfile).isNull()

            // 確保 Firestore 的 document 方法沒有被呼叫
            verify(exactly = 0) { mockFirestore.document(any()) }

            // 取消訂閱
            cancelAndIgnoreRemainingEvents()
        }
    }

    // 測試 updateUserProfile() 函式
    @Test
    fun `updateUserProfile calls set with correct user profile`() = runTest {
        // 模擬 Firestore 呼叫路徑
        every { mockFirestore.document(any()) } returns mockDocumentReference

        // 模擬 set() 函式，並設定為成功
        every { mockDocumentReference.set(any()) } returns Tasks.forResult(null)

        val fakeUserProfile = UserProfile("Updated Name", "updated@email.com")

        // 呼叫我們要測試的 suspend 函式
        repository.updateUserProfile(fakeUserProfile)

        // 驗證 document() 函式是否被呼叫，且路徑正確
        val expectedPath = "artifacts/$fakeAppId/users/$fakeUserId/profile/data"
        verify(exactly = 1) { mockFirestore.document(expectedPath) }

        // 驗證 set() 函式是否被呼叫，且參數正確
        verify(exactly = 1) { mockDocumentReference.set(eq(fakeUserProfile)) }
    }

    @Test
    fun `updateUserProfile does nothing if user is not logged in`() = runTest {
        // 模擬使用者未登入
        every { mockAuth.currentUser } returns null

        val fakeUserProfile = UserProfile("Updated Name", "updated@email.com")

        // 呼叫函式
        repository.updateUserProfile(fakeUserProfile)

        // 驗證 Firestore 的任何方法都未被呼叫
        verify(exactly = 0) { mockFirestore.document(any()) }
    }
}