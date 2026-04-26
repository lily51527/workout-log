package idv.wennyli.workoutlog.ui.view.aiCoach

import com.google.common.truth.Truth.assertThat
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.data.model.AiCoachFeedback
import idv.wennyli.workoutlog.data.repository.AiCoachRepository
import idv.wennyli.workoutlog.utils.AppResource
import idv.wennyli.workoutlog.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiCoachViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: AiCoachRepository
    private lateinit var mockAppResource: AppResource
    private lateinit var viewModel: AiCoachViewModel

    @Before
    fun setup() {
        mockRepository = mockk()
        mockAppResource = mockk()
        viewModel = AiCoachViewModel(mockRepository, mockAppResource)
    }

    // 驗證 ViewModel 建立後初始狀態為 Idle，尚未觸發任何 API 呼叫
    @Test
    fun `initial uiState should be Idle`() {
        assertThat(viewModel.uiState.value).isEqualTo(AiCoachUiState.Idle)
    }

    // --- 成功路徑 ---

    // 驗證 repository 成功回傳 feedback 時，uiState 更新為 Success 並包含完整資料
    @Test
    fun `getFeedback should set uiState to Success when repository returns feedback`() = runTest {
        // Given
        val fakeFeedback = AiCoachFeedback(
            summary = "訓練表現良好",
            reasoning = "分析推理內容",
            generatedAt = 1700000000000L
        )
        coEvery { mockRepository.getFeedback() } returns fakeFeedback

        // When
        viewModel.getFeedback()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(AiCoachUiState.Success(fakeFeedback))
    }

    // --- 錯誤訊息對應 ---

    // 驗證 RESOURCE_EXHAUSTED 例外時，對應至配額超限的友善訊息
    @Test
    fun `getFeedback should map RESOURCE_EXHAUSTED to quota exceeded message`() = runTest {
        // Given
        coEvery { mockRepository.getFeedback() } throws Exception("RESOURCE_EXHAUSTED: quota exceeded")
        every { mockAppResource.getString(R.string.error_ai_quota_exceeded) } returns "AI 服務今日使用量已達上限，請明天再試。"

        // When
        viewModel.getFeedback()

        // Then
        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("AI 服務今日使用量已達上限，請明天再試。")
    }

    // 驗證 UNAUTHENTICATED 例外時，對應至請先登入的友善訊息
    @Test
    fun `getFeedback should map UNAUTHENTICATED to login required message`() = runTest {
        // Given
        coEvery { mockRepository.getFeedback() } throws Exception("UNAUTHENTICATED")
        every { mockAppResource.getString(R.string.error_ai_unauthenticated) } returns "請先登入才能使用 AI 教練。"

        // When
        viewModel.getFeedback()

        // Then
        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("請先登入才能使用 AI 教練。")
    }

    // 驗證 UNAVAILABLE 例外時，對應至網路連線異常的友善訊息
    @Test
    fun `getFeedback should map UNAVAILABLE to network error message`() = runTest {
        // Given
        coEvery { mockRepository.getFeedback() } throws Exception("UNAVAILABLE: network error")
        every { mockAppResource.getString(R.string.error_ai_unavailable) } returns "無法連線至伺服器，請檢查網路後重試。"

        // When
        viewModel.getFeedback()

        // Then
        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("無法連線至伺服器，請檢查網路後重試。")
    }

    // 驗證 DEADLINE_EXCEEDED 例外時，對應至請求逾時的友善訊息
    @Test
    fun `getFeedback should map DEADLINE_EXCEEDED to timeout message`() = runTest {
        // Given
        coEvery { mockRepository.getFeedback() } throws Exception("DEADLINE_EXCEEDED")
        every { mockAppResource.getString(R.string.error_ai_timeout) } returns "請求逾時，請稍後再試。"

        // When
        viewModel.getFeedback()

        // Then
        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("請求逾時，請稍後再試。")
    }

    // 驗證 INTERNAL 例外時，對應至伺服器錯誤的友善訊息
    @Test
    fun `getFeedback should map INTERNAL to server error message`() = runTest {
        // Given
        coEvery { mockRepository.getFeedback() } throws Exception("INTERNAL: server error")
        every { mockAppResource.getString(R.string.error_ai_internal) } returns "伺服器發生錯誤，請稍後再試。"

        // When
        viewModel.getFeedback()

        // Then
        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("伺服器發生錯誤，請稍後再試。")
    }

    // 驗證未預期的例外時，對應至通用錯誤訊息，確保不會有未處理的錯誤狀態
    @Test
    fun `getFeedback should map unknown exception to generic error message`() = runTest {
        // Given
        coEvery { mockRepository.getFeedback() } throws Exception("Some unknown error")
        every { mockAppResource.getString(R.string.error_ai_unknown) } returns "無法取得 AI 回饋，請稍後再試。"

        // When
        viewModel.getFeedback()

        // Then
        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("無法取得 AI 回饋，請稍後再試。")
    }
}
