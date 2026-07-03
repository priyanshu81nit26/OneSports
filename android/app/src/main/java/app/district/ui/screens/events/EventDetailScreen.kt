package app.district.ui.screens.events

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.ui.components.RiseCard
import app.district.ui.components.RiseMissionTimeline
import app.district.ui.components.RiseOutlinedButton
import app.district.ui.components.RisePrimaryButton
import app.district.ui.components.RiseScreen
import app.district.ui.theme.Rise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    LaunchedEffect(eventId) { viewModel.load(eventId) }

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
                Text("Event details", color = Rise.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))

            when {
                ui.loading -> CircularProgressIndicator(color = Rise.AccentSoft)
                ui.event == null -> Text(ui.error ?: "We couldn't find this event", color = Rise.Danger)
                else -> {
                    val e = ui.event!!

                    RiseCard {
                        Text(e.imageEmoji, fontSize = 48.sp)
                        Text(e.title, color = Rise.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("${e.category.emoji} ${e.category.label}", color = Rise.AccentSoft, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        if (e.hasTimeline) {
                            Spacer(Modifier.height(6.dp))
                            Text("${e.dayCount} day schedule", color = Rise.TextSecondary, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        detailRow("When", formatDateRange(e.startAt, e.endAt, e.dayCount))
                        detailRow("Where", e.displayVenue)
                        detailRow("Entry fee", e.fee)
                        detailRow("Hosted by", e.organizerName)
                        if (e.prize.isNotBlank()) detailRow("Prize", e.prize)
                        if (!e.isFull || e.isRegistered) {
                            detailRow("Spots left", if (e.maxParticipants <= 0) "Open" else "${e.spotsLeft} remaining")
                        } else {
                            Text("Event full — join the waitlist by saving it.", color = Rise.Accent, fontSize = 13.sp)
                        }
                        if (e.communityName.isNotBlank()) detailRow("Community", e.communityName)
                        Spacer(Modifier.height(8.dp))
                        Text(e.description, color = Rise.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                        if (e.participantMessage.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text("For participants", color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(e.participantMessage, color = Rise.AccentSoft, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                        if (e.rules.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Rules", color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(e.rules, color = Rise.TextMuted, fontSize = 13.sp)
                        }
                        e.customFields.filter { it.label.isNotBlank() || it.value.isNotBlank() }.forEach { field ->
                            detailRow(field.label, field.value)
                        }
                    }

                    if (e.hasTimeline && e.timelineDays.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Schedule", color = Rise.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Day-by-day breakdown for this event.", color = Rise.TextMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(12.dp))
                        RiseMissionTimeline(days = e.timelineDays)
                    }

                    Spacer(Modifier.height(16.dp))
                    RisePrimaryButton(
                        text = when {
                            ui.busy -> "Please wait…"
                            e.isRegistered -> "Cancel booking"
                            e.isFull -> "Event full"
                            else -> "Book your spot"
                        },
                        enabled = !ui.busy && (!e.isFull || e.isRegistered),
                        onClick = { viewModel.requestRegister() }
                    )
                    Spacer(Modifier.height(10.dp))
                    RiseOutlinedButton(
                        text = when {
                            ui.busy -> "Please wait…"
                            e.isSaved -> "Saved — remove"
                            else -> "Save for later"
                        },
                        enabled = !ui.busy,
                        onClick = { viewModel.toggleSave(eventId) }
                    )
                }
            }
        }
    }

    if (ui.showBookingConfirm) {
        val e = ui.event
        AlertDialog(
            onDismissRequest = { viewModel.dismissBookingConfirm() },
            containerColor = Rise.Card,
            title = { Text("Confirm booking", color = Rise.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(e?.title.orEmpty(), color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Fee: ${e?.fee ?: "Free"}", color = Rise.TextSecondary, fontSize = 13.sp)
                    Text("When: ${e?.let { formatDateRange(it.startAt, it.endAt, it.dayCount) } ?: ""}", color = Rise.TextSecondary, fontSize = 13.sp)
                    Text("Venue: ${e?.displayVenue ?: ""}", color = Rise.TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmRegistration() }) {
                    Text("Confirm", color = Rise.TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissBookingConfirm() }) {
                    Text("Cancel", color = Rise.TextMuted)
                }
            }
        )
    }
}

@Composable
private fun detailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label: ", color = Rise.TextMuted, fontSize = 13.sp)
        Text(value, color = Rise.TextPrimary, fontSize = 13.sp)
    }
}

private fun formatDate(ms: Long): String {
    if (ms <= 0L) return "TBD"
    return SimpleDateFormat("EEE, d MMM yyyy · h:mm a", Locale.getDefault()).format(Date(ms))
}

private fun formatDateRange(startAt: Long, endAt: Long, dayCount: Int): String {
    if (startAt <= 0L) return "TBD"
    val start = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(startAt))
    return if (dayCount > 1 && endAt > startAt) {
        val end = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(endAt))
        "$start – $end · $dayCount days"
    } else {
        formatDate(startAt)
    }
}
