package app.district.ui.screens.bookings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.ui.components.RiseEventPosterCard
import app.district.ui.components.RiseFilterChip
import app.district.ui.components.RiseScreen
import app.district.ui.components.RiseSectionTitle
import app.district.ui.screens.home.HomeViewModel
import app.district.ui.theme.Rise

enum class BookingTab { REGISTERED, SAVED }

@Composable
fun MyBookingsScreen(
    initialTab: BookingTab = BookingTab.REGISTERED,
    onBack: () -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    var tab by remember(initialTab) { mutableStateOf(initialTab) }

    RiseScreen(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(Modifier.height(36.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
                }
                Text("My bookings", color = Rise.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Sport events you've joined or saved.",
                color = Rise.TextSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                RiseFilterChip("Going", tab == BookingTab.REGISTERED, onClick = { tab = BookingTab.REGISTERED })
                RiseFilterChip("Saved", tab == BookingTab.SAVED, onClick = { tab = BookingTab.SAVED })
            }
            Spacer(Modifier.height(20.dp))

            when (tab) {
                BookingTab.REGISTERED -> {
                    if (ui.registeredEvents.isEmpty()) {
                        EmptyBookingHint(
                            icon = { Icon(Icons.Filled.Event, null, tint = Rise.TextMuted) },
                            title = "No bookings yet",
                            body = "Browse sport events on Discover and tap Register to join."
                        )
                    } else {
                        ui.registeredEvents.forEach { event ->
                            RiseEventPosterCard(event, onEventClick, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
                BookingTab.SAVED -> {
                    if (ui.savedEvents.isEmpty()) {
                        EmptyBookingHint(
                            icon = { Icon(Icons.Filled.Bookmark, null, tint = Rise.TextMuted) },
                            title = "Nothing saved",
                            body = "Tap the bookmark on any event card to save it here."
                        )
                    } else {
                        ui.savedEvents.forEach { event ->
                            RiseEventPosterCard(event, onEventClick, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBookingHint(
    icon: @Composable () -> Unit,
    title: String,
    body: String
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        icon()
        Spacer(Modifier.height(12.dp))
        Text(title, color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(body, color = Rise.TextMuted, fontSize = 13.sp, lineHeight = 18.sp)
    }
}
