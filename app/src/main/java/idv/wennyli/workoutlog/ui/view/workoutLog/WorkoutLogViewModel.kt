package idv.wennyli.workoutlog.ui.view.workoutLog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.wennyli.workoutlog.data.model.Workout
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
    val exerciseToMuscleMap = createExerciseToMuscleMap()

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

    private fun createExerciseToMuscleMap(): Map<String, String> {
        return mapOf(
            "臥推" to "胸大肌 (中束), 三角肌 (前束), 三頭肌",
            "划船" to "闊背肌, 斜方肌, 菱形肌, 二頭肌",
            "深蹲" to "股四頭肌, 臀大肌, 股二頭肌",
            "硬舉" to "臀大肌, 股二頭肌, 豎脊肌, 闊背肌",
            "肩推" to "三角肌 (前束, 中束), 三頭肌",
            "二頭彎舉" to "二頭肌",
            "三頭下壓" to "三頭肌",
            "引體向上" to "闊背肌, 二頭肌, 斜方肌",
            "腿推" to "股四頭肌, 臀大肌, 股二頭肌",
            "腿彎舉" to "股二頭肌",
            "腿伸展" to "股四頭肌",
            "側平舉" to "三角肌 (中束)",
            "飛鳥" to "胸大肌 (中束), 三角肌 (前束)",
            "羅馬尼亞硬舉" to "股二頭肌, 臀大肌, 豎脊肌",
            "農夫走路" to "核心肌群, 前臂肌群, 股四頭肌, 臀大肌",
            "平板支撐" to "腹直肌, 腹斜肌, 豎脊肌",
            "捲腹" to "腹直肌",
            "小腿提踵" to "腓腸肌, 比目魚肌",
            "上斜胸推" to "胸大肌 (上束), 三角肌 (前束), 三頭肌",
            "水平胸推" to "胸大肌 (中束), 三角肌 (前束), 三頭肌",
            "下斜胸推" to "胸大肌 (下束), 三角肌 (前束), 三頭肌",
            "蝴蝶機(夾胸)" to "胸大肌 (中束)",
            "反向飛鳥" to "三角肌 (後束), 斜方肌, 菱形肌",
            "保加利雅分腿蹲" to "股四頭肌, 臀大肌, 股二頭肌",
            "仰臥踢腿" to "腹直肌 (下腹), 髂腰肌",
            "屈膝捲腹" to "腹直肌",
            "滑輪下拉" to "闊背肌, 二頭肌",
            "棒式" to "腹直肌, 腹斜肌, 豎脊肌",
            "側棒式" to "腹斜肌, 臀中肌",
            "哈克深蹲" to "股四頭肌, 臀大肌",
            "爬梯機" to "股四頭肌, 臀大肌, 股二頭肌, 小腿肌群",
            "45度腿推" to "股四頭肌, 臀大肌, 股二頭肌"
        )
    }
}