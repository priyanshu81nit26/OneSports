package app.district.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.data.DistrictCommunity
import app.district.data.DistrictEvent
import app.district.data.EventCategory
import app.district.data.EventDateFilter
import app.district.data.EventListFilter
import app.district.data.EventTags
import app.district.ui.components.RisePrimaryButton
import app.district.ui.components.FocusLogo
import app.district.ui.components.RiseSportCategoryGrid
import app.district.ui.components.RiseEventPosterCard
import app.district.ui.components.RiseFilterChip
import app.district.ui.components.RiseGroupedCard
import app.district.ui.components.RiseListRow
import app.district.ui.components.RiseScreen
import app.district.ui.components.RiseSearchBar
import app.district.ui.components.RiseSectionTitle
import app.district.ui.theme.Rise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onCreateEvent: () -> Unit,
    onEventClick: (String) -> Unit,
    onCreateCommunity: () -> Unit,
    onJoinCommunity: () -> Unit = {},
    onCommunityClick: (String) -> Unit,
    onConnections: () -> Unit,
    onSettings: () -> Unit,
    onEditProfile: () -> Unit = {},
    onBookings: (savedTab: Boolean) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()

    RiseScreen {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(48.dp))
                DiscoverTopBar(
                    onSaved = { onBookings(true) },
                    onProfile = onSettings
                )
                Spacer(Modifier.height(16.dp))

                when {
                    ui.loading -> Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Rise.AccentSoft)
                    }
                    ui.error != null -> ErrorPanel(ui.error!!) { viewModel.refresh() }
                    else -> when (ui.tab) {
                        HomeTab.DISCOVER -> DiscoverTab(
                            filter = ui.eventFilter,
                            featured = ui.featuredEvents,
                            events = ui.allEvents,
                            onSearch = viewModel::setEventSearch,
                            onCategory = viewModel::setEventCategory,
                            onTag = viewModel::setEventTag,
                            onDateFilter = viewModel::setDateFilter,
                            onToggleTimeline = viewModel::toggleTimelineOnly,
                            onClearFilters = viewModel::clearEventFilters,
                            onCategoryPick = viewModel::setEventCategory,
                            onEventClick = onEventClick
                        )
                        HomeTab.HOST -> HostTab(ui.myEvents, onCreateEvent, onEventClick)
                        HomeTab.COMMUNITIES -> CommunitiesTab(ui.communities, ui.myCommunities, onCreateCommunity, onJoinCommunity, onCommunityClick)
                        HomeTab.PROFILE -> ProfileTab(
                            summary = ui.summary,
                            registeredCount = ui.registeredEvents.size,
                            savedCount = ui.savedEvents.size,
                            onSettings = onSettings,
                            onEditProfile = onEditProfile,
                            onConnections = onConnections,
                            onBookings = onBookings,
                            onHost = { viewModel.selectTab(HomeTab.HOST) },
                            onDiscover = { viewModel.selectTab(HomeTab.DISCOVER) }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            RiseBottomBar(selected = ui.tab, onSelect = viewModel::selectTab)
        }
    }
}

@Composable
private fun DiscoverTopBar(onSaved: () -> Unit, onProfile: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.LocationOn, null, tint = Rise.TextPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your city", color = Rise.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(" ▾", color = Rise.TextMuted, fontSize = 14.sp)
            }
            Text("Discover sport & events", color = Rise.TextMuted, fontSize = 12.sp)
        }
        IconButton(onClick = onSaved) {
            Icon(Icons.Filled.BookmarkBorder, "Saved", tint = Rise.TextPrimary)
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Rise.Card)
                .clickable(onClick = onProfile),
            contentAlignment = Alignment.Center
        ) {
            FocusLogo(Modifier.size(32.dp))
        }
    }
}

