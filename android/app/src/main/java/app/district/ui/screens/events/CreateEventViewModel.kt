package app.district.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.AuthErrors
import app.district.data.CreateEventRequest
import app.district.data.DistrictCommunity
import app.district.data.DistrictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateEventUiState(
    val saving: Boolean = false,
    val error: String? = null,
    val createdId: String? = null,
    val myCommunities: List<DistrictCommunity> = emptyList()
)

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repo: DistrictRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(CreateEventUiState())
    val ui: StateFlow<CreateEventUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                _ui.value = _ui.value.copy(myCommunities = repo.listMyCommunities())
            }
        }
    }

    fun create(request: CreateEventRequest) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            runCatching {
                val event = repo.createEvent(request)
                _ui.value = CreateEventUiState(createdId = event.id, myCommunities = _ui.value.myCommunities)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    saving = false,
                    error = AuthErrors.message(e),
                    myCommunities = _ui.value.myCommunities
                )
            }
        }
    }

    fun clearError() {
        _ui.value = _ui.value.copy(error = null)
    }
}
