package app.district.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.district.ui.theme.Arcade
import kotlin.math.cos
import kotlin.math.sin

/** Frosted dark panel with a neon hairline + soft outer glow. The arena's primary surface. */
@Composable
fun NeonGlassPanel(
    modifier: Modifier = Modifier,
    accent: Color = Arcade.NeonCyan,
    cornerRadius: Dp = 28.dp,
    innerPadding: Dp = 18.dp,
    glow: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .then(
                if (glow) Modifier.shadow(22.dp, shape, clip = false, ambientColor = accent.copy(alpha = 0.5f), spotColor = accent.copy(alpha = 0.35f))
                else Modifier
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Arcade.GlassFill, Arcade.GlassFillSoft)
                )
            )
            .border(BorderStroke(1.dp, Brush.linearGradient(listOf(accent.copy(alpha = 0.8f), Arcade.GlassStroke))), shape)
            .padding(innerPadding),
        content = content
    )
}

/**
 * Primary arcade action button. Applies a real 3D press (scale + rotationX tilt + lift) plus a
 * pulsing neon glow so taps feel tactile and game-like.
 */
@Composable
fun ArcadeButton(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = Arcade.NeonCyan,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val active = pressed && enabled
    val density = LocalDensity.current

    val scale by animateFloatAsState(
        targetValue = if (active) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "arcadeScale"
    )
    val tilt by animateFloatAsState(
        targetValue = if (active) 6f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "arcadeTilt"
    )
    val pulse by rememberInfiniteTransition(label = "btnPulse").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "btnGlow"
    )
    val glowAlpha = if (enabled) pulse else 0.2f
    val fillColor = if (enabled) accent else accent.copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationX = tilt
                cameraDistance = 16f * density.density
            }
            .shadow(18.dp, shape, clip = false, ambientColor = accent.copy(alpha = glowAlpha), spotColor = accent.copy(alpha = glowAlpha))
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(fillColor.copy(alpha = 0.18f), fillColor.copy(alpha = 0.30f), fillColor.copy(alpha = 0.18f))))
            .border(BorderStroke(1.5.dp, fillColor), shape)
            .clickable(enabled = enabled, interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) Arcade.StarWhite else Arcade.StarFaint,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

enum class OptionState { IDLE, SELECTED, CORRECT, WRONG }

/** A selectable answer tile with animated colour/elevation feedback for correct/wrong reveals. */
@Composable
fun ArcadeOptionTile(
    label: String,
    state: OptionState,
    accent: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val target = when (state) {
        OptionState.CORRECT -> Arcade.NeonLime
        OptionState.WRONG -> Arcade.NeonRed
        OptionState.SELECTED -> accent
        OptionState.IDLE -> Arcade.GlassStroke
    }
    val borderColor by animateColorAsState(target, tween(220), label = "optBorder")
    val fillAlpha by animateFloatAsState(
        targetValue = when (state) {
            OptionState.IDLE -> 0.10f
            else -> 0.22f
        },
        animationSpec = tween(220),
        label = "optFill"
    )
    val scale by animateFloatAsState(
        targetValue = if (state == OptionState.CORRECT) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "optScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (state == OptionState.IDLE) 0.dp else 14.dp, shape, clip = false, ambientColor = target.copy(alpha = 0.5f), spotColor = target.copy(alpha = 0.4f))
            .clip(shape)
            .background(target.copy(alpha = fillAlpha))
            .border(BorderStroke(1.dp, borderColor), shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = Arcade.StarWhite,
            fontWeight = if (state == OptionState.IDLE) FontWeight.Medium else FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

/** Small HUD chip used in the arena header for stats (level, age band, streak, etc). */
@Composable
fun HudPill(text: String, accent: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.16f))
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.6f)), shape)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** Celebration particle burst drawn over the "passed" panel. */
@Composable
fun CelebrationBurst(modifier: Modifier = Modifier) {
    val colors = listOf(Arcade.NeonCyan, Arcade.NeonMagenta, Arcade.NeonAmber, Arcade.NeonLime, Arcade.NeonViolet)
    val progress by rememberInfiniteTransition(label = "burst").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Restart),
        label = "burstP"
    )
    val particles = remember {
        List(28) { i ->
            val angle = (i / 28f) * 6.28f
            Triple(cos(angle), sin(angle), colors[i % colors.size])
        }
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.32f
        val maxR = size.minDimension * 0.42f
        particles.forEach { (dx, dy, color) ->
            val r = maxR * progress
            val alpha = (1f - progress).coerceIn(0f, 1f)
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = 5f * (1f - progress * 0.5f),
                center = Offset(cx + dx * r, cy + dy * r)
            )
        }
    }
}
