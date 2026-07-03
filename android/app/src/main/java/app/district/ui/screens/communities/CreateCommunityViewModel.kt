package app.district.ui.screens.communities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.AuthErrors
import app.district.data.CreateCommunityRequest
import app.district.data.DistrictRepository
import app.district.data.CommunityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateCommunityUiState(
    val saving: Boolean = false,
    val error: String? = null,
    val createdId: String? = null
)

@HiltViewModel
class CreateCommunityViewModel @Inject constructor(
    private val repo: DistrictRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(CreateCommunityUiState())
    val ui: StateFlow<CreateCommunityUiState> = _ui.asStateFlow()

    fun create(request: CreateCommunityRequest) {
        viewModelScope.launch {
            _ui.value = CreateCommunityUiState(saving = true)
            runCatching {
                val community = repo.createCommunity(request)
                _ui.value = CreateCommunityUiState(createdId = community.id)
            }.onFailure {
                _ui.value = CreateCommunityUiState(error = AuthErrors.message(it))
            }
        }
    }
}
