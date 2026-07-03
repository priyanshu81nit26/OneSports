package app.district.ui.screens.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.ConnectionRequest
import app.district.data.DistrictConnection
import app.district.data.DistrictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectionsUiState(
    val loading: Boolean = true,
    val connections: List<DistrictConnection> = emptyList(),
    val pending: List<ConnectionRequest> = emptyList(),
    val error: String? = null,
    val busy: Boolean = false
)

@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val repo: DistrictRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ConnectionsUiState())
    val ui: StateFlow<ConnectionsUiState> = _ui.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.value = ConnectionsUiState(loading = true)
            runCatching {
                val connections = repo.listMyConnections()
                val pending = repo.listPendingConnectionRequests()
                _ui.value = ConnectionsUiState(
                    loading = false,
                    connections = connections,
                    pending = pending
                )
            }.onFailure {
                _ui.value = ConnectionsUiState(
                    loading = false,
                    error = "Something went wrong. Try again in a moment."
                )
            }
        }
    }

    fun respond(requestId: String, accept: Boolean) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = true)
            runCatching {
                repo.respondConnectionRequest(requestId, accept)
                refresh()
            }.onFailure {
                _ui.value = _ui.value.copy(busy = false, error = "Something went wrong. Try again in a moment.")
            }
        }
    }
}
