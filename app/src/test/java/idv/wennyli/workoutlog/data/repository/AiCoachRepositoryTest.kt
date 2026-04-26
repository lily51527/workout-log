package idv.wennyli.workoutlog.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiCoachRepositoryTest {

    private lateinit var mockFunctions: FirebaseFunctions
    private lateinit var mockCallableReference: HttpsCallableReference
    private lateinit var repository: AiCoachRepository

    private val fakeAppId = "fake-app-id"

    // 初始化 mock 物件並建立 repository 實例，每個測試執行前重置狀態
    @Before
    fun setup() {
        mockFunctions = mockk()
        mockCallableReference = mockk()

        every { mockFunctions.getHttpsCallable("getAiCoachFeedback") } returns mockCallableReference

        repository = AiCoachRepositoryImpl(
            functions = mockFunctions,
            appId = fakeAppId
        )
    }

    // HttpsCallableResult 的 constructor 是 package-private，用反射建立真實實例
    private fun createCallableResult(data: Map<String, Any>): HttpsCallableResult {
        val constructor = HttpsCallableResult::class.java.getDeclaredConstructor(Any::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(data)
    }

    // --- 正常流程 ---

    // 驗證 API 回傳完整資料時，所有欄位都能正確對應到 AiCoachFeedback，包含 exercises 與 warnings
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
        every { mockCallableReference.call(any()) } returns Tasks.forResult(
            createCallableResult(
                fakeData
            )
        )

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

    // 驗證多筆 recommendedExercises 的順序與 API 回傳一致，不會被重新排序
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
        every { mockCallableReference.call(any()) } returns Tasks.forResult(
            createCallableResult(
                fakeData
            )
        )

        val result = repository.getFeedback()

        assertThat(result.recommendedExercises).hasSize(2)
        assertThat(result.recommendedExercises[0].exercise).isEqualTo("深蹲")
        assertThat(result.recommendedExercises[1].exercise).isEqualTo("臥推")
    }

    // --- 缺少選填欄位的邊界情況 ---

    // 驗證 API 未回傳 recommendedExercises 和 warnings 時，結果為空 list 而非 null 或例外
    @Test
    fun `getFeedback should return empty lists when recommendedExercises and warnings are absent`() =
        runTest {
            val fakeData: Map<String, Any> = mapOf(
                "summary" to "尚無資料",
                "reasoning" to "",
                "generatedAt" to 0L
            )
            every { mockCallableReference.call(any()) } returns Tasks.forResult(
                createCallableResult(
                    fakeData
                )
            )

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
            every { mockCallableReference.call(any()) } returns Tasks.forResult(
                createCallableResult(
                    fakeData
                )
            )

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
        every { mockCallableReference.call(any()) } returns Tasks.forResult(
            createCallableResult(
                fakeData
            )
        )

        val result = repository.getFeedback()

        assertThat(result.summary).isEmpty()
    }

    // --- 呼叫參數驗證 ---

    // 驗證呼叫 Cloud Functions 時使用正確的 function 名稱，且帶入正確的 appId 作為參數
    @Test
    fun `getFeedback should call correct function name with appId`() = runTest {
        val fakeData: Map<String, Any> =
            mapOf("summary" to "", "reasoning" to "", "generatedAt" to 0L)
        every { mockCallableReference.call(any()) } returns Tasks.forResult(
            createCallableResult(
                fakeData
            )
        )

        repository.getFeedback()

        verify(exactly = 1) { mockFunctions.getHttpsCallable("getAiCoachFeedback") }
        verify(exactly = 1) { mockCallableReference.call(mapOf("appId" to fakeAppId)) }
    }

    // --- 例外傳遞 ---

    // 驗證 Firebase Functions 回傳錯誤（如配額超限）時，例外會正確向上傳遞給 ViewModel 處理
    @Test
    fun `getFeedback should propagate exception when Firebase Functions call fails`() = runTest {
        val fakeException = Exception("RESOURCE_EXHAUSTED: quota exceeded")
        every { mockCallableReference.call(any()) } returns Tasks.forException(fakeException)

        var thrown: Exception? = null
        try {
            repository.getFeedback()
        } catch (e: Exception) {
            thrown = e
        }

        assertThat(thrown).isNotNull()
        assertThat(thrown?.message).contains("RESOURCE_EXHAUSTED")
    }

    // 驗證網路不可用時，例外會正確向上傳遞，由 AiCoachViewModel 的 catch 區塊對應至友善錯誤訊息
    @Test
    fun `getFeedback should propagate exception when network is unavailable`() = runTest {
        val fakeException = Exception("UNAVAILABLE: network error")
        every { mockCallableReference.call(any()) } returns Tasks.forException(fakeException)

        var thrown: Exception? = null
        try {
            repository.getFeedback()
        } catch (e: Exception) {
            thrown = e
        }

        assertThat(thrown).isNotNull()
        assertThat(thrown?.message).contains("UNAVAILABLE")
    }
}
