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
                val message = when {
                    e.message?.contains("RESOURCE_EXHAUSTED") == true -> "AI 服務今日使用量已達上限，請明天再試"
                    e.message?.contains("UNAUTHENTICATED") == true -> "請先登入才能使用 AI 教練"
                    e.message?.contains("UNAVAILABLE") == true -> "無法連線至伺服器，請檢查網路後重試"
                    e.message?.contains("DEADLINE_EXCEEDED") == true -> "請求逾時，請稍後再試"
                    e.message?.contains("INTERNAL") == true -> "伺服器發生錯誤，請稍後再試"
                    else -> "無法取得 AI 回饋，請稍後再試"
                }
                _uiState.value = AiCoachUiState.Error(message)
            }
        }
    }
}
