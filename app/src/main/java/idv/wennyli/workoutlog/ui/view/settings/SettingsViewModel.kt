package idv.wennyli.workoutlog.ui.view.settings

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
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val bodyMeasurementRepository: BodyMeasurementRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _measurements = MutableStateFlow<List<BodyMeasurement>>(emptyList())
    val measurements: StateFlow<List<BodyMeasurement>> = _measurements.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserProfile()
        loadBodyMeasurements()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userProfileRepository.getUserProfile().collect { userProfile ->
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
            bodyMeasurementRepository.getBodyMeasurements().collect { measurementList ->
                _measurements.value = measurementList
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