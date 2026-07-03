package app.district.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.district.ui.theme.Rise

enum class RiseButtonStyle {
    Primary,
    Secondary,
    Outlined
}

@Composable
fun RiseAnimatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: RiseButtonStyle = RiseButtonStyle.Primary,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale = when {
        !enabled -> 1f
        isPressed -> 0.94f
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "btnScale"
    )
    val infinite = rememberInfiniteTransition(label = "borderGlow")
    val glow by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val shape = RoundedCornerShape(28.dp)
    val resolvedContent = contentColor ?: when (style) {
        RiseButtonStyle.Primary -> Rise.Black
        RiseButtonStyle.Secondary -> Rise.TextPrimary
        RiseButtonStyle.Outlined -> Rise.AccentSoft
    }
    val fillBrush = when {
        !enabled -> Brush.linearGradient(listOf(Rise.CardElevated, Rise.Card))
        containerColor != null -> Brush.linearGradient(listOf(containerColor, containerColor))
        style == RiseButtonStyle.Outlined -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
        style == RiseButtonStyle.Secondary -> Brush.linearGradient(
            listOf(Rise.CardElevated, Rise.Card.copy(alpha = 0.92f))
        )
        else -> Brush.linearGradient(
            listOf(
                Rise.AccentSoft.copy(alpha = 0.95f + glow * 0.05f),
                Rise.Accent.copy(alpha = 0.88f),
                Rise.TextPrimary.copy(alpha = 0.92f)
            )
        )
    }
    val borderBrush = when {
        !enabled -> Brush.linearGradient(listOf(Rise.Border, Rise.Border))
        style == RiseButtonStyle.Outlined -> Brush.linearGradient(
            listOf(
                Rise.AccentSoft.copy(alpha = glow),
                Rise.Accent.copy(alpha = glow * 0.75f),
                Rise.AccentSoft.copy(alpha = glow)
            )
        )
        else -> Brush.linearGradient(
            listOf(
                Rise.AccentSoft.copy(alpha = glow * 0.55f),
                Rise.Accent.copy(alpha = glow * 0.35f),
                Rise.AccentSoft.copy(alpha = glow * 0.55f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (enabled) (if (isPressed) 14.dp else 10.dp) else 0.dp,
                shape = shape,
                ambientColor = Rise.AccentSoft.copy(alpha = 0.28f),
                spotColor = Rise.Accent.copy(alpha = 0.22f)
            )
            .clip(shape)
            .border(1.5.dp, borderBrush, shape)
            .background(fillBrush)
            .height(54.dp)
            .semantics { role = Role.Button }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) resolvedContent else Rise.TextMuted,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
