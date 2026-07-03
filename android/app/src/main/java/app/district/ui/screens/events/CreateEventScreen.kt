package app.district.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.data.CreateEventRequest
import app.district.data.EventCategory
import app.district.data.EventCustomField
import app.district.data.EventTimelineDay
import app.district.data.EventTimelineSegment
import app.district.data.EventTags
import app.district.ui.components.RiseCard
import app.district.ui.components.RisePill
import app.district.ui.components.RisePrimaryButton
import app.district.ui.components.RiseScreen
import app.district.ui.components.riseFieldColors
import app.district.ui.theme.Rise
import java.util.Calendar

@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("50") }
    var fee by remember { mutableStateOf("Free") }
    var rules by remember { mutableStateOf("") }
    var prize by remember { mutableStateOf("") }
    var participantMessage by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(EventCategory.MARATHON) }
    var communityId by remember { mutableStateOf("") }
    var daysFromNow by remember { mutableIntStateOf(7) }
    var dayCount by remember { mutableIntStateOf(1) }
    var buildTimeline by remember { mutableStateOf(false) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    val timelineDays = remember { mutableStateListOf<EventTimelineDay>() }
    val customFields = remember { mutableStateListOf<EventCustomField>() }

    LaunchedEffect(dayCount, buildTimeline) {
        if (!buildTimeline) return@LaunchedEffect
        while (timelineDays.size < dayCount) {
            val n = timelineDays.size + 1
            timelineDays.add(
                EventTimelineDay(
                    dayNumber = n,
                    title = if (n == 1) "Day 1 — kick-off" else "Day $n",
                    venue = venue,
                    segments = listOf(EventTimelineSegment(label = "Start", startTime = "09:00", endTime = ""))
                )
            )
        }
        while (timelineDays.size > dayCount) timelineDays.removeAt(timelineDays.lastIndex)
    }

    LaunchedEffect(ui.createdId) {
        ui.createdId?.let(onCreated)
    }

    val startCal = remember(daysFromNow, dayCount) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysFromNow)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }
    }
    val endCal = remember(startCal, dayCount) {
        (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, (dayCount - 1).coerceAtLeast(0)); add(Calendar.HOUR_OF_DAY, 6) }
    }

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
                Text("Create an event", color = Rise.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))

            RiseCard(Modifier.fillMaxWidth()) {
                Text("Basics", color = Rise.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("Category", color = Rise.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EventCategory.entries.forEach { cat ->
                        val selected = category == cat
                        Text(
                            "${cat.emoji} ${cat.label}",
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) Rise.AccentSoft.copy(alpha = 0.25f) else Rise.CardElevated)
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (selected) Rise.AccentSoft else Rise.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (ui.myCommunities.isNotEmpty()) {
                    Text("Link to community (optional)", color = Rise.TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "None",
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (communityId.isBlank()) Rise.AccentSoft.copy(alpha = 0.25f) else Rise.CardElevated)
                                .clickable { communityId = "" }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (communityId.isBlank()) Rise.AccentSoft else Rise.TextSecondary,
                            fontSize = 12.sp
                        )
                        ui.myCommunities.forEach { comm ->
                            val selected = communityId == comm.id
                            Text(
                                "${comm.emoji} ${comm.name}",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) Rise.AccentSoft.copy(alpha = 0.25f) else Rise.CardElevated)
                                    .clickable { communityId = comm.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (selected) Rise.AccentSoft else Rise.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                field("What's happening?", title, { title = it })
                field("Description", description, { description = it }, minLines = 2)
                field("Main venue", venue, { venue = it; if (buildTimeline && timelineDays.isNotEmpty() && timelineDays[0].venue.isBlank()) timelineDays[0] = timelineDays[0].copy(venue = it) })
                field("Address", address, { address = it })
                field("Capacity", maxParticipants, { maxParticipants = it.filter { c -> c.isDigit() } })
                field("Entry fee", fee, { fee = it })
                field("Prize (optional)", prize, { prize = it })
                field("Message for participants", participantMessage, { participantMessage = it }, minLines = 2)
                field("Rules or what to bring", rules, { rules = it }, minLines = 2)

                Spacer(Modifier.height(12.dp))
                Text("Tags (pick at least one)", color = Rise.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EventTags.presets.forEach { tag ->
                        val picked = selectedTags.contains(tag)
                        Text(
                            tag,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (picked) Rise.Accent.copy(alpha = 0.28f) else Rise.CardElevated)
                                .clickable {
                                    selectedTags = if (picked) selectedTags - tag else selectedTags + tag
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (picked) Rise.Accent else Rise.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (picked) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Starts in", color = Rise.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1L, 3L, 7L, 14L, 30L).forEach { d ->
                        RisePill(
                            text = if (d == 1L) "Tomorrow" else "$d days",
                            selected = daysFromNow.toLong() == d,
                            onClick = { daysFromNow = d.toInt() }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Number of days", color = Rise.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..7).forEach { d ->
                        RisePill(
                            text = if (d == 1) "1 day" else "$d days",
                            selected = dayCount == d,
                            onClick = { dayCount = d }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            RiseCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Timeline, null, tint = Rise.AccentSoft)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Mission timeline", color = Rise.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "Build a day-by-day schedule — perfect for marathons and multi-day events.",
                            color = Rise.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                RisePrimaryButton(
                    text = if (buildTimeline) "Timeline enabled ✓" else "Create timeline",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        buildTimeline = !buildTimeline
                        if (buildTimeline && timelineDays.isEmpty()) {
                            repeat(dayCount) { i ->
                                timelineDays.add(
                                    EventTimelineDay(
                                        dayNumber = i + 1,
                                        title = if (i == 0) "Day 1 — kick-off" else "Day ${i + 1}",
                                        venue = venue,
                                        segments = listOf(EventTimelineSegment(label = "Start", startTime = "09:00", endTime = ""))
                                    )
                                )
                            }
                        }
                    }
                )

                if (buildTimeline) {
                    Spacer(Modifier.height(16.dp))
                    timelineDays.forEachIndexed { index, day ->
                        TimelineDayEditor(
                            day = day,
                            accent = Rise.AccentSoft,
                            onUpdate = { timelineDays[index] = it }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    Text("Extra event info", color = Rise.TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    customFields.forEachIndexed { index, customField ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            field("Label", customField.label, { customFields[index] = customField.copy(label = it) }, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            field("Value", customField.value, { customFields[index] = customField.copy(value = it) }, modifier = Modifier.weight(1f))
                            IconButton(onClick = { customFields.removeAt(index) }) {
                                Icon(Icons.Filled.Delete, "Remove", tint = Rise.Danger)
                            }
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { customFields.add(EventCustomField()) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, null, tint = Rise.AccentSoft)
                        Spacer(Modifier.width(6.dp))
                        Text("Add custom field", color = Rise.AccentSoft, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            RiseCard(Modifier.fillMaxWidth()) {
                ui.error?.let {
                    Text(it, color = Rise.Danger, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                }
                RisePrimaryButton(
                    text = if (ui.saving) "Creating…" else "Publish event",
                    enabled = title.length >= 3 && selectedTags.isNotEmpty() && !ui.saving,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.create(
                            CreateEventRequest(
                                title = title.trim(),
                                description = description.trim(),
                                category = category,
                                venue = venue.trim(),
                                address = address.trim(),
                                startAt = startCal.timeInMillis,
                                endAt = endCal.timeInMillis,
                                maxParticipants = maxParticipants.toIntOrNull() ?: 50,
                                fee = fee.trim().ifBlank { "Free" },
                                rules = rules.trim(),
                                communityId = communityId,
                                imageEmoji = category.emoji,
                                hasTimeline = buildTimeline,
                                dayCount = dayCount,
                                prize = prize.trim(),
                                participantMessage = participantMessage.trim(),
                                timelineDays = if (buildTimeline) timelineDays.toList() else emptyList(),
                                customFields = customFields.filter { it.label.isNotBlank() || it.value.isNotBlank() },
                                tags = selectedTags.toList()
                            )
                        )
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TimelineDayEditor(
    day: EventTimelineDay,
    accent: androidx.compose.ui.graphics.Color,
    onUpdate: (EventTimelineDay) -> Unit
) {
    val segments = remember(day.dayNumber) { mutableStateListOf<EventTimelineSegment>().also { it.addAll(day.segments) } }
    val extraFields = remember(day.dayNumber) { mutableStateListOf<EventCustomField>().also { it.addAll(day.extraFields) } }

    LaunchedEffect(segments.toList(), extraFields.toList()) {
        onUpdate(day.copy(segments = segments.toList(), extraFields = extraFields.toList()))
    }

    RiseCard(Modifier.fillMaxWidth(), innerPadding = 14.dp) {
        Text("Day ${day.dayNumber}", color = accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        field("Day title", day.title, { onUpdate(day.copy(title = it)) })
        field("Venue for this day", day.venue, { onUpdate(day.copy(venue = it)) })

        Spacer(Modifier.height(8.dp))
        Text("Schedule in the day", color = Rise.TextSecondary, fontSize = 11.sp)
        segments.forEachIndexed { index, seg ->
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                field("Activity", seg.label, { segments[index] = seg.copy(label = it) }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(6.dp))
                timeField("Start", seg.startTime, { segments[index] = seg.copy(startTime = it) })
                Spacer(Modifier.width(6.dp))
                timeField("End", seg.endTime, { segments[index] = seg.copy(endTime = it) })
                if (segments.size > 1) {
                    IconButton(onClick = { segments.removeAt(index) }) {
                        Icon(Icons.Filled.Delete, "Remove", tint = Rise.Danger, modifier = Modifier.height(20.dp).width(20.dp))
                    }
                }
            }
        }
        Row(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { segments.add(EventTimelineSegment(label = "Session", startTime = "", endTime = "")) }
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, null, tint = accent, modifier = Modifier.height(18.dp).width(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add time slot", color = accent, fontSize = 12.sp)
        }

        extraFields.forEachIndexed { index, extraField ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                field("Field", extraField.label, { extraFields[index] = extraField.copy(label = it) }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(6.dp))
                field("Info", extraField.value, { extraFields[index] = extraField.copy(value = it) }, modifier = Modifier.weight(1f))
                IconButton(onClick = { extraFields.removeAt(index) }) {
                    Icon(Icons.Filled.Delete, "Remove", tint = Rise.Danger)
                }
            }
        }
        Row(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { extraFields.add(EventCustomField()) }
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, null, tint = accent, modifier = Modifier.height(18.dp).width(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add day field", color = accent, fontSize = 12.sp)
        }
    }
}

@Composable
private fun timeField(label: String, value: String, onValue: (String) -> Unit) {
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.width(96.dp),
        colors = riseFieldColors()
    )
}

@Composable
private fun field(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    minLines: Int = 1,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        minLines = minLines,
        singleLine = minLines == 1,
        modifier = modifier,
        colors = riseFieldColors()
    )
}
