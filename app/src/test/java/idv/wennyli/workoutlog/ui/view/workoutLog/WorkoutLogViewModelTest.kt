package idv.wennyli.workoutlog.ui.view.workoutLog

import com.google.common.truth.Truth.assertThat
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.data.repository.WorkoutRepository
import idv.wennyli.workoutlog.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutLogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var viewModel: WorkoutLogViewModel

    @Before
    fun setup() {
        workoutRepository = mockk()
        every { workoutRepository.getWorkouts() } returns flowOf(emptyList())
        viewModel = WorkoutLogViewModel(workoutRepository)
    }

    // 驗證 ViewModel 初始化時會自動載入訓練資料，並在收到資料後將 loading 設為 false
    @Test
    fun `init should load workouts and set loading to false`() = runTest {
        // Given
        val fakeWorkouts = listOf(Workout(id = "1", exercise = "Push up"))
        every { workoutRepository.getWorkouts() } returns flowOf(fakeWorkouts)

        // When（覆寫 @Before 建立的 viewModel 以套用不同的 mock 資料）
        viewModel = WorkoutLogViewModel(workoutRepository)

        // Then
        assertThat(viewModel.workouts.value).isEqualTo(fakeWorkouts)
        assertThat(viewModel.loading.value).isFalse()
    }

    // 驗證呼叫 deleteWorkout() 時，會正確將 workoutId 傳遞給 repository 執行刪除
    @Test
    fun `deleteWorkout should call repository deleteWorkout`() = runTest {
        // Given
        coEvery { workoutRepository.deleteWorkout(any()) } returns Unit

        // When
        viewModel.deleteWorkout("workout123")

        // Then
        coVerify { workoutRepository.deleteWorkout("workout123") }
    }

    // 驗證 repository 拋出例外時，error state 會更新為包含錯誤訊息的字串
    @Test
    fun `deleteWorkout failure should update error state`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { workoutRepository.deleteWorkout(any()) } throws Exception(errorMessage)

        // When
        viewModel.deleteWorkout("workout123")

        // Then
        assertThat(viewModel.error.value).contains(errorMessage)
    }

    // 驗證 clearError() 會將 error state 重置為 null，清除畫面上的錯誤提示
    @Test
    fun `clearError should set error state to null`() = runTest {
        // Given
        coEvery { workoutRepository.deleteWorkout(any()) } throws Exception("error")
        viewModel.deleteWorkout("123")
        assertThat(viewModel.error.value).isNotNull()

        // When
        viewModel.clearError()

        // Then
        assertThat(viewModel.error.value).isNull()
    }

    // 驗證 setCurrentDate() 會更新 currentDate state，供 UI 顯示正確的日期
    @Test
    fun `setCurrentDate should update currentDate state`() = runTest {
        // Given
        val newDate = "2023-10-27"

        // When
        viewModel.setCurrentDate(newDate)

        // Then
        assertThat(viewModel.currentDate.value).isEqualTo(newDate)
    }

    // 驗證 ViewModel 初始化時，currentDate 的格式符合 yyyy-MM-dd
    @Test
    fun `initial currentDate should follow yyyy-MM-dd format`() = runTest {
        assertThat(viewModel.currentDate.value).matches("\\d{4}-\\d{2}-\\d{2}")
    }
}