@Composable
private fun DiscoverTab(
    filter: EventListFilter,
    featured: List<DistrictEvent>,
    events: List<DistrictEvent>,
    onSearch: (String) -> Unit,
    onCategory: (EventCategory?) -> Unit,
    onTag: (String?) -> Unit,
    onDateFilter: (EventDateFilter) -> Unit,
    onToggleTimeline: () -> Unit,
    onClearFilters: () -> Unit,
    onCategoryPick: (EventCategory) -> Unit,
    onEventClick: (String) -> Unit
) {
    RiseSearchBar(
        value = filter.search,
        onValueChange = onSearch,
        placeholder = "Search football, cricket, running…"
    )
    Spacer(Modifier.height(20.dp))

    if (featured.isNotEmpty()) {
        RiseSectionTitle("Featured sport events")
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(featured, key = { it.id }) { event ->
                RiseEventPosterCard(
                    event = event,
                    onClick = onEventClick,
                    featured = true,
                    modifier = Modifier.width(260.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    RiseSectionTitle("Explore sports")
    Spacer(Modifier.height(12.dp))
    RiseSportCategoryGrid(
        categories = EventCategory.sportGrid,
        selected = filter.category,
        onCategoryClick = onCategoryPick
    )
    Spacer(Modifier.height(24.dp))

    RiseSectionTitle("All events")
    Spacer(Modifier.height(12.dp))
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RiseFilterChip(
            "All",
            filter.category == null && filter.tag.isNullOrBlank() && !filter.timelineOnly && filter.dateFilter == EventDateFilter.ALL,
            onClick = { onClearFilters() }
        )
        RiseFilterChip("Today", filter.dateFilter == EventDateFilter.TODAY, onClick = {
            onDateFilter(if (filter.dateFilter == EventDateFilter.TODAY) EventDateFilter.ALL else EventDateFilter.TODAY)
        })
        RiseFilterChip("Tomorrow", filter.dateFilter == EventDateFilter.TOMORROW, onClick = {
            onDateFilter(if (filter.dateFilter == EventDateFilter.TOMORROW) EventDateFilter.ALL else EventDateFilter.TOMORROW)
        })
        RiseFilterChip("Timelines", filter.timelineOnly, onClick = onToggleTimeline)
        EventTags.presets.take(3).forEach { tag ->
            RiseFilterChip(tag, filter.tag == tag, onClick = { onTag(if (filter.tag == tag) null else tag) })
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EventCategory.sportGrid.forEach { cat ->
            RiseFilterChip("${cat.emoji} ${cat.label}", filter.category == cat, onClick = {
                onCategory(if (filter.category == cat) null else cat)
            })
        }
    }
    Spacer(Modifier.height(16.dp))

    if (events.isEmpty()) {
        EmptyPanel("No events yet", "Be the first to host — tap Host below.")
    } else {
        events.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { event ->
                    RiseEventPosterCard(
                        event = event,
                        onClick = onEventClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HostTab(events: List<DistrictEvent>, onCreate: () -> Unit, onClick: (String) -> Unit) {
    RiseSectionTitle("Host an event")
    Spacer(Modifier.height(8.dp))
    Text("Organise marathons, matches, and meetups for your community.", color = Rise.TextMuted, fontSize = 14.sp)
    Spacer(Modifier.height(16.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Rise.Card)
            .clickable(onClick = onCreate)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Rise.AccentSoft.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, null, tint = Rise.AccentSoft)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Create event", color = Rise.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Add timeline, tags, and venue details", color = Rise.TextMuted, fontSize = 12.sp)
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    RiseSectionTitle("You're hosting")
    Spacer(Modifier.height(12.dp))
    if (events.isEmpty()) EmptyPanel("Nothing hosted yet", "Tap Create event above to get started.")
    else events.forEach { event ->
        RiseEventPosterCard(event, onClick, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CommunitiesTab(
    all: List<DistrictCommunity>,
    mine: List<DistrictCommunity>,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    onClick: (String) -> Unit
) {
    RiseSectionTitle("Communities")
    Spacer(Modifier.height(12.dp))
    RiseGroupedCard {
        RiseListRow("Start a community", { Icon(Icons.Filled.Add, null, tint = Rise.TextPrimary) }, onCreate)
        Box(Modifier.fillMaxWidth().height(1.dp).background(Rise.Border))
        RiseListRow("Join with code or name", { Icon(Icons.Filled.Groups, null, tint = Rise.TextPrimary) }, onJoin)
    }
    Spacer(Modifier.height(16.dp))
    if (mine.isNotEmpty()) {
        Text("Joined", color = Rise.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        mine.forEach { CommunityRow(it, onClick) }
    }
    Spacer(Modifier.height(12.dp))
    Text("Discover", color = Rise.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    if (all.filter { !it.isMember }.isEmpty()) EmptyPanel("No communities yet", "Create one for your club or gym.")
    else all.filter { !it.isMember }.forEach { CommunityRow(it, onClick) }
}

@Composable
private fun CommunityRow(community: DistrictCommunity, onClick: (String) -> Unit) {
    RiseGroupedCard(Modifier.padding(bottom = 10.dp)) {
        RiseListRow(
            title = "${community.emoji} ${community.name}",
            subtitle = "${community.memberCount} members · ${community.location}",
            icon = { Icon(Icons.Filled.Groups, null, tint = Rise.AccentSoft) },
            onClick = { onClick(community.id) }
        )
    }
}

@Composable
private fun ProfileTab(
    summary: app.district.data.DistrictSummary,
    registeredCount: Int,
    savedCount: Int,
    onSettings: () -> Unit,
    onEditProfile: () -> Unit,
    onConnections: () -> Unit,
    onBookings: (savedTab: Boolean) -> Unit,
    onHost: () -> Unit,
    onDiscover: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(Rise.ProfileGradientStart, Rise.Card)))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FocusLogo(Modifier.size(56.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Your profile", color = Rise.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "$registeredCount going · $savedCount saved",
                        color = Rise.AccentSoft,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Set your photo, bio, and username in Set profile.",
                color = Rise.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
    Spacer(Modifier.height(20.dp))
    RiseGroupedCard {
        RiseListRow("Set profile", { Icon(Icons.Filled.Person, null, tint = Rise.TextPrimary) }, onEditProfile)
    }
    Text("All bookings", color = Rise.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BookingShortcut("Sport events", "$registeredCount going", Icons.Filled.ConfirmationNumber, Modifier.weight(1f)) {
            onBookings(false)
        }
        BookingShortcut("Saved", "$savedCount saved", Icons.Filled.BookmarkBorder, Modifier.weight(1f)) {
            onBookings(true)
        }
    }
    Spacer(Modifier.height(20.dp))
    Text("Activity", color = Rise.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    RiseGroupedCard {
        RiseListRow("Events you're hosting", { Icon(Icons.Filled.Event, null, tint = Rise.TextPrimary) }, onHost)
        Box(Modifier.fillMaxWidth().height(1.dp).background(Rise.Border))
        RiseListRow("Discover events", { Icon(Icons.Filled.Home, null, tint = Rise.TextPrimary) }, onDiscover)
        Box(Modifier.fillMaxWidth().height(1.dp).background(Rise.Border))
        RiseListRow("Connections", { Icon(Icons.Filled.Person, null, tint = Rise.TextPrimary) }, onConnections)
    }
    Spacer(Modifier.height(20.dp))
    Text("More", color = Rise.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    RiseGroupedCard {
        RiseListRow("Account settings", { Icon(Icons.Filled.Person, null, tint = Rise.TextPrimary) }, onSettings)
    }
    Spacer(Modifier.height(24.dp))
    Text("Rise", color = Rise.TextMuted, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    Text("v1.0.0", color = Rise.TextMuted, fontSize = 12.sp)
}

@Composable
private fun BookingShortcut(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Rise.Card)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Icon(icon, null, tint = Rise.AccentSoft, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(10.dp))
        Text(title, color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(subtitle, color = Rise.TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun RiseBottomBar(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Rise.Surface)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bottomTab("Discover", Icons.Filled.Home, HomeTab.DISCOVER, selected, onSelect)
        bottomTab("Host", Icons.Filled.SportsScore, HomeTab.HOST, selected, onSelect)
        bottomTab("Groups", Icons.Filled.Groups, HomeTab.COMMUNITIES, selected, onSelect)
        bottomTab("Profile", Icons.Filled.Person, HomeTab.PROFILE, selected, onSelect)
    }
}

@Composable
private fun bottomTab(
    label: String,
    icon: ImageVector,
    tab: HomeTab,
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect(tab) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, label, tint = if (selected == tab) Rise.TextPrimary else Rise.TextMuted)
        Text(label, fontSize = 11.sp, color = if (selected == tab) Rise.TextPrimary else Rise.TextMuted)
    }
}

@Composable
private fun EmptyPanel(title: String, body: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Rise.Card)
            .padding(20.dp)
    ) {
        Column {
            Text(title, color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(body, color = Rise.TextMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Rise.Card)
            .padding(20.dp)
    ) {
        Column {
            Text(message, color = Rise.TextPrimary, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            RisePrimaryButton(text = "Try again", onClick = onRetry)
        }
    }
}

private fun formatDate(ms: Long): String {
    if (ms <= 0L) return "TBD"
    return SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(ms))
}
