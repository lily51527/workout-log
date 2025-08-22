package idv.wennyli.workoutlog.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// 定義驗證狀態的密封類別
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "Registration failed", e)
                _authState.value = AuthState.Error(mapFirebaseError(e))
            } catch (e: Exception) {
                _authState.value = AuthState.Error("發生未知錯誤: ${e.message}")
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "Sign in failed", e)
                _authState.value = AuthState.Error(mapFirebaseError(e))
            } catch (e: Exception) {
                _authState.value = AuthState.Error("發生未知錯誤: ${e.message}")
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInAnonymously().await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error("匿名登入失敗: ${e.message}")
            }
        }
    }

    // TODO:這段來源待驗證
    private fun mapFirebaseError(e: FirebaseAuthException): String {
        return when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "電子郵件格式不正確。"
            "ERROR_WRONG_PASSWORD" -> "密碼錯誤。"
            "ERROR_USER_NOT_FOUND" -> "找不到此用戶。"
            "ERROR_USER_DISABLED" -> "此用戶已被停用。"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "此電子郵件已被註冊。"
            "ERROR_WEAK_PASSWORD" -> "密碼強度不足，請至少設定 6 個字元。"
            else -> "驗證失敗: ${e.message}"
        }
    }
}