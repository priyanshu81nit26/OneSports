package app.district.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import app.district.data.DistrictEvent
import app.district.data.EventCategory
import app.district.data.EventTimelineDay
import app.district.data.EventTimelineSegment
import app.district.ui.theme.Rise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RiseScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Rise.Black),
        content = content
    )
}

@Composable
fun RiseSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Rise.TextMuted) },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Rise.Card,
            unfocusedContainerColor = Rise.Card,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Rise.TextPrimary,
            unfocusedTextColor = Rise.TextPrimary,
            cursorColor = Rise.AccentSoft
        )
    )
}

@Composable
fun RiseFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        label,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, if (selected) Rise.TextPrimary else Rise.ChipOutline, RoundedCornerShape(20.dp))
            .background(if (selected) Rise.CardElevated else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        color = if (selected) Rise.TextPrimary else Rise.TextSecondary,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
    )
}

@Composable
fun RiseSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        modifier = modifier.padding(vertical = 4.dp),
        color = Rise.TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun RiseEventPosterCard(
    event: DistrictEvent,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    onBookmark: ((String) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(event.id) }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(if (featured) 0.72f else 0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(categoryGradient(event.category))
        ) {
            Text(
                event.imageEmoji.ifBlank { event.category.emoji },
                fontSize = if (featured) 56.sp else 40.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            if (onBookmark != null) {
                IconButton(
                    onClick = { onBookmark(event.id) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        if (event.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Save",
                        tint = Rise.TextPrimary
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (featured && event.displayVenue.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = Rise.Accent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    event.displayVenue,
                    color = Rise.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
        }
        Text(
            event.title.ifBlank { event.category.label },
            color = Rise.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = if (featured) 16.sp else 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            formatEventWhen(event.startAt),
            color = Rise.TextMuted,
            fontSize = 12.sp
        )
        if (!featured && event.displayVenue.isNotBlank()) {
            Text(
                event.displayVenue,
                color = Rise.TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RiseCategoryTile(
    category: EventCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Rise.Card)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(category.label, color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Text(category.emoji, fontSize = 36.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun RiseListRow(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Rise.TextPrimary, fontSize = 15.sp)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, color = Rise.TextMuted, fontSize = 12.sp)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Rise.TextMuted)
    }
}

@Composable
fun RiseGroupedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Rise.Card)
    ) {
        content()
    }
}

@Composable
fun RiseSportCategoryGrid(
    categories: List<EventCategory>,
    selected: EventCategory?,
    onCategoryClick: (EventCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    categories.chunked(3).forEach { row ->
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            row.forEach { category ->
                RiseCategoryTile(
                    category = category,
                    onClick = { onCategoryClick(category) },
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (selected == category) Modifier.border(1.dp, Rise.AccentSoft, RoundedCornerShape(16.dp))
                            else Modifier
                        )
                )
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(10.dp))
    }
}

private fun categoryGradient(category: EventCategory): Brush {
    val (a, b) = when (category) {
        EventCategory.FOOTBALL -> Color(0xFF1B4332) to Color(0xFF081C15)
        EventCategory.CRICKET -> Color(0xFF2D5016) to Color(0xFF1B3409)
        EventCategory.BASKETBALL -> Color(0xFF4A3728) to Color(0xFF1A1208)
        EventCategory.BADMINTON -> Color(0xFF3D2C5E) to Color(0xFF1A1A2E)
        EventCategory.TENNIS -> Color(0xFF2D5016) to Color(0xFF142109)
        EventCategory.SWIMMING -> Color(0xFF1D3557) to Color(0xFF0D1B2A)
        EventCategory.MARATHON -> Color(0xFF4A1942) to Color(0xFF1A1A2E)
        EventCategory.SPORTS -> Color(0xFF1B4332) to Color(0xFF081C15)
        EventCategory.FITNESS -> Color(0xFF3D2C5E) to Color(0xFF1A1A2E)
        EventCategory.DANCE -> Color(0xFF5C2E46) to Color(0xFF2D1320)
        EventCategory.CYCLING -> Color(0xFF1D3557) to Color(0xFF0D1B2A)
        EventCategory.CHESS -> Color(0xFF2B2D42) to Color(0xFF141622)
        EventCategory.OTHER -> Color(0xFF2C2C2E) to Color(0xFF121212)
    }
    return Brush.verticalGradient(listOf(a, b))
}

private fun formatEventWhen(ms: Long): String {
    if (ms <= 0L) return "Date TBD"
    return SimpleDateFormat("EEE, dd MMM · h:mm a", Locale.getDefault()).format(Date(ms))
}

@Composable
fun RiseDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Rise.Border))
}

@Composable
fun RiseCard(
    modifier: Modifier = Modifier,
    innerPadding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Rise.Card)
            .padding(innerPadding),
        content = content
    )
}

@Composable
fun RisePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Rise.TextPrimary,
    contentColor: Color = Rise.Black
) {
    RiseAnimatedButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = if (containerColor == Rise.TextPrimary) RiseButtonStyle.Primary else RiseButtonStyle.Secondary,
        containerColor = if (containerColor == Rise.TextPrimary) null else containerColor,
        contentColor = contentColor
    )
}

@Composable
fun RiseOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = Rise.TextPrimary
) {
    RiseAnimatedButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = RiseButtonStyle.Outlined,
        contentColor = accent
    )
}

@Composable
fun RisePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Rise.AccentSoft.copy(alpha = 0.22f) else Rise.CardElevated)
            .border(1.dp, if (selected) Rise.AccentSoft else Rise.Border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = if (selected) Rise.AccentSoft else Rise.TextSecondary,
        fontSize = 12.sp,
        maxLines = 1,
        softWrap = false,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
    )
}

@Composable
fun RiseMissionTimeline(
    days: List<EventTimelineDay>,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        days.forEach { day ->
            RiseCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Rise.AccentSoft.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${day.dayNumber}", color = Rise.AccentSoft, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            day.title.ifBlank { "Day ${day.dayNumber}" },
                            color = Rise.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        if (day.venue.isNotBlank()) {
                            Text(day.venue, color = Rise.TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (day.segments.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    day.segments.forEach { segment ->
                        RiseTimelineSegmentRow(segment)
                        Spacer(Modifier.height(6.dp))
                    }
                }
                day.extraFields.filter { it.label.isNotBlank() || it.value.isNotBlank() }.forEach { field ->
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(field.label, color = Rise.TextMuted, fontSize = 12.sp)
                        Text(field.value, color = Rise.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun RiseTimelineSegmentRow(segment: EventTimelineSegment) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Rise.CardElevated)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            segment.label.ifBlank { "Session" },
            color = Rise.TextPrimary,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val timeText = when {
            segment.startTime.isNotBlank() && segment.endTime.isNotBlank() -> "${segment.startTime} – ${segment.endTime}"
            segment.startTime.isNotBlank() -> segment.startTime
            segment.endTime.isNotBlank() -> segment.endTime
            else -> ""
        }
        if (timeText.isNotBlank()) {
            Text(timeText, color = Rise.AccentSoft, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun riseFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Rise.CardElevated,
    unfocusedContainerColor = Rise.CardElevated,
    focusedBorderColor = Rise.AccentSoft,
    unfocusedBorderColor = Rise.Border,
    focusedTextColor = Rise.TextPrimary,
    unfocusedTextColor = Rise.TextPrimary,
    focusedLabelColor = Rise.TextSecondary,
    unfocusedLabelColor = Rise.TextMuted,
    cursorColor = Rise.AccentSoft
)
