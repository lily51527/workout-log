package idv.wennyli.workoutlog.ui.view.aiCoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.data.model.AiCoachException
import idv.wennyli.workoutlog.data.model.AiCoachFeedback
import idv.wennyli.workoutlog.data.repository.AiCoachRepository
import idv.wennyli.workoutlog.utils.AppResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AiCoachUiState {
    object Idle : AiCoachUiState()
    object Loading : AiCoachUiState()
    data class Success(val feedback: AiCoachFeedback) : AiCoachUiState()
    data class Error(val message: String) : AiCoachUiState()
}

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val aiCoachRepository: AiCoachRepository,
    private val appResource: AppResource
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiCoachUiState>(AiCoachUiState.Idle)
    val uiState: StateFlow<AiCoachUiState> = _uiState.asStateFlow()

    fun getFeedback() {
        if (_uiState.value is AiCoachUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = AiCoachUiState.Loading
            try {
                val feedback = aiCoachRepository.getFeedback()
                _uiState.value = AiCoachUiState.Success(feedback)
            } catch (e: AiCoachException) {
                val message = when (e) {
                    is AiCoachException.QuotaExceeded -> appResource.getString(R.string.error_ai_quota_exceeded)
                    is AiCoachException.Unauthenticated -> appResource.getString(R.string.error_ai_unauthenticated)
                    is AiCoachException.Unavailable -> appResource.getString(R.string.error_ai_unavailable)
                    is AiCoachException.Timeout -> appResource.getString(R.string.error_ai_timeout)
                    is AiCoachException.Internal -> appResource.getString(R.string.error_ai_internal)
                    is AiCoachException.Unknown -> appResource.getString(R.string.error_ai_unknown)
                }
                _uiState.value = AiCoachUiState.Error(message)
            }
        }
    }
}
