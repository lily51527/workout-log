package idv.wennyli.workoutlog.ui.view.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.wennyli.workoutlog.data.model.BodyMeasurement
import idv.wennyli.workoutlog.data.model.UserProfile
import idv.wennyli.workoutlog.data.repository.BodyMeasurementRepository
import idv.wennyli.workoutlog.data.repository.UserProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// 這個 data class 專門用於 SettingsScreen 的 UI 顯示
data class BodyMeasurementUiState(
    val id: String,
    val formattedDate: String, // 預先格式化好的日期字串
    val details: String // 預先組合好的身高體重等詳細資訊
)

private const val TAG = "SettingsViewModel"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val bodyMeasurementRepository: BodyMeasurementRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _measurementUiStateList =
        MutableStateFlow<List<BodyMeasurementUiState>>(emptyList())
    val measurementUiStateList: StateFlow<List<BodyMeasurementUiState>> =
        _measurementUiStateList.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserProfile()
        loadBodyMeasurements()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userProfileRepository.getUserProfile()
                .catch { exception ->
                    Log.e(TAG, "loadUserProfile error exception : $exception")
                    _error.value = "載入個人資料失敗: ${exception.message}"
                }
                .collect { userProfile ->
                    _userProfile.value = userProfile
                }
        }
    }

    fun updateUserProfile(gender: String? = null, birthDate: String? = null) {
        viewModelScope.launch {
            try {
                val currentProfile = _userProfile.value ?: UserProfile()
                val newProfile = currentProfile.copy(
                    gender = gender ?: currentProfile.gender,
                    birthDate = birthDate ?: currentProfile.birthDate
                )
                userProfileRepository.updateUserProfile(newProfile)
            } catch (e: Exception) {
                _error.value = "更新個人資料失敗: ${e.message}"
            }
        }
    }

    private fun loadBodyMeasurements() {
        viewModelScope.launch {
            bodyMeasurementRepository.getBodyMeasurements().map { measurements ->
                measurements.map { measurement -> measurement.toUiState() }
            }.catch { exception ->
                Log.e(TAG, "loadBodyMeasurements error exception : $exception")
                _error.value = "載入數據失敗: ${exception.message}"
            }.collect { measurementList ->
                _measurementUiStateList.value = measurementList
            }
        }
    }

    fun addBodyMeasurement(heightStr: String, weightStr: String, bodyFatStr: String) {
        val height = heightStr.toDoubleOrNull()
        val weight = weightStr.toDoubleOrNull()
        val bodyFat = bodyFatStr.toDoubleOrNull()

        if (height == null || height <= 0 || weight == null || weight <= 0) {
            _error.value = "請輸入有效的身高和體重"
            return
        }

        if (bodyFat != null && bodyFat < 0) {
            _error.value = "體脂肪率不能為負數。"
            return
        }

        viewModelScope.launch {
            try {
                val measurement =
                    BodyMeasurement(height = height, weight = weight, bodyFat = bodyFat)
                bodyMeasurementRepository.addBodyMeasurement(measurement)
            } catch (e: Exception) {
                _error.value = "新增失敗: ${e.message}"
            }
        }
    }

    fun deleteBodyMeasurement(measurementId: String) {
        viewModelScope.launch {
            try {
                bodyMeasurementRepository.deleteBodyMeasurement(measurementId)
            } catch (e: Exception) {
                _error.value = "刪除失敗: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// 擴充函式，將原始資料模型轉換為 UI 狀態模型
private fun BodyMeasurement.toUiState(): BodyMeasurementUiState {
    val formattedDate = this.timestamp?.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
    } ?: "N/A"

    val bodyFatText = this.bodyFat?.let { ", 體脂: $it%" } ?: ""
    val details = "身高: ${this.height} cm, 體重: ${this.weight} kg$bodyFatText"

    return BodyMeasurementUiState(
        id = this.id,
        formattedDate = formattedDate,
        details = details
    )
}