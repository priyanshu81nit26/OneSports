package app.district.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.AccountType
import app.district.data.AuthErrors
import app.district.data.DeviceProfile
import app.district.data.DeviceProfileStore
import app.district.data.DistrictRepository
import app.district.data.PrefsManager
import app.district.data.ProfileRole
import app.district.data.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingDraft(
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirthMillis: Long = 0L,
    val username: String = ""
)

data class OnboardingUiState(
    val draft: OnboardingDraft = OnboardingDraft(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val usernameChecking: Boolean = false,
    val usernameAvailable: Boolean? = null,
    val completed: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: PrefsManager,
    private val repo: DistrictRepository,
    private val profileStore: DeviceProfileStore
) : ViewModel() {

    private val _ui = MutableStateFlow(OnboardingUiState())
    val ui: StateFlow<OnboardingUiState> = _ui.asStateFlow()

    fun saveDetails(firstName: String, lastName: String, dateOfBirthMillis: Long) {
        _ui.update {
            it.copy(
                draft = it.draft.copy(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    dateOfBirthMillis = dateOfBirthMillis
                ),
                error = null
            )
        }
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }

    fun checkUsername(raw: String) {
        val username = PrefsManager.normalizeUsername(raw).take(20)
        _ui.update { it.copy(draft = it.draft.copy(username = username), error = null) }
        if (username.length < 6) {
            _ui.update { it.copy(usernameAvailable = null) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(usernameChecking = true, error = null) }
            val available = runCatching { repo.isUsernameAvailable(username) }.getOrDefault(false)
            _ui.update {
                it.copy(
                    usernameChecking = false,
                    usernameAvailable = available,
                    error = if (!available) "Username is already taken" else null
                )
            }
        }
    }

    fun confirmUsername(raw: String): Boolean {
        val username = PrefsManager.normalizeUsername(raw).take(20)
        if (username.length < 6) {
            _ui.update { it.copy(error = "Username must be at least 6 characters") }
            return false
        }
        if (_ui.value.usernameAvailable != true) {
            _ui.update { it.copy(error = "Check username availability first") }
            return false
        }
        _ui.update { it.copy(draft = it.draft.copy(username = username), error = null) }
        return true
    }

    fun completeSignup(password: String, confirmPassword: String) {
        viewModelScope.launch {
            val draft = _ui.value.draft
            val username = draft.username
            if (username.length < 6) {
                _ui.update { it.copy(error = "Username must be at least 6 characters") }
                return@launch
            }
            if (draft.firstName.isBlank() || draft.lastName.isBlank() || draft.dateOfBirthMillis <= 0L) {
                _ui.update { it.copy(error = "Complete your name and date of birth first") }
                return@launch
            }
            if (password.length < 6) {
                _ui.update { it.copy(error = "Password must be at least 6 characters") }
                return@launch
            }
            if (password != confirmPassword) {
                _ui.update { it.copy(error = "Passwords do not match") }
                return@launch
            }
            _ui.update { it.copy(isLoading = true, error = null) }
            try {
                val account = repo.currentAccount() ?: throw IllegalStateException("Sign in first")
                val authEmail = "${username.lowercase()}@users.rise.app"
                if (!repo.isUsernameAvailable(username)) {
                    throw IllegalStateException("Username is already taken")
                }
                repo.linkEmailPassword(authEmail, password)
                repo.claimUsername(account.uid, username)
                val profile = UserProfile(
                    uid = account.uid,
                    username = username,
                    firstName = draft.firstName,
                    lastName = draft.lastName,
                    dateOfBirthMillis = draft.dateOfBirthMillis,
                    bio = "",
                    photoUri = "",
                    usernameChangedAt = System.currentTimeMillis()
                )
                repo.saveUserProfile(profile)
                prefs.setUsername(username)
                prefs.setAccountType(AccountType.INDIVIDUAL)
                prefs.createProfile(
                    role = ProfileRole.USER,
                    name = profile.displayName.ifBlank { draft.firstName },
                    age = 0,
                    avatar = "",
                    photoUri = "",
                    username = username,
                    blockedApps = emptySet(),
                    makeActive = true
                )
                prefs.setSetupDone(true)
                runCatching { repo.saveAccountSnapshot(prefs.exportAccountSnapshotJson()) }
                profileStore.upsert(
                    DeviceProfile(
                        uid = account.uid,
                        username = username,
                        displayName = profile.displayName,
                        photoUri = ""
                    )
                )
                _ui.update { it.copy(isLoading = false, completed = true) }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = AuthErrors.message(e)) }
            }
        }
    }
}
