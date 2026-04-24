package idv.wennyli.workoutlog.ui.view.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.data.repository.ExerciseRepository
import idv.wennyli.workoutlog.data.repository.WorkoutRepository
import idv.wennyli.workoutlog.utils.toFormattedDate
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

enum class TimerState {
    IDLE, WORKING, RESTING, FINISHED
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    // 儲存使用者輸入的運動項目名稱
    private val _exerciseName = MutableStateFlow("")
    val exerciseName: StateFlow<String> = _exerciseName.asStateFlow()

    // 儲存使用者設定的總組數
    private val _totalSets = MutableStateFlow(3)
    val totalSets: StateFlow<Int> = _totalSets.asStateFlow()

    // 儲存使用者設定的組間休息時間（秒）
    private val _restTime = MutableStateFlow(60)
    val restTime: StateFlow<Int> = _restTime.asStateFlow()

    // 追蹤目前進行到第幾組
    private val _currentSet = MutableStateFlow(0)
    val currentSet: StateFlow<Int> = _currentSet.asStateFlow()

    // 追蹤休息時間的剩餘秒數
    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    // 追蹤計時器的整體狀態（閒置、進行中、休息中、已完成）
    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    // 用於顯示儲存成功後的提示訊息
    private val _showSaveConfirmation = MutableStateFlow(false)
    val showSaveConfirmation: StateFlow<Boolean> = _showSaveConfirmation.asStateFlow()

    fun onExerciseNameChange(exerciseName: String) {
        _exerciseName.value = exerciseName
    }

    fun onTotalSetsChange(totalSets: Int) {
        _totalSets.value = totalSets
    }

    fun onRestTimeChange(restTime: Int) {
        _restTime.value = restTime
    }

    fun setTimeLeft(timeLeft: Int) {
        _timeLeft.value = timeLeft
    }

    fun setTimerState(timerState: TimerState) {
        _timerState.value = timerState
    }

    fun onStartSet() {
        if (_currentSet.value < _totalSets.value) {
            _currentSet.value++
            _timerState.value = TimerState.WORKING
        }
    }

    fun onStartRest() {
        if (_timerState.value == TimerState.WORKING) {
            _timeLeft.value = _restTime.value
            _timerState.value = TimerState.RESTING
        }
    }

    fun onSkipRest() {
        _timeLeft.value = 0
        _timerState.value =
            if (_currentSet.value < _totalSets.value) TimerState.WORKING else TimerState.FINISHED
    }

    fun onReset() {
        _exerciseName.value = ""
        _totalSets.value = 3
        _restTime.value = 60
        _currentSet.value = 0
        _timeLeft.value = 0
        _timerState.value = TimerState.IDLE
        _showSaveConfirmation.value = false
    }

    fun saveWorkout() {
        viewModelScope.launch {
            if (_exerciseName.value.isBlank() || _currentSet.value == 0) return@launch

            val workout = Workout(
                date = Date().toFormattedDate(),
                exercise = _exerciseName.value,
                muscleGroup = exerciseRepository.getExerciseToMuscleMap()[_exerciseName.value] ?: "",
                sets = _currentSet.value,
                reps = 8,
                repsUnit = "次數",
                weight = 0.0,
                muscleFeel = 3,
                control = 3,
                notes = "從計時器完成"
            )
            workoutRepository.addWorkout(workout)
            _showSaveConfirmation.value = true // 顯示儲存成功提示
        }
    }
}