package app.district.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.district.data.DemoSportsData
import app.district.data.DistrictActivity
import app.district.data.DistrictCommunity
import app.district.data.DistrictEvent
import app.district.data.DistrictRepository
import app.district.data.DistrictSummary
import app.district.data.EventCategory
import app.district.data.EventDateFilter
import app.district.data.EventFilterUtils
import app.district.data.EventListFilter
import app.district.data.LocalEventStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeTab { DISCOVER, HOST, COMMUNITIES, PROFILE }

data class HomeUiState(
    val tab: HomeTab = HomeTab.DISCOVER,
    val loading: Boolean = true,
    val error: String? = null,
    val usingDemoData: Boolean = false,
    val summary: DistrictSummary = DistrictSummary(),
    val activityFeed: List<DistrictActivity> = emptyList(),
    val myEvents: List<DistrictEvent> = emptyList(),
    val allEvents: List<DistrictEvent> = emptyList(),
    val featuredEvents: List<DistrictEvent> = emptyList(),
    val registeredEvents: List<DistrictEvent> = emptyList(),
    val savedEvents: List<DistrictEvent> = emptyList(),
    val communities: List<DistrictCommunity> = emptyList(),
    val myCommunities: List<DistrictCommunity> = emptyList(),
    val eventFilter: EventListFilter = EventListFilter(),
    val catalogEvents: List<DistrictEvent> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: DistrictRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(tab: HomeTab) {
        _ui.value = _ui.value.copy(tab = tab)
    }

    fun setEventSearch(search: String) {
        _ui.value = _ui.value.copy(eventFilter = _ui.value.eventFilter.copy(search = search))
        applyFilters()
    }

    fun setEventCategory(category: EventCategory?) {
        _ui.value = _ui.value.copy(eventFilter = _ui.value.eventFilter.copy(category = category))
        applyFilters()
    }

    fun setEventTag(tag: String?) {
        _ui.value = _ui.value.copy(eventFilter = _ui.value.eventFilter.copy(tag = tag))
        applyFilters()
    }

    fun setDateFilter(dateFilter: EventDateFilter) {
        _ui.value = _ui.value.copy(eventFilter = _ui.value.eventFilter.copy(dateFilter = dateFilter))
        applyFilters()
    }

    fun toggleTimelineOnly() {
        val next = !_ui.value.eventFilter.timelineOnly
        _ui.value = _ui.value.copy(eventFilter = _ui.value.eventFilter.copy(timelineOnly = next))
        applyFilters()
    }

    fun clearEventFilters() {
        _ui.value = _ui.value.copy(eventFilter = EventListFilter())
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = EventFilterUtils.filter(_ui.value.catalogEvents, _ui.value.eventFilter)
        _ui.value = _ui.value.copy(
            allEvents = filtered,
            featuredEvents = EventFilterUtils.featured(filtered),
            registeredEvents = _ui.value.catalogEvents.filter { it.isRegistered },
            savedEvents = _ui.value.catalogEvents.filter { it.isSaved }
        )
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            runCatching {
                coroutineScope {
                    val filter = _ui.value.eventFilter
                    val summaryDef = async { runCatching { repo.getDistrictSummary() }.getOrDefault(DistrictSummary()) }
                    val feedDef = async { runCatching { repo.listActivityFeed() }.getOrDefault(emptyList()) }
                    val myEventsDef = async { runCatching { repo.listMyOrganizedEvents() }.getOrDefault(emptyList()) }
                    val allEventsDef = async { runCatching { repo.listEvents(filter) }.getOrDefault(emptyList()) }
                    val registeredDef = async { runCatching { repo.listMyRegisteredEvents() }.getOrDefault(emptyList()) }
                    val savedDef = async { runCatching { repo.listSavedEvents() }.getOrDefault(emptyList()) }
                    val communitiesDef = async { runCatching { repo.listCommunities() }.getOrDefault(emptyList()) }
                    val myCommunitiesDef = async { runCatching { repo.listMyCommunities() }.getOrDefault(emptyList()) }

                    var remoteEvents = allEventsDef.await()
                    val usingDemo = false
                    remoteEvents = LocalEventStore.applyAll(remoteEvents)

                    val filtered = EventFilterUtils.filter(remoteEvents, filter)
                    val registered = if (usingDemo) remoteEvents.filter { it.isRegistered } else registeredDef.await()
                    val saved = if (usingDemo) remoteEvents.filter { it.isSaved } else savedDef.await()

                    _ui.value = _ui.value.copy(
                        loading = false,
                        usingDemoData = usingDemo,
                        catalogEvents = remoteEvents,
                        summary = summaryDef.await(),
                        activityFeed = feedDef.await(),
                        myEvents = myEventsDef.await(),
                        allEvents = filtered,
                        featuredEvents = EventFilterUtils.featured(filtered),
                        registeredEvents = registered,
                        savedEvents = saved,
                        communities = communitiesDef.await(),
                        myCommunities = myCommunitiesDef.await()
                    )
                }
            }.onFailure {
                val demo = LocalEventStore.applyAll(DemoSportsData.sampleEvents)
                val filtered = EventFilterUtils.filter(demo, _ui.value.eventFilter)
                _ui.value = _ui.value.copy(
                    loading = false,
                    usingDemoData = true,
                    catalogEvents = demo,
                    allEvents = filtered,
                    featuredEvents = EventFilterUtils.featured(filtered),
                    registeredEvents = demo.filter { it.isRegistered },
                    savedEvents = demo.filter { it.isSaved },
                    error = null
                )
            }
        }
    }
}
