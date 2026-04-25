package idv.wennyli.workoutlog.ui.view.workoutLog

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.data.repository.ExerciseRepository
import idv.wennyli.workoutlog.data.repository.WorkoutRepository
import idv.wennyli.workoutlog.ui.navigation.WorkoutDestinations
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
class AddWorkoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var exerciseRepository: ExerciseRepository

    @Before
    fun setup() {
        workoutRepository = mockk()
        exerciseRepository = mockk()
        every { exerciseRepository.getExerciseToMuscleMap() } returns emptyMap()
    }

    private fun createViewModel(workoutId: String? = null): AddWorkoutViewModel {
        val savedStateHandle = if (workoutId != null) {
            SavedStateHandle(mapOf(WorkoutDestinations.WORKOUT_ID_ARG to workoutId))
        } else {
            SavedStateHandle()
        }
        return AddWorkoutViewModel(savedStateHandle, workoutRepository, exerciseRepository)
    }

    // 新增模式：workoutId 為 null 時，isEditMode 應為 false
    @Test
    fun `add mode - isEditMode should be false when no workoutId`() = runTest {
        every { workoutRepository.getWorkouts() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        assertThat(viewModel.isEditMode).isFalse()
    }

    // 新增模式：isLoading 初始值應為 false（不需等待資料載入）
    @Test
    fun `add mode - isLoading should be false initially`() = runTest {
        every { workoutRepository.getWorkouts() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        assertThat(viewModel.isLoading.value).isFalse()
    }

    // 新增模式：workoutToEdit 初始值應為 null
    @Test
    fun `add mode - workoutToEdit should be null initially`() = runTest {
        every { workoutRepository.getWorkouts() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        assertThat(viewModel.workoutToEdit.value).isNull()
    }

    // 新增模式：saveWorkout 應呼叫 addWorkout，不應呼叫 updateWorkout
    @Test
    fun `add mode - saveWorkout should call addWorkout`() = runTest {
        every { workoutRepository.getWorkouts() } returns flowOf(emptyList())
        coEvery { workoutRepository.addWorkout(any()) } returns Unit
        val viewModel = createViewModel()
        val newWorkout = Workout(exercise = "深蹲")

        viewModel.saveWorkout(newWorkout)

        coVerify { workoutRepository.addWorkout(newWorkout) }
        coVerify(exactly = 0) { workoutRepository.updateWorkout(any()) }
    }

    // 編輯模式：workoutId 存在時，isEditMode 應為 true
    @Test
    fun `edit mode - isEditMode should be true when workoutId exists`() = runTest {
        every { workoutRepository.getWorkouts() } returns flowOf(emptyList())
        val viewModel = createViewModel(workoutId = "workout123")

        assertThat(viewModel.isEditMode).isTrue()
    }

    // 編輯模式：isLoading 初始值應為 true（等待資料載入）
    @Test
    fun `edit mode - isLoading should be true initially`() = runTest {
        every { workoutRepository.getWorkouts() } returns flowOf(emptyList())
        val viewModel = createViewModel(workoutId = "workout123")

        assertThat(viewModel.isLoading.value).isTrue()
    }

    // 編輯模式：找到對應 workout 後，workoutToEdit 應更新且 isLoading 設為 false
    @Test
    fun `edit mode - should load workout and set isLoading to false when found`() = runTest {
        val targetWorkout = Workout(id = "workout123", exercise = "臥推", userId = "user1")
        every { workoutRepository.getWorkouts() } returns flowOf(listOf(targetWorkout))
        val viewModel = createViewModel(workoutId = "workout123")

        assertThat(viewModel.workoutToEdit.value).isEqualTo(targetWorkout)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    // 編輯模式：找不到對應 workout 時，workoutToEdit 應維持 null，isLoading 應維持 true
    @Test
    fun `edit mode - should keep isLoading true when workout not found`() = runTest {
        every { workoutRepository.getWorkouts() } returns flowOf(listOf(Workout(id = "other")))
        val viewModel = createViewModel(workoutId = "workout123")

        assertThat(viewModel.workoutToEdit.value).isNull()
        assertThat(viewModel.isLoading.value).isTrue()
    }

    // 編輯模式：saveWorkout 應保留原始 id 與 userId，呼叫 updateWorkout
    @Test
    fun `edit mode - saveWorkout should call updateWorkout with original id and userId`() = runTest {
        val original = Workout(id = "workout123", exercise = "臥推", userId = "user1")
        every { workoutRepository.getWorkouts() } returns flowOf(listOf(original))
        coEvery { workoutRepository.updateWorkout(any()) } returns Unit
        val viewModel = createViewModel(workoutId = "workout123")
        val updated = Workout(exercise = "深蹲", userId = "newUser")

        viewModel.saveWorkout(updated)

        coVerify {
            workoutRepository.updateWorkout(
                updated.copy(id = original.id, userId = original.userId)
            )
        }
        coVerify(exactly = 0) { workoutRepository.addWorkout(any()) }
    }

    // 驗證 exerciseToMuscleMap 回傳 exerciseRepository 提供的對應表
    @Test
    fun `exerciseToMuscleMap should return data from exerciseRepository`() = runTest {
        val fakeMap = mapOf("臥推" to "胸大肌")
        every { exerciseRepository.getExerciseToMuscleMap() } returns fakeMap
        every { workoutRepository.getWorkouts() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        assertThat(viewModel.exerciseToMuscleMap).isEqualTo(fakeMap)
    }
}
