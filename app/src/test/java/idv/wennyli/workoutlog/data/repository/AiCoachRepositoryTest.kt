package idv.wennyli.workoutlog.data.repository

import com.google.common.truth.Truth.assertThat
import idv.wennyli.workoutlog.data.model.AiCoachException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiCoachRepositoryTest {

    private lateinit var mockDataSource: AiCoachDataSource
    private lateinit var repository: AiCoachRepository

    private val fakeAppId = "fake-app-id"

    @Before
    fun setup() {
        mockDataSource = mockk()
        repository = AiCoachRepositoryImpl(
            dataSource = mockDataSource,
            appId = fakeAppId
        )
    }

    // --- 正常流程 ---

    // 驗證 DataSource 回傳完整資料時，所有欄位都能正確對應到 AiCoachFeedback，包含 exercises 與 warnings
    @Test
    fun `getFeedback should return correctly mapped AiCoachFeedback on success`() = runTest {
        val fakeTimestamp = 1700000000000L
        val fakeData: Map<String, Any> = mapOf(
            "summary" to "訓練表現良好",
            "reasoning" to "分析推理內容",
            "warnings" to listOf("注意休息", "下肢訓練不足"),
            "recommendedExercises" to listOf(
                mapOf(
                    "exercise" to "深蹲",
                    "muscleGroup" to "下肢",
                    "intensitySuggestion" to "4 組 8-10 下"
                )
            ),
            "generatedAt" to fakeTimestamp
        )
        coEvery { mockDataSource.fetchFeedback(fakeAppId) } returns fakeData

        val result = repository.getFeedback()

        assertThat(result.summary).isEqualTo("訓練表現良好")
        assertThat(result.reasoning).isEqualTo("分析推理內容")
        assertThat(result.warnings).containsExactly("注意休息", "下肢訓練不足").inOrder()
        assertThat(result.generatedAt).isEqualTo(fakeTimestamp)
        assertThat(result.recommendedExercises).hasSize(1)
        with(result.recommendedExercises[0]) {
            assertThat(exercise).isEqualTo("深蹲")
            assertThat(muscleGroup).isEqualTo("下肢")
            assertThat(intensitySuggestion).isEqualTo("4 組 8-10 下")
        }
    }

    // 驗證多筆 recommendedExercises 的順序與 DataSource 回傳一致，不會被重新排序
    @Test
    fun `getFeedback should return multiple recommended exercises in order`() = runTest {
        val fakeData: Map<String, Any> = mapOf(
            "summary" to "test",
            "reasoning" to "",
            "generatedAt" to 0L,
            "recommendedExercises" to listOf(
                mapOf("exercise" to "深蹲", "muscleGroup" to "下肢", "intensitySuggestion" to ""),
                mapOf("exercise" to "臥推", "muscleGroup" to "胸大肌", "intensitySuggestion" to "")
            )
        )
        coEvery { mockDataSource.fetchFeedback(fakeAppId) } returns fakeData

        val result = repository.getFeedback()

        assertThat(result.recommendedExercises).hasSize(2)
        assertThat(result.recommendedExercises[0].exercise).isEqualTo("深蹲")
        assertThat(result.recommendedExercises[1].exercise).isEqualTo("臥推")
    }

    // --- 缺少選填欄位的邊界情況 ---

    // 驗證 DataSource 未回傳 recommendedExercises 和 warnings 時，結果為空 list 而非 null 或例外
    @Test
    fun `getFeedback should return empty lists when recommendedExercises and warnings are absent`() =
        runTest {
            val fakeData: Map<String, Any> = mapOf(
                "summary" to "尚無資料",
                "reasoning" to "",
                "generatedAt" to 0L
            )
            coEvery { mockDataSource.fetchFeedback(fakeAppId) } returns fakeData

            val result = repository.getFeedback()

            assertThat(result.recommendedExercises).isEmpty()
            assertThat(result.warnings).isEmpty()
        }

    // 驗證 exercise 物件內的欄位缺失時，各欄位 fallback 為空字串而非拋出例外
    @Test
    fun `getFeedback should use empty string defaults when exercise fields are missing`() =
        runTest {
            val fakeData: Map<String, Any> = mapOf(
                "summary" to "test",
                "reasoning" to "",
                "generatedAt" to 0L,
                "recommendedExercises" to listOf(mapOf<String, Any>())
            )
            coEvery { mockDataSource.fetchFeedback(fakeAppId) } returns fakeData

            val result = repository.getFeedback()

            assertThat(result.recommendedExercises).hasSize(1)
            with(result.recommendedExercises[0]) {
                assertThat(exercise).isEmpty()
                assertThat(muscleGroup).isEmpty()
                assertThat(intensitySuggestion).isEmpty()
            }
        }

    // 驗證 summary 欄位缺失時 fallback 為空字串，確保 null-safe 解析邏輯正確
    @Test
    fun `getFeedback should use empty string when summary is absent`() = runTest {
        val fakeData: Map<String, Any> = mapOf(
            "reasoning" to "",
            "generatedAt" to 0L
        )
        coEvery { mockDataSource.fetchFeedback(fakeAppId) } returns fakeData

        val result = repository.getFeedback()

        assertThat(result.summary).isEmpty()
    }

    // 驗證 generatedAt 為 Double 型別時仍能正確轉換，不會回傳 0L
    @Test
    fun `getFeedback should correctly convert generatedAt when returned as Double`() = runTest {
        val fakeData: Map<String, Any> = mapOf(
            "summary" to "test",
            "reasoning" to "",
            "generatedAt" to 1700000000000.0
        )
        coEvery { mockDataSource.fetchFeedback(fakeAppId) } returns fakeData

        val result = repository.getFeedback()

        assertThat(result.generatedAt).isEqualTo(1700000000000L)
    }

    // --- 呼叫參數驗證 ---

    // 驗證呼叫 DataSource 時帶入正確的 appId
    @Test
    fun `getFeedback should call dataSource with correct appId`() = runTest {
        val fakeData: Map<String, Any> =
            mapOf("summary" to "", "reasoning" to "", "generatedAt" to 0L)
        coEvery { mockDataSource.fetchFeedback(fakeAppId) } returns fakeData

        repository.getFeedback()

        coVerify(exactly = 1) { mockDataSource.fetchFeedback(fakeAppId) }
    }

    // --- 例外傳遞 ---

    // 驗證 DataSource 拋出 AiCoachException 時，例外會正確向上傳遞給 ViewModel 處理
    @Test
    fun `getFeedback should propagate AiCoachException when dataSource throws`() = runTest {
        coEvery { mockDataSource.fetchFeedback(any()) } throws AiCoachException.QuotaExceeded

        var thrown: Exception? = null
        try {
            repository.getFeedback()
        } catch (e: Exception) {
            thrown = e
        }

        assertThat(thrown).isInstanceOf(AiCoachException.QuotaExceeded::class.java)
    }

    // 驗證網路不可用時，AiCoachException.Unavailable 正確向上傳遞
    @Test
    fun `getFeedback should propagate AiCoachException when network is unavailable`() = runTest {
        coEvery { mockDataSource.fetchFeedback(any()) } throws AiCoachException.Unavailable

        var thrown: Exception? = null
        try {
            repository.getFeedback()
        } catch (e: Exception) {
            thrown = e
        }

        assertThat(thrown).isInstanceOf(AiCoachException.Unavailable::class.java)
    }
}
