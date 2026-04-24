package idv.wennyli.workoutlog.ui.view.workoutLog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.data.repository.ExerciseRepository
import idv.wennyli.workoutlog.data.repository.WorkoutRepository
import idv.wennyli.workoutlog.ui.navigation.WorkoutDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    // 透過 SavedStateHandle 從導覽參數取得 workoutId（新增模式時為 null）
    private val workoutId: String? = savedStateHandle[WorkoutDestinations.WORKOUT_ID_ARG]
    val isEditMode: Boolean = workoutId != null

    // 預定義的訓練動作與肌群對應表
    val exerciseToMuscleMap = exerciseRepository.getExerciseToMuscleMap()

    private val _workoutToEdit = MutableStateFlow<Workout?>(null)
    val workoutToEdit: StateFlow<Workout?> = _workoutToEdit.asStateFlow()

    // 編輯模式下初始為 true，等資料載入後設為 false；新增模式直接為 false
    private val _isLoading = MutableStateFlow(isEditMode)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        if (isEditMode && workoutId != null) {
            loadWorkoutForEdit(workoutId)
        }
    }

    private fun loadWorkoutForEdit(id: String) {
        viewModelScope.launch {
            workoutRepository.getWorkouts().collect { workouts ->
                val found = workouts.find { it.id == id }
                if (found != null) {
                    _workoutToEdit.value = found
                    _isLoading.value = false
                }
            }
        }
    }

    fun saveWorkout(workout: Workout) {
        viewModelScope.launch {
            val original = _workoutToEdit.value
            if (isEditMode && original != null) {
                // 更新時保留原始的 id 與 userId
                workoutRepository.updateWorkout(
                    workout.copy(id = original.id, userId = original.userId)
                )
            } else {
                workoutRepository.addWorkout(workout)
            }
        }
    }
}
