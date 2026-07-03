package app.district.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.AuthErrors
import app.district.data.DistrictRepository
import app.district.data.PrefsManager
import app.district.data.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val profile: UserProfile? = null,
    val canChangeUsername: Boolean = true,
    val usernameDaysLeft: Int = 0
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repo: DistrictRepository,
    private val prefs: PrefsManager
) : ViewModel() {

    private val _ui = MutableStateFlow(EditProfileUiState())
    val ui: StateFlow<EditProfileUiState> = _ui.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val uid = repo.currentAccount()?.uid ?: throw IllegalStateException("Not logged in")
                val profile = repo.fetchUserProfile(uid) ?: UserProfile(uid = uid)
                val changedAt = profile.usernameChangedAt
                val cooldownMs = 15L * 24 * 60 * 60 * 1000
                val elapsed = System.currentTimeMillis() - changedAt
                val canChange = changedAt <= 0L || elapsed >= cooldownMs
                val daysLeft = if (canChange) 0 else ((cooldownMs - elapsed) / (24 * 60 * 60 * 1000)).toInt() + 1
                _ui.value = _ui.value.copy(
                    loading = false,
                    profile = profile,
                    canChangeUsername = canChange,
                    usernameDaysLeft = daysLeft
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = AuthErrors.message(e))
            }
        }
    }

    fun save(username: String, bio: String, photoUri: String) {
        viewModelScope.launch {
            val current = _ui.value.profile ?: return@launch
            _ui.value = _ui.value.copy(saving = true, error = null, saved = false)
            try {
                val cleanUsername = PrefsManager.normalizeUsername(username)
                if (cleanUsername.length < 6) throw IllegalStateException("Username must be at least 6 characters")
                if (cleanUsername != current.username) {
                    if (!_ui.value.canChangeUsername) {
                        throw IllegalStateException("You can change username again in ${_ui.value.usernameDaysLeft} days")
                    }
                    if (!repo.isUsernameAvailable(cleanUsername)) {
                        throw IllegalStateException("Username is already taken")
                    }
                    repo.updateUsername(current.uid, cleanUsername)
                    prefs.setUsername(cleanUsername)
                }
                val updated = current.copy(
                    username = cleanUsername,
                    bio = bio.trim(),
                    photoUri = photoUri
                )
                repo.saveUserProfile(updated)
                _ui.value = _ui.value.copy(saving = false, saved = true, profile = updated)
                refresh()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(saving = false, error = AuthErrors.message(e))
            }
        }
    }

    fun clearMessages() {
        _ui.value = _ui.value.copy(error = null, saved = false)
    }
}
