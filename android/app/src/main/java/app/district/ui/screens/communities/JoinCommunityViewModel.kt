package app.district.ui.screens.communities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.AuthErrors
import app.district.data.DistrictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinCommunityUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val joinedId: String? = null
)

@HiltViewModel
class JoinCommunityViewModel @Inject constructor(
    private val repo: DistrictRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(JoinCommunityUiState())
    val ui: StateFlow<JoinCommunityUiState> = _ui.asStateFlow()

    fun joinByCode(code: String) {
        viewModelScope.launch {
            _ui.value = JoinCommunityUiState(busy = true)
            runCatching {
                val community = repo.joinCommunityByCode(code.filter { it.isDigit() }.take(6))
                _ui.value = JoinCommunityUiState(joinedId = community.id)
            }.onFailure {
                _ui.value = JoinCommunityUiState(error = AuthErrors.message(it))
            }
        }
    }

    fun joinByName(name: String) {
        viewModelScope.launch {
            _ui.value = JoinCommunityUiState(busy = true)
            runCatching {
                val community = repo.joinCommunityByName(name.trim())
                _ui.value = JoinCommunityUiState(joinedId = community.id)
            }.onFailure {
                _ui.value = JoinCommunityUiState(error = AuthErrors.message(it))
            }
        }
    }

    fun clearError() {
        _ui.value = _ui.value.copy(error = null)
    }
}
