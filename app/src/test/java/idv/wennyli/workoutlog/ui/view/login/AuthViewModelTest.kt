package idv.wennyli.workoutlog.ui.view.login

import androidx.compose.runtime.ExperimentalComposeApi
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.ui.view.login.AuthState
import idv.wennyli.workoutlog.ui.view.login.AuthViewModel
import idv.wennyli.workoutlog.utils.AppResource
import idv.wennyli.workoutlog.utils.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalComposeApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockAppResource: AppResource
    private lateinit var viewModel: AuthViewModel

    // 用來 Mock 成功的結果 (因為 Firebase API 回傳的是 Task<AuthResult>)
    private lateinit var mockAuthAuthResult: AuthResult

    @Before
    fun setup() {
        mockAuth = mockk()
        mockAppResource = mockk()
        mockAuthAuthResult = mockk()

        every { mockAppResource.getString(R.string.error_auth_anonymous_failed) } returns "匿名登入失敗，請稍後再試。"
        every { mockAppResource.getString(R.string.error_auth_wrong_password) } returns "密碼錯誤。"
        every { mockAppResource.getString(R.string.error_auth_invalid_email) } returns "電子郵件格式不正確。"
        every { mockAppResource.getString(R.string.error_auth_weak_password) } returns "密碼強度不足，請至少設定 6 個字元。"
        every { mockAppResource.getString(R.string.error_auth_unknown) } returns "發生未知錯誤，請稍後再試。"
        every { mockAppResource.getString(R.string.error_auth_failed) } returns "驗證失敗，請稍後再試。"

        viewModel = AuthViewModel(mockAuth, mockAppResource)
    }

    // ==========================================
    // [R]ight - 正確路徑測試
    // ==========================================

    @Test
    fun `registerWithEmail should update state to Success on successful auth`() = runTest {
        // Arrange
        val fakeEmail = "test@mail.com"
        val fakePassword = "abcd"

        every {
            mockAuth.createUserWithEmailAndPassword(
                fakeEmail,
                fakePassword
            )
        } returns Tasks.forResult(mockAuthAuthResult)

        // Act
        viewModel.registerWithEmail(email = fakeEmail, password = fakePassword)

        // Assert
        // 因為使用了 UnconfinedTestDispatcher，協程會立刻跑完
        assertThat(viewModel.authState.value).isEqualTo(AuthState.Success)
        verify(exactly = 1) { mockAuth.createUserWithEmailAndPassword(fakeEmail, fakePassword) }
    }

    @Test
    fun `signInWithEmail should update state to Success on successful auth`() = runTest {
        // Arrange
        val fakeEmail = "test@mail.com"
        val fakePassword = "abcd"
        // 使用 Tasks.forResult 來模擬一個成功的 Firebase Task
        every {
            mockAuth.signInWithEmailAndPassword(
                fakeEmail,
                fakePassword
            )
        } returns Tasks.forResult(
            mockAuthAuthResult
        )

        // Act
        viewModel.signInWithEmail(email = fakeEmail, password = fakePassword)

        // Assert
        // 因為使用了 UnconfinedTestDispatcher，協程會立刻跑完
        assertThat(viewModel.authState.value).isEqualTo(AuthState.Success)
        verify(exactly = 1) { mockAuth.signInWithEmailAndPassword(fakeEmail, fakePassword) }
    }

    @Test
    fun `signInAnonymously should update state to Success on successful auth`() = runTest {
        // Arrange
        every { mockAuth.signInAnonymously() } returns Tasks.forResult(mockAuthAuthResult)

        // Act
        viewModel.signInAnonymously()

        // Assert
        assertThat(viewModel.authState.value).isEqualTo(AuthState.Success)
        verify(exactly = 1) { mockAuth.signInAnonymously() }
    }

    // ==========================================
    // [E]rror - 錯誤處理測試
    // ==========================================

    @Test
    fun `signInAnonymously should update state to Error on generic exception`() = runTest {
        // Arrange
        val errorMsg = "Network Timeout"
        every { mockAuth.signInAnonymously() } returns Tasks.forException(Exception(errorMsg))

        // Act
        viewModel.signInAnonymously()

        // Assert
        val state = viewModel.authState.value
        assertThat(state).isInstanceOf(AuthState.Error::class.java)
        assertThat((state as AuthState.Error).message).contains("匿名登入失敗")
    }

    // ==========================================
    // [B]oundary (CORRECT) - 格式不符/邊界條件
    // ==========================================
    // 這裡我們測試 Conformance(格式)，藉由模擬 Firebase 拋出的特定 Error Code，
    // 驗證我們 private fun mapFirebaseError 是否有被正確觸發並翻譯成中文。

    @Test
    fun `signInWithEmail should map ERROR_WRONG_PASSWORD to localized string`() = runTest {
        // Arrange
        // 模擬密碼錯誤的情境
        val firebaseException =
            FirebaseAuthException("ERROR_WRONG_PASSWORD", "Firebase error description")
        every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns Tasks.forException(
            firebaseException
        )

        // Act
        viewModel.signInWithEmail("test@example.com", "wrong_pass")

        // Assert
        val state = viewModel.authState.value
        assertThat(state).isInstanceOf(AuthState.Error::class.java)
        assertThat((state as AuthState.Error).message).contains("密碼錯誤。")
    }

    @Test
    fun `registerWithEmail should map ERROR_INVALID_EMAIL to localized string`() = runTest {
        // Arrange
        // 模擬使用者輸入了無效的 Email (例如 "abc")
        val firebaseException =
            FirebaseAuthException("ERROR_INVALID_EMAIL", "Bad format")
        every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns Tasks.forException(
            firebaseException
        )

        // Act
        viewModel.registerWithEmail("abc", "123456")

        // Assert
        val state = viewModel.authState.value
        assertThat(state).isInstanceOf(AuthState.Error::class.java)
        assertThat((state as AuthState.Error).message).contains("電子郵件格式不正確。")
    }

    @Test
    fun `registerWithEmail should map ERROR_WEAK_PASSWORD to localized string`() = runTest {
        // Arrange
        // 模擬使用者輸入過短的密碼 (低於 6 碼)
        val firebaseException = FirebaseAuthException("ERROR_WEAK_PASSWORD", "Weak")
        every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns Tasks.forException(
            firebaseException
        )

        // Act
        viewModel.registerWithEmail("test@example.com", "123")

        // Assert
        val state = viewModel.authState.value
        assertThat(state).isInstanceOf(AuthState.Error::class.java)
        assertThat((state as AuthState.Error).message).contains("密碼強度不足，請至少設定 6 個字元。")
    }
}