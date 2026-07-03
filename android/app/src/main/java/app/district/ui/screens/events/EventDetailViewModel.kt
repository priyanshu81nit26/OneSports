package app.district.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.DemoSportsData
import app.district.data.DistrictEvent
import app.district.data.DistrictRepository
import app.district.data.LocalEventStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventDetailUiState(
    val loading: Boolean = true,
    val event: DistrictEvent? = null,
    val error: String? = null,
    val busy: Boolean = false,
    val showBookingConfirm: Boolean = false
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val repo: DistrictRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(EventDetailUiState())
    val ui: StateFlow<EventDetailUiState> = _ui.asStateFlow()

    fun load(eventId: String) {
        viewModelScope.launch {
            _ui.value = EventDetailUiState(loading = true)
            runCatching {
                val remote = repo.getEvent(eventId)
                val event = when {
                    remote != null -> LocalEventStore.apply(remote)
                    else -> DemoSportsData.findEvent(eventId)?.let { LocalEventStore.apply(it) }
                }
                _ui.value = EventDetailUiState(loading = false, event = event)
            }.onFailure {
                val demo = DemoSportsData.findEvent(eventId)?.let { LocalEventStore.apply(it) }
                _ui.value = if (demo != null) {
                    EventDetailUiState(loading = false, event = demo)
                } else {
                    EventDetailUiState(loading = false, error = "Something went wrong. Try again in a moment.")
                }
            }
        }
    }

    fun requestRegister() {
        val event = _ui.value.event ?: return
        if (event.isRegistered) {
            toggleRegistration(event.id)
        } else {
            _ui.value = _ui.value.copy(showBookingConfirm = true)
        }
    }

    fun dismissBookingConfirm() {
        _ui.value = _ui.value.copy(showBookingConfirm = false)
    }

    fun confirmRegistration() {
        val eventId = _ui.value.event?.id ?: return
        _ui.value = _ui.value.copy(showBookingConfirm = false)
        toggleRegistration(eventId)
    }

    fun toggleRegistration(eventId: String) {
        val event = _ui.value.event ?: return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = true, error = null)
            if (eventId.startsWith("demo-")) {
                if (event.isRegistered) LocalEventStore.unregister(eventId) else LocalEventStore.register(eventId)
                val updated = DemoSportsData.findEvent(eventId)?.let { LocalEventStore.apply(it) }
                    ?: event.copy(isRegistered = !event.isRegistered)
                _ui.value = EventDetailUiState(event = updated, loading = false)
                return@launch
            }
            runCatching {
                val updated = if (event.isRegistered) {
                    repo.cancelEventRegistration(eventId)
                    repo.getEvent(eventId)
                } else {
                    repo.registerForEvent(eventId)
                }
                _ui.value = EventDetailUiState(
                    event = LocalEventStore.apply(updated ?: event.copy(isRegistered = !event.isRegistered)),
                    loading = false
                )
            }.onFailure {
                _ui.value = _ui.value.copy(busy = false, error = "Something went wrong. Try again in a moment.")
            }
        }
    }

    fun toggleSave(eventId: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = true, error = null)
            if (eventId.startsWith("demo-")) {
                LocalEventStore.toggleSave(eventId)
                val updated = DemoSportsData.findEvent(eventId)?.let { LocalEventStore.apply(it) }
                _ui.value = EventDetailUiState(event = updated ?: _ui.value.event, loading = false)
                return@launch
            }
            runCatching {
                val updated = repo.toggleEventSave(eventId)
                _ui.value = EventDetailUiState(event = LocalEventStore.apply(updated), loading = false)
            }.onFailure {
                _ui.value = _ui.value.copy(busy = false, error = "Something went wrong. Try again in a moment.")
            }
        }
    }
}
