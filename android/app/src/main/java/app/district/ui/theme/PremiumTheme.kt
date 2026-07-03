package app.district.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium Glass White + Blue theme tokens. Used throughout the non-arcade (dashboard/settings/auth)
 * screens for a cohesive, modern, YC-level look:
 *
 * • Frosted white glass panels
 * • Soft blue accent gradients
 * • Large curved corners (less padding)
 * • 3D zoom-on-press interactions on every actionable card/button
 */
object Premium {
    // ---- Surface fills ----
    val GlassWhite = Color(0xF0FFFFFF)
    val GlassWhiteSoft = Color(0xD8FFFFFF)
    val GlassBlueTint = Color(0xFFF0F6FF)
    val GlassBlueLight = Color(0xFFE4EFFF)

    // ---- Accents (bluish) ----
    val Blue100 = Color(0xFFDAEBFF)
    val Blue200 = Color(0xFFB0D4FF)
    val Blue400 = Color(0xFF4DA3FF)
    val Blue500 = Color(0xFF2B8AFF)
    val Blue600 = Color(0xFF1A6FE0)
    val Blue700 = Color(0xFF0F53B0)

    val Cyan300 = Color(0xFF7FECF7)
    val Cyan400 = Color(0xFF3DD8E8)

    // ---- Supporting palette ----
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Neutral50 = Color(0xFFF9FAFB)
    val Neutral100 = Color(0xFFF3F4F6)
    val Neutral200 = Color(0xFFE5E7EB)
    val Neutral400 = Color(0xFF9CA3AF)
    val Neutral600 = Color(0xFF4B5563)
    val Neutral800 = Color(0xFF1F2937)
    val Neutral900 = Color(0xFF111827)

    // ---- Gradients ----
    val backgroundGradient: Brush
        get() = Brush.verticalGradient(
            0f to Color(0xFFEEF5FF),
            0.4f to Color(0xFFF5F9FF),
            1f to GlassBlueTint
        )

    val cardGradient: Brush
        get() = Brush.verticalGradient(
            listOf(GlassWhite, GlassWhiteSoft)
        )

    val blueAccentGradient: Brush
        get() = Brush.horizontalGradient(
            listOf(Blue500, Cyan400, Blue400)
        )

    val subtleBlueGradient: Brush
        get() = Brush.linearGradient(
            listOf(Blue100.copy(alpha = 0.6f), Blue200.copy(alpha = 0.3f))
        )

    // ---- Border ----
    val GlassBorder = Color(0xFFDBE8F7)
    val BlueBorder = Color(0xFFB8D4F5)

    // ---- Corner radii (curvy) ----
    val RadiusXS = 14.dp
    val RadiusSM = 18.dp
    val RadiusMD = 24.dp
    val RadiusLG = 30.dp
    val RadiusXL = 36.dp
    val RadiusXXL = 42.dp

    // ---- Spacing (less padding) ----
    val PadXS = 6.dp
    val PadSM = 10.dp
    val PadMD = 14.dp
    val PadLG = 18.dp
    val PadXL = 22.dp
}

/**
 * A premium 3D zoom-on-press button/card that scales up slightly on press then bounces back,
 * creating a tactile "popping" feel consistent with a polished mobile experience.
 *
 * @param zoomScale how much to scale UP on press (default 1.05 = 5% zoom-in)
 * @param tiltDegrees the subtle rotationX tilt on press (3D perspective pop)
 */
@Composable
fun ZoomPressCard(
    modifier: Modifier = Modifier,
    accentColor: Color = Premium.Blue500,
    cornerRadius: Dp = Premium.RadiusLG,
    innerPadding: Dp = Premium.PadMD,
    enabled: Boolean = true,
    zoomScale: Float = 1.03f,
    tiltDegrees: Float = 2f,
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val active = pressed && enabled
    val density = LocalDensity.current

    val scale by animateFloatAsState(
        targetValue = if (active) zoomScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "zoomScale"
    )
    val tilt by animateFloatAsState(
        targetValue = if (active) tiltDegrees else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "zoomTilt"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationX = tilt
                cameraDistance = 14f * density.density
            }
            .shadow(
                if (active) 10.dp else 6.dp,
                shape,
                clip = false,
                ambientColor = accentColor.copy(alpha = 0.12f),
                spotColor = accentColor.copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(Premium.cardGradient)
            .border(1.dp, Premium.GlassBorder, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(innerPadding),
        content = content
    )
}

/**
 * Premium action button with glass white+blue gradient fill, rounded curves, and 3D press.
 */
@Composable
fun PremiumButton(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Premium.Blue500,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(Premium.RadiusMD)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val active = pressed && enabled
    val density = LocalDensity.current

    val scale by animateFloatAsState(
        targetValue = if (active) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btnScale"
    )
    val tilt by animateFloatAsState(
        targetValue = if (active) 2f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "btnTilt"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationX = tilt
                cameraDistance = 16f * density.density
            }
            .shadow(8.dp, shape, clip = false, ambientColor = accentColor.copy(alpha = 0.14f), spotColor = accentColor.copy(alpha = 0.10f))
            .clip(shape)
            .background(
                if (enabled) Brush.horizontalGradient(listOf(accentColor, accentColor.copy(alpha = 0.8f)))
                else Brush.horizontalGradient(listOf(Premium.Neutral200, Premium.Neutral200))
            )
            .border(1.dp, if (enabled) accentColor.copy(alpha = 0.4f) else Premium.Neutral200, shape)
            .clickable(enabled = enabled, interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) Color.White else Premium.Neutral400,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

/**
 * Premium glass surface without interaction (for static info panels).
 */
@Composable
fun PremiumGlassPane(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Premium.RadiusLG,
    innerPadding: Dp = Premium.PadLG,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(4.dp, shape, clip = false, ambientColor = Premium.Blue400.copy(alpha = 0.10f), spotColor = Premium.Blue400.copy(alpha = 0.06f))
            .clip(shape)
            .background(Premium.cardGradient)
            .border(1.dp, Premium.GlassBorder, shape)
            .padding(innerPadding),
        content = content
    )
}

/**
 * Premium blue-tinted accent card for highlighted sections.
 */
@Composable
fun PremiumAccentCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Premium.RadiusLG,
    innerPadding: Dp = Premium.PadMD,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(6.dp, shape, clip = false, ambientColor = Premium.Blue500.copy(alpha = 0.10f), spotColor = Premium.Blue500.copy(alpha = 0.06f))
            .clip(shape)
            .background(Premium.subtleBlueGradient)
            .border(1.dp, Premium.BlueBorder, shape)
            .padding(innerPadding),
        content = content
    )
}
