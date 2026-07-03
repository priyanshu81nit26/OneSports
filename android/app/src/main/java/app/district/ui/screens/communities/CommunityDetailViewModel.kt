package app.district.ui.screens.communities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.CommunityMember
import app.district.data.CommunityUpdate
import app.district.data.DistrictCommunity
import app.district.data.DistrictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityDetailUiState(
    val loading: Boolean = true,
    val community: DistrictCommunity? = null,
    val updates: List<CommunityUpdate> = emptyList(),
    val members: List<CommunityMember> = emptyList(),
    val error: String? = null,
    val busy: Boolean = false,
    val updateDraft: String = ""
)

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    private val repo: DistrictRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(CommunityDetailUiState())
    val ui: StateFlow<CommunityDetailUiState> = _ui.asStateFlow()

    fun load(communityId: String) {
        viewModelScope.launch {
            _ui.value = CommunityDetailUiState(loading = true)
            runCatching {
                val community = repo.getCommunity(communityId)
                val updates = repo.listCommunityUpdates(communityId)
                val members = repo.listCommunityMembers(communityId)
                _ui.value = CommunityDetailUiState(
                    loading = false,
                    community = community,
                    updates = updates,
                    members = members
                )
            }.onFailure {
                _ui.value = CommunityDetailUiState(loading = false, error = "Something went wrong. Try again in a moment.")
            }
        }
    }

    fun join(communityId: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = true)
            runCatching {
                val c = repo.joinCommunity(communityId)
                load(communityId)
                _ui.value = _ui.value.copy(community = c, busy = false)
            }.onFailure {
                _ui.value = _ui.value.copy(busy = false, error = "Something went wrong. Try again in a moment.")
            }
        }
    }

    fun postUpdate(communityId: String, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = true)
            runCatching {
                repo.postCommunityUpdate(communityId, message.trim())
                load(communityId)
                _ui.value = _ui.value.copy(busy = false, updateDraft = "")
            }.onFailure {
                _ui.value = _ui.value.copy(busy = false, error = "Something went wrong. Try again in a moment.")
            }
        }
    }

    fun connect(member: CommunityMember) {
        viewModelScope.launch {
            runCatching { repo.sendConnectionRequest(member.userId) }
                .onFailure { _ui.value = _ui.value.copy(error = "Something went wrong. Try again in a moment.") }
        }
    }
}
