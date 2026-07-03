package app.district.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.district.data.EventTimelineDay
import app.district.ui.theme.Arcade
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val orbitAccents = listOf(
    Arcade.NeonCyan,
    Arcade.NeonViolet,
    Arcade.NeonAmber,
    Arcade.NeonMagenta,
    Arcade.NeonLime,
    Arcade.NeonBlue
)

/**
 * Gamified circular mission timeline — days orbit a central hub like planets on a track.
 * Tap a node to inspect that day's venue, segments, and extra fields.
 */
@Composable
fun OrbitTimeline(
    days: List<EventTimelineDay>,
    accent: Color,
    startAt: Long = 0L,
    modifier: Modifier = Modifier,
    initialDay: Int = 1
) {
    if (days.isEmpty()) return

    var selectedDay by remember(days) { mutableIntStateOf(initialDay.coerceIn(1, days.size)) }
    val selected = days.firstOrNull { it.dayNumber == selectedDay } ?: days.first()
    val selectedAccent = orbitAccents[(selected.dayNumber - 1) % orbitAccents.size]

    val transition = rememberInfiniteTransition(label = "orbitSpin")
    val orbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(48000, easing = LinearEasing), RepeatMode.Restart),
        label = "orbit"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            val sizePx = with(LocalDensity.current) { maxWidth.toPx().coerceAtMost(maxHeight.toPx()) }
            val center = Offset(sizePx / 2f, sizePx / 2f)
            val orbitRadius = sizePx * 0.36f

            Canvas(modifier = Modifier.size(with(LocalDensity.current) { sizePx.toDp() })) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                        center = center,
                        radius = orbitRadius * 1.15f
                    ),
                    radius = orbitRadius * 1.15f,
                    center = center
                )
                rotate(orbitAngle, center) {
                    drawCircle(
                        color = accent.copy(alpha = 0.18f),
                        radius = orbitRadius,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        color = Arcade.NeonViolet.copy(alpha = 0.12f),
                        radius = orbitRadius * 0.72f,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                }
                days.forEachIndexed { index, day ->
                    val baseAngle = (index.toFloat() / days.size) * 360f - 90f
                    val angleRad = Math.toRadians((baseAngle + orbitAngle * 0.08f).toDouble())
                    val nodeCenter = Offset(
                        center.x + cos(angleRad).toFloat() * orbitRadius,
                        center.y + sin(angleRad).toFloat() * orbitRadius
                    )
                    val nodeAccent = orbitAccents[index % orbitAccents.size]
                    val isSelected = day.dayNumber == selectedDay
                    val nodeR = if (isSelected) 22f else 16f
                    if (isSelected) {
                        drawCircle(nodeAccent.copy(alpha = 0.35f * pulse), nodeR * 1.8f, nodeCenter)
                    }
                    drawCircle(nodeAccent.copy(alpha = if (isSelected) 0.95f else 0.65f), nodeR, nodeCenter)
                    drawCircle(Color.White.copy(alpha = 0.2f), nodeR * 0.35f, Offset(nodeCenter.x - 4f, nodeCenter.y - 4f))
                }
            }

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer { rotationZ = -orbitAngle * 0.04f }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.35f), Arcade.SpaceNavy.copy(alpha = 0.9f))
                        )
                    )
                    .border(2.dp, accent.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Day", color = Arcade.StarFaint, fontSize = 10.sp)
                    Text(
                        "${selected.dayNumber}",
                        color = Arcade.StarWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("of ${days.size}", color = Arcade.StarDim, fontSize = 10.sp)
                }
            }

            days.forEachIndexed { index, day ->
                val baseAngle = (index.toFloat() / days.size) * 360f - 90f
                val angleRad = Math.toRadians((baseAngle + orbitAngle * 0.08f).toDouble())
                val x = (sizePx / 2f + cos(angleRad).toFloat() * orbitRadius).roundToInt()
                val y = (sizePx / 2f + sin(angleRad).toFloat() * orbitRadius).roundToInt()
                val nodeAccent = orbitAccents[index % orbitAccents.size]
                val isSelected = day.dayNumber == selectedDay
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "nodeScale"
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x - 18, y - 18) }
                        .size(36.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .clip(CircleShape)
                        .clickable { selectedDay = day.dayNumber },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${day.dayNumber}",
                        color = if (isSelected) Arcade.StarWhite else nodeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        NeonGlassPanel(accent = selectedAccent, innerPadding = 16.dp) {
            Text(
                selected.title.ifBlank { "Day ${selected.dayNumber}" },
                color = Arcade.StarWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (selected.venue.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("📍 ${selected.venue}", color = Arcade.StarDim, fontSize = 13.sp)
            }
            if (selected.segments.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Schedule", color = selectedAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                selected.segments.forEach { seg ->
                    TimelineSegmentRow(seg.label, seg.startTime, seg.endTime, selectedAccent)
                }
            }
            selected.extraFields.filter { it.label.isNotBlank() || it.value.isNotBlank() }.forEach { field ->
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(field.label, color = Arcade.StarFaint, fontSize = 12.sp)
                    Text(field.value, color = Arcade.StarWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun TimelineSegmentRow(label: String, start: String, end: String, accent: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(
            label.ifBlank { "Session" },
            color = Arcade.StarWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val timeText = when {
            start.isNotBlank() && end.isNotBlank() -> "$start – $end"
            start.isNotBlank() -> start
            end.isNotBlank() -> end
            else -> ""
        }
        if (timeText.isNotBlank()) {
            Text(timeText, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
