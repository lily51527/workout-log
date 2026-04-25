package idv.wennyli.workoutlog.ui.view.timer

import com.google.common.truth.Truth.assertThat
import idv.wennyli.workoutlog.data.repository.ExerciseRepository
import idv.wennyli.workoutlog.data.repository.WorkoutRepository
import idv.wennyli.workoutlog.ui.view.timer.TimerState
import idv.wennyli.workoutlog.ui.view.timer.TimerViewModel
import idv.wennyli.workoutlog.utils.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockWorkoutRepository: WorkoutRepository
    private lateinit var mockExerciseRepository: ExerciseRepository
    private lateinit var viewModel: TimerViewModel

    @Before
    fun setup() {
        mockWorkoutRepository = mockk(relaxed = true)
        mockExerciseRepository = mockk(relaxed = true)
        viewModel = TimerViewModel(mockWorkoutRepository, mockExerciseRepository)
    }

    // 驗證 ViewModel 初始化後，狀態為 IDLE 且所有欄位皆為預設值
    @Test
    fun `initial state should be IDLE with default values`() {
        assertThat(viewModel.timerState.value).isEqualTo(TimerState.IDLE)
        assertThat(viewModel.exerciseName.value).isEmpty()
        assertThat(viewModel.totalSets.value).isEqualTo(3)
        assertThat(viewModel.restTime.value).isEqualTo(60)
        assertThat(viewModel.currentSet.value).isEqualTo(0)
        assertThat(viewModel.timeLeft.value).isEqualTo(0)
        assertThat(viewModel.showSaveConfirmation.value).isFalse()
    }

    // 驗證呼叫 onExerciseNameChange 後，exerciseName 狀態正確更新
    @Test
    fun `onExerciseNameChange should update exerciseName`() {
        val newName = "Bench Press"
        viewModel.onExerciseNameChange(newName)
        assertThat(viewModel.exerciseName.value).isEqualTo(newName)
    }

    // 驗證呼叫 onTotalSetsChange 後，totalSets 狀態正確更新
    @Test
    fun `onTotalSetsChange should update totalSets`() {
        val newSets = 5
        viewModel.onTotalSetsChange(newSets)
        assertThat(viewModel.totalSets.value).isEqualTo(newSets)
    }

    // 驗證呼叫 onRestTimeChange 後，restTime 狀態正確更新
    @Test
    fun `onRestTimeChange should update restTime`() {
        val newRest = 90
        viewModel.onRestTimeChange(newRest)
        assertThat(viewModel.restTime.value).isEqualTo(newRest)
    }

    // 驗證呼叫 onStartSet 後，currentSet 遞增且計時狀態切換為 WORKING
    @Test
    fun `onStartSet should increment currentSet and set state to WORKING`() {
        viewModel.onStartSet()
        assertThat(viewModel.currentSet.value).isEqualTo(1)
        assertThat(viewModel.timerState.value).isEqualTo(TimerState.WORKING)
    }

    // 驗證 onStartSet 不會讓 currentSet 超過 totalSets 上限
    @Test
    fun `onStartSet should not exceed totalSets`() {
        viewModel.onTotalSetsChange(1)
        viewModel.onStartSet()
        viewModel.onStartSet() // Should not increment beyond totalSets
        assertThat(viewModel.currentSet.value).isEqualTo(1)
    }

    // 驗證在 WORKING 狀態下呼叫 onStartRest，計時狀態切換為 RESTING 並以 restTime 初始化 timeLeft
    @Test
    fun `onStartRest should set state to RESTING and initialize timeLeft`() {
        viewModel.onStartSet() // Sets state to WORKING
        viewModel.onRestTimeChange(45)
        viewModel.onStartRest()

        assertThat(viewModel.timerState.value).isEqualTo(TimerState.RESTING)
        assertThat(viewModel.timeLeft.value).isEqualTo(45)
    }

    // 驗證非 WORKING 狀態下呼叫 onStartRest，狀態與 timeLeft 皆不改變
    @Test
    fun `onStartRest should do nothing if state is not WORKING`() {
        viewModel.onStartRest()
        assertThat(viewModel.timerState.value).isEqualTo(TimerState.IDLE)
        assertThat(viewModel.timeLeft.value).isEqualTo(0)
    }

    // 驗證還有剩餘組數時，onSkipRest 清零 timeLeft 並切換回 WORKING 狀態
    @Test
    fun `onSkipRest should set timeLeft to 0 and transition to WORKING if sets remain`() {
        viewModel.onTotalSetsChange(2)
        viewModel.onStartSet() // currentSet = 1, state = WORKING
        viewModel.onStartRest() // state = RESTING, timeLeft = 60

        viewModel.onSkipRest()

        assertThat(viewModel.timeLeft.value).isEqualTo(0)
        assertThat(viewModel.timerState.value).isEqualTo(TimerState.WORKING)
    }

    // 驗證所有組數已完成時，onSkipRest 將狀態切換為 FINISHED
    @Test
    fun `onSkipRest should set state to FINISHED if no sets remain`() {
        viewModel.onTotalSetsChange(1)
        viewModel.onStartSet() // currentSet = 1, state = WORKING
        viewModel.onStartRest() // state = RESTING, timeLeft = 60

        viewModel.onSkipRest()

        assertThat(viewModel.timerState.value).isEqualTo(TimerState.FINISHED)
    }

    // 驗證呼叫 onReset 後，所有欄位恢復為初始預設值
    @Test
    fun `onReset should restore all initial values`() {
        viewModel.onExerciseNameChange("Squat")
        viewModel.onStartSet()
        viewModel.onReset()

        assertThat(viewModel.exerciseName.value).isEmpty()
        assertThat(viewModel.currentSet.value).isEqualTo(0)
        assertThat(viewModel.timerState.value).isEqualTo(TimerState.IDLE)
        assertThat(viewModel.totalSets.value).isEqualTo(3)
        assertThat(viewModel.restTime.value).isEqualTo(60)
        assertThat(viewModel.timeLeft.value).isEqualTo(0)
        assertThat(viewModel.showSaveConfirmation.value).isFalse()
    }

    // 驗證儲存訓練時呼叫 repository，並在成功後顯示確認提示
    @Test
    fun `saveWorkout should call repository and show confirmation on success`() = runTest {
        every { mockExerciseRepository.getExerciseToMuscleMap() } returns
                mapOf("臥推" to "胸大肌 (中束), 三角肌 (前束), 三頭肌")

        viewModel.onExerciseNameChange("臥推")
        viewModel.onStartSet() // currentSet = 1

        viewModel.saveWorkout()

        coVerify {
            mockWorkoutRepository.addWorkout(match {
                it.exercise == "臥推" &&
                        it.muscleGroup.contains("胸大肌") &&
                        it.sets == 1
            })
        }
        assertThat(viewModel.showSaveConfirmation.value).isTrue()
    }

    // 驗證 exerciseName 為空字串時，saveWorkout 不呼叫 repository 也不顯示確認提示
    @Test
    fun `saveWorkout should not call repository if exerciseName is blank`() = runTest {
        viewModel.onExerciseNameChange("")
        viewModel.onStartSet()

        viewModel.saveWorkout()

        coVerify(exactly = 0) { mockWorkoutRepository.addWorkout(any()) }
        assertThat(viewModel.showSaveConfirmation.value).isFalse()
    }

    // 驗證 currentSet 為 0（尚未開始任何組數）時，saveWorkout 不呼叫 repository 也不顯示確認提示
    @Test
    fun `saveWorkout should not call repository if currentSet is 0`() = runTest {
        viewModel.onExerciseNameChange("臥推")
        // currentSet is 0 by default

        viewModel.saveWorkout()

        coVerify(exactly = 0) { mockWorkoutRepository.addWorkout(any()) }
        assertThat(viewModel.showSaveConfirmation.value).isFalse()
    }
}
