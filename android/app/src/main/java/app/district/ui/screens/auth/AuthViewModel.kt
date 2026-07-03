package app.district.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.AuthErrors
import app.district.data.DistrictRepository
import app.district.data.PrefsManager
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null,
    val needsSetup: Boolean? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val prefs: PrefsManager,
    private val repo: DistrictRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = repo.authState.stateIn(
        viewModelScope, SharingStarted.Eagerly, repo.isLoggedIn()
    )

    val isSetupDone = prefs.isSetupDone.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    fun loginWithUsername(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                repo.loginWithUsername(username.trim(), password)
                syncCurrentAccount()
                _uiState.value = AuthUiState(loginSuccess = true, needsSetup = needsProfileSetup())
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = AuthErrors.message(e))
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                repo.loginWithGoogle(idToken)
                syncCurrentAccount()
                _uiState.value = AuthUiState(loginSuccess = true, needsSetup = needsProfileSetup())
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = AuthErrors.message(e))
            }
        }
    }

    fun loginWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                repo.loginWithPhoneCredential(credential)
                syncCurrentAccount()
                _uiState.value = AuthUiState(loginSuccess = true, needsSetup = needsProfileSetup())
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = AuthErrors.message(e))
            }
        }
    }

    fun reconcileCurrentSession() {
        viewModelScope.launch {
            try {
                if (repo.isLoggedIn()) syncCurrentAccount()
            } catch (_: Exception) {
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = AuthUiState()
    }

    fun showError(message: String) {
        _uiState.value = AuthUiState(error = message)
    }

    private suspend fun needsProfileSetup(): Boolean {
        val uid = repo.currentAccount()?.uid ?: return true
        val cloudComplete = runCatching { repo.hasCompletedProfile(uid) }.getOrDefault(false)
        val localDone = prefs.isSetupDone.first()
        return !cloudComplete && !localDone
    }

    private suspend fun syncCurrentAccount(loadCloud: Boolean = true) {
        val account = repo.currentAccount() ?: throw IllegalStateException("No authenticated account")
        prefs.switchAuthenticatedUser(account.uid, account.email, account.phoneNumber)
        if (loadCloud) {
            val cloudState = runCatching { repo.fetchAccountCloudState() }.getOrNull()
            val snapshot = cloudState?.snapshotJson.orEmpty()
            if (snapshot.isNotBlank()) {
                prefs.restoreAccountSnapshotJson(snapshot)
            }
        }
        val uid = account.uid
        if (runCatching { repo.hasCompletedProfile(uid) }.getOrDefault(false)) {
            prefs.setSetupDone(true)
        }
    }
}
