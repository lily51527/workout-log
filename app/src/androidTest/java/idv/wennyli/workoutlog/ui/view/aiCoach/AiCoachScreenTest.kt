package idv.wennyli.workoutlog.ui.view.aiCoach

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import idv.wennyli.workoutlog.data.model.AiCoachFeedback
import idv.wennyli.workoutlog.data.model.RecommendedExercise
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AiCoachScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Idle 狀態 ---

    // 驗證初始狀態顯示標題、說明文字與「取得 AI 回饋」按鈕
    @Test
    fun idleState_showsTitleDescriptionAndButton() {
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Idle,
                    onRequestFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("AI 健身教練").assertIsDisplayed()
        composeTestRule.onNodeWithText("點擊下方按鈕，讓 AI 分析你的訓練紀錄").assertIsDisplayed()
        composeTestRule.onNodeWithText("取得 AI 回饋").assertIsDisplayed()
    }

    // 驗證點擊「取得 AI 回饋」按鈕會觸發 onRequestFeedback callback
    @Test
    fun idleState_clickGetFeedbackButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Idle,
                    onRequestFeedback = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("取得 AI 回饋").performClick()

        assertTrue(clicked)
    }

    // --- Loading 狀態 ---

    // 驗證 Loading 狀態顯示分析中提示文字
    @Test
    fun loadingState_showsLoadingText() {
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Loading,
                    onRequestFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("AI 正在分析你的訓練資料…").assertIsDisplayed()
    }

    // --- Success 狀態 ---

    // 驗證 Success 狀態顯示整體評估與分析推理卡片內容
    @Test
    fun successState_showsSummaryAndReasoning() {
        val feedback = AiCoachFeedback(
            summary = "訓練穩定",
            reasoning = "胸肌訓練佔比最高",
            generatedAt = 0L
        )
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Success(feedback),
                    onRequestFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("整體評估").assertIsDisplayed()
        composeTestRule.onNodeWithText("訓練穩定").assertIsDisplayed()
        composeTestRule.onNodeWithText("分析推理").assertIsDisplayed()
        composeTestRule.onNodeWithText("胸肌訓練佔比最高").assertIsDisplayed()
    }

    // 驗證 reasoning 為空時，分析推理卡片不顯示
    @Test
    fun successState_hidesReasoningCardWhenBlank() {
        val feedback = AiCoachFeedback(
            summary = "訓練穩定",
            reasoning = "",
            generatedAt = 0L
        )
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Success(feedback),
                    onRequestFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("分析推理").assertDoesNotExist()
    }

    // 驗證 warnings 不為空時，注意事項卡片顯示所有項目
    @Test
    fun successState_showsWarnings() {
        val feedback = AiCoachFeedback(
            summary = "test",
            warnings = listOf("注意休息", "下肢訓練不足"),
            generatedAt = 0L
        )
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Success(feedback),
                    onRequestFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("注意事項").assertIsDisplayed()
        composeTestRule.onNodeWithText("• 注意休息").assertIsDisplayed()
        composeTestRule.onNodeWithText("• 下肢訓練不足").assertIsDisplayed()
    }

    // 驗證 warnings 為空時，注意事項卡片不顯示
    @Test
    fun successState_hidesWarningsCardWhenEmpty() {
        val feedback = AiCoachFeedback(
            summary = "test",
            warnings = emptyList(),
            generatedAt = 0L
        )
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Success(feedback),
                    onRequestFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("注意事項").assertDoesNotExist()
    }

    // 驗證建議動作區塊顯示動作名稱與肌群資訊
    @Test
    fun successState_showsRecommendedExercises() {
        val feedback = AiCoachFeedback(
            summary = "test",
            recommendedExercises = listOf(
                RecommendedExercise(
                    exercise = "深蹲",
                    muscleGroup = "下肢",
                    intensitySuggestion = "4 組 8-10 下"
                )
            ),
            generatedAt = 0L
        )
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Success(feedback),
                    onRequestFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("建議動作").assertIsDisplayed()
        composeTestRule.onNodeWithText("深蹲").assertIsDisplayed()
        composeTestRule.onNodeWithText("肌群：下肢").assertIsDisplayed()
        composeTestRule.onNodeWithText("4 組 8-10 下").assertIsDisplayed()
    }

    // 驗證 recommendedExercises 為空時，建議動作區塊不顯示
    @Test
    fun successState_hidesExercisesSectionWhenEmpty() {
        val feedback = AiCoachFeedback(
            summary = "test",
            recommendedExercises = emptyList(),
            generatedAt = 0L
        )
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Success(feedback),
                    onRequestFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("建議動作").assertDoesNotExist()
    }

    // 驗證 Success 狀態下點擊「重新分析」會觸發 onRequestFeedback callback
    @Test
    fun successState_clickReanalyzeButton_triggersCallback() {
        var clicked = false
        val feedback = AiCoachFeedback(summary = "test", generatedAt = 0L)
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Success(feedback),
                    onRequestFeedback = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("重新分析").performClick()

        assertTrue(clicked)
    }

    // --- Error 狀態 ---

    // 驗證 Error 狀態顯示錯誤標題與錯誤訊息
    @Test
    fun errorState_showsErrorTitleAndMessage() {
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Error("AI 服務今日使用量已達上限，請明天再試。"),
                    onRequestFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("發生錯誤").assertIsDisplayed()
        composeTestRule.onNodeWithText("AI 服務今日使用量已達上限，請明天再試。").assertIsDisplayed()
        composeTestRule.onNodeWithText("重試").assertIsDisplayed()
    }

    // 驗證 Error 狀態下點擊「重試」會觸發 onRequestFeedback callback
    @Test
    fun errorState_clickRetryButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            WorkoutLogTheme {
                AiCoachScreen(
                    uiState = AiCoachUiState.Error("錯誤訊息"),
                    onRequestFeedback = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("重試").performClick()

        assertTrue(clicked)
    }
}
