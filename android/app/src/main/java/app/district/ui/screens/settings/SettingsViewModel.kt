package app.district.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.AccountCloudState
import app.district.data.DistrictRepository
import app.district.data.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PrefsManager,
    private val repo: DistrictRepository
) : ViewModel() {

    private val _pinVerified = MutableStateFlow(false)
    val pinVerified: StateFlow<Boolean> = _pinVerified

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError

    private val _deleteState = MutableStateFlow<AccountCloudState?>(null)
    val deleteState: StateFlow<AccountCloudState?> = _deleteState

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    init {
        _pinVerified.value = true
        refreshDeleteState()
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            if (prefs.verifyUserPin(pin)) {
                _pinVerified.value = true
                _pinError.value = null
            } else {
                _pinError.value = "Incorrect PIN"
            }
        }
    }

    fun logoutAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                runCatching { repo.saveAccountSnapshot(prefs.exportAccountSnapshotJson()) }
                prefs.clearAuthenticatedUser()
                repo.logout()
            } catch (_: Exception) { }
            onDone()
        }
    }

    fun requestAccountDeletion(pin: String, confirmPin: String, reason: String, typedDelete: String) {
        viewModelScope.launch {
            when {
                pin.length < 4 -> _actionMessage.value = "Enter your PIN"
                pin != confirmPin -> _actionMessage.value = "PIN confirmation does not match"
                reason.trim().length < 3 -> _actionMessage.value = "Tell us why you're leaving"
                typedDelete.trim().lowercase() != "delete" -> _actionMessage.value = "Type delete to confirm"
                !prefs.verifyUserPin(pin) -> _actionMessage.value = "Incorrect PIN"
                else -> {
                    val deleteAfter = System.currentTimeMillis() + DELETE_COOLDOWN_MS
                    repo.requestAccountDeletion(reason, deleteAfter)
                    refreshDeleteState()
                    _actionMessage.value = "Account deletion scheduled. You can cancel within 15 days."
                }
            }
        }
    }

    fun cancelAccountDeletion() {
        viewModelScope.launch {
            repo.cancelAccountDeletion()
            refreshDeleteState()
            _actionMessage.value = "Account revived. Deletion request cancelled."
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    private fun refreshDeleteState() {
        viewModelScope.launch {
            _deleteState.value = runCatching { repo.fetchAccountCloudState() }.getOrNull()
        }
    }

    companion object {
        private const val DELETE_COOLDOWN_MS = 15L * 24L * 60L * 60L * 1000L
    }
}
