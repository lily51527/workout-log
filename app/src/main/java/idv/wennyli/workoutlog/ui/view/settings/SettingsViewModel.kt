package idv.wennyli.workoutlog.ui.view.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.wennyli.workoutlog.data.model.UserProfile
import idv.wennyli.workoutlog.data.repository.UserProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userProfileRepository.getUserProfile().collect {
                _userProfile.value = it
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

    fun clearError() {
        _error.value = null
    }
}