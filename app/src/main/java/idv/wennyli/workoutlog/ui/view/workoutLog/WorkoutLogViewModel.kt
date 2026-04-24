package idv.wennyli.workoutlog.ui.view.workoutLog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.data.repository.ExerciseRepository
import idv.wennyli.workoutlog.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WorkoutLogViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentDate = MutableStateFlow<String>(getCurrentDateString())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // 預定義的訓練動作與肌群對應表
    val exerciseToMuscleMap = exerciseRepository.getExerciseToMuscleMap()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            _loading.value = true
            workoutRepository.getWorkouts().collect { workouts ->
                _workouts.value = workouts
                _loading.value = false
            }
        }
    }

    fun addWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                workoutRepository.addWorkout(workout)
            } catch (e: Exception) {
                _error.value = "新增訓練失敗: ${e.message}"
            }
        }
    }

    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(workout)
            } catch (e: Exception) {
                _error.value = "更新訓練失敗: ${e.message}"
            }
        }
    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workoutId)
            } catch (e: Exception) {
                _error.value = "刪除訓練失敗: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun setCurrentDate(date: String) {
        _currentDate.value = date
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}