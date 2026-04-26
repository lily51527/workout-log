package idv.wennyli.workoutlog.ui.view.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.utils.AppResource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val appResource: AppResource
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "Registration failed", e)
                _authState.value = AuthState.Error(mapFirebaseError(e))
            } catch (e: Exception) {
                _authState.value = AuthState.Error(appResource.getString(R.string.error_auth_unknown))
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
                _authState.value = AuthState.Error(appResource.getString(R.string.error_auth_unknown))
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
                _authState.value = AuthState.Error(appResource.getString(R.string.error_auth_anonymous_failed))
            }
        }
    }

    private fun mapFirebaseError(e: FirebaseAuthException): String {
        return when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> appResource.getString(R.string.error_auth_invalid_email)
            "ERROR_WRONG_PASSWORD" -> appResource.getString(R.string.error_auth_wrong_password)
            "ERROR_USER_NOT_FOUND" -> appResource.getString(R.string.error_auth_user_not_found)
            "ERROR_USER_DISABLED" -> appResource.getString(R.string.error_auth_user_disabled)
            "ERROR_EMAIL_ALREADY_IN_USE" -> appResource.getString(R.string.error_auth_email_already_in_use)
            "ERROR_WEAK_PASSWORD" -> appResource.getString(R.string.error_auth_weak_password)
            else -> appResource.getString(R.string.error_auth_failed)
        }
    }
}
