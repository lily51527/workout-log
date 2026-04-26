package idv.wennyli.workoutlog.ui.view.aiCoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.wennyli.workoutlog.data.model.AiCoachFeedback
import idv.wennyli.workoutlog.data.repository.AiCoachRepository
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
    private val aiCoachRepository: AiCoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiCoachUiState>(AiCoachUiState.Idle)
    val uiState: StateFlow<AiCoachUiState> = _uiState.asStateFlow()

    fun getFeedback() {
        viewModelScope.launch {
            _uiState.value = AiCoachUiState.Loading
            try {
                val feedback = aiCoachRepository.getFeedback()
                _uiState.value = AiCoachUiState.Success(feedback)
            } catch (e: Exception) {
                _uiState.value = AiCoachUiState.Error(e.message ?: "未知錯誤")
            }
        }
    }
}
