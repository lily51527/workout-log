package idv.wennyli.workoutlog.ui.view.aiCoach

import com.google.common.truth.Truth.assertThat
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.data.model.AiCoachException
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
        val fakeFeedback = AiCoachFeedback(
            summary = "訓練表現良好",
            reasoning = "分析推理內容",
            generatedAt = 1700000000000L
        )
        coEvery { mockRepository.getFeedback() } returns fakeFeedback

        viewModel.getFeedback()

        assertThat(viewModel.uiState.value).isEqualTo(AiCoachUiState.Success(fakeFeedback))
    }

    // --- 重複請求防護 ---

    // 驗證 Loading 狀態時再次呼叫 getFeedback 不會發出第二次請求
    @Test
    fun `getFeedback should not trigger new request when already loading`() = runTest {
        val fakeFeedback = AiCoachFeedback(summary = "test", generatedAt = 0L)
        coEvery { mockRepository.getFeedback() } returns fakeFeedback

        viewModel.getFeedback()
        // 手動把狀態設為 Loading 來模擬進行中的請求
        val loadingViewModel = AiCoachViewModel(mockk {
            coEvery { getFeedback() } coAnswers {
                kotlinx.coroutines.delay(1000)
                fakeFeedback
            }
        }, mockAppResource)

        assertThat(loadingViewModel.uiState.value).isEqualTo(AiCoachUiState.Idle)
    }

    // --- 錯誤訊息對應 ---

    // 驗證 QuotaExceeded 例外時，對應至配額超限的友善訊息
    @Test
    fun `getFeedback should map QuotaExceeded to quota exceeded message`() = runTest {
        coEvery { mockRepository.getFeedback() } throws AiCoachException.QuotaExceeded
        every { mockAppResource.getString(R.string.error_ai_quota_exceeded) } returns "AI 服務今日使用量已達上限，請明天再試。"

        viewModel.getFeedback()

        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("AI 服務今日使用量已達上限，請明天再試。")
    }

    // 驗證 Unauthenticated 例外時，對應至請先登入的友善訊息
    @Test
    fun `getFeedback should map Unauthenticated to login required message`() = runTest {
        coEvery { mockRepository.getFeedback() } throws AiCoachException.Unauthenticated
        every { mockAppResource.getString(R.string.error_ai_unauthenticated) } returns "請先登入才能使用 AI 教練。"

        viewModel.getFeedback()

        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("請先登入才能使用 AI 教練。")
    }

    // 驗證 Unavailable 例外時，對應至網路連線異常的友善訊息
    @Test
    fun `getFeedback should map Unavailable to network error message`() = runTest {
        coEvery { mockRepository.getFeedback() } throws AiCoachException.Unavailable
        every { mockAppResource.getString(R.string.error_ai_unavailable) } returns "無法連線至伺服器，請檢查網路後重試。"

        viewModel.getFeedback()

        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("無法連線至伺服器，請檢查網路後重試。")
    }

    // 驗證 Timeout 例外時，對應至請求逾時的友善訊息
    @Test
    fun `getFeedback should map Timeout to timeout message`() = runTest {
        coEvery { mockRepository.getFeedback() } throws AiCoachException.Timeout
        every { mockAppResource.getString(R.string.error_ai_timeout) } returns "請求逾時，請稍後再試。"

        viewModel.getFeedback()

        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("請求逾時，請稍後再試。")
    }

    // 驗證 Internal 例外時，對應至伺服器錯誤的友善訊息
    @Test
    fun `getFeedback should map Internal to server error message`() = runTest {
        coEvery { mockRepository.getFeedback() } throws AiCoachException.Internal
        every { mockAppResource.getString(R.string.error_ai_internal) } returns "伺服器發生錯誤，請稍後再試。"

        viewModel.getFeedback()

        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("伺服器發生錯誤，請稍後再試。")
    }

    // 驗證未預期的例外時，對應至通用錯誤訊息，確保不會有未處理的錯誤狀態
    @Test
    fun `getFeedback should map Unknown exception to generic error message`() = runTest {
        coEvery { mockRepository.getFeedback() } throws AiCoachException.Unknown()
        every { mockAppResource.getString(R.string.error_ai_unknown) } returns "無法取得 AI 回饋，請稍後再試。"

        viewModel.getFeedback()

        val state = viewModel.uiState.value as AiCoachUiState.Error
        assertThat(state.message).isEqualTo("無法取得 AI 回饋，請稍後再試。")
    }
}
