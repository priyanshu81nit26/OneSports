package app.district.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.district.data.DistrictEvent
import app.district.ui.theme.Arcade

/**
 * Compact grid tile — event type centred in bold, organiser / venue / prize below.
 */
@Composable
fun EventGridCard(
    event: DistrictEvent,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = Arcade.NeonCyan
) {
    NeonGlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.92f)
            .clickable { onClick(event.id) },
        accent = accent,
        innerPadding = 14.dp,
        cornerRadius = 22.dp
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(event.imageEmoji.ifBlank { event.category.emoji }, fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                event.category.label.uppercase(),
                color = Arcade.StarWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            if (event.hasTimeline) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "${event.dayCount} day mission",
                    color = accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                event.organizerName.ifBlank { "Organiser" },
                color = Arcade.StarDim,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                event.displayVenue.ifBlank { "Venue TBD" },
                color = Arcade.StarFaint,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )
            if (event.prize.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "🏆 ${event.prize}",
                    color = Arcade.NeonAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
