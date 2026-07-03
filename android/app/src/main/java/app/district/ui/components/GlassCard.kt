package app.district.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.district.ui.theme.GlassBorder
import app.district.ui.theme.GlassWhite
import app.district.ui.theme.Slate400

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(6.dp, shape, clip = false, ambientColor = Color(0x1A2F9AF7), spotColor = Color(0x142F9AF7))
            .clip(shape)
            .background(GlassWhite)
            .border(1.dp, GlassBorder, shape)
            .padding(18.dp),
        content = content
    )
}

@Composable
fun PremiumActionSurface(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF111111),
    selected: Boolean = false,
    enabled: Boolean = true,
    cornerRadius: Dp = 28.dp,
    innerPadding: Dp = 16.dp,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val activePress = pressed && enabled
    val density = LocalDensity.current
    val scale by animateFloatAsState(
        targetValue = if (activePress) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "premiumActionScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (activePress) 2.5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "premiumActionTilt"
    )
    val lift by animateDpAsState(
        targetValue = if (activePress) 2.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "premiumActionLift"
    )
    val elevation by animateDpAsState(
        targetValue = when {
            !enabled -> 2.dp
            activePress -> 6.dp
            selected -> 12.dp
            else -> 8.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "premiumActionElevation"
    )
    val fill = when {
        !enabled -> GlassWhite.copy(alpha = 0.55f)
        selected -> accentColor.copy(alpha = 0.10f)
        else -> GlassWhite
    }
    val border = when {
        !enabled -> GlassBorder.copy(alpha = 0.55f)
        selected -> accentColor.copy(alpha = 0.30f)
        activePress -> accentColor.copy(alpha = 0.28f)
        else -> GlassBorder
    }
    val alpha = if (enabled) 1f else 0.62f
    val translationY = with(density) { lift.toPx() }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationX = rotation
                this.translationY = translationY
                cameraDistance = 18f * density.density
                this.alpha = alpha
            }
            .shadow(elevation, shape, clip = false, ambientColor = accentColor.copy(alpha = 0.10f), spotColor = accentColor.copy(alpha = 0.08f))
            .clip(shape)
            .background(fill)
            .border(1.dp, border, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(innerPadding),
        content = content
    )
}

@Composable
fun PremiumStaticSurface(
    modifier: Modifier = Modifier,
    accentColor: Color = Slate400,
    cornerRadius: Dp = 26.dp,
    innerPadding: Dp = 16.dp,
    elevated: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(
                if (elevated) 8.dp else 3.dp,
                shape,
                clip = false,
                ambientColor = accentColor.copy(alpha = 0.10f),
                spotColor = accentColor.copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(GlassWhite)
            .border(1.dp, accentColor.copy(alpha = 0.16f), shape)
            .padding(innerPadding),
        content = content
    )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    innerPadding: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(4.dp, shape, clip = false, ambientColor = Color(0x1A2F9AF7), spotColor = Color(0x142F9AF7))
            .clip(shape)
            .background(GlassWhite)
            .border(0.5.dp, GlassBorder, shape)
            .padding(innerPadding),
        content = content
    )
}

@Composable
fun AccentGlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color,
    cornerRadius: Dp = 28.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(6.dp, shape, clip = false, ambientColor = accentColor.copy(alpha = 0.12f), spotColor = accentColor.copy(alpha = 0.08f))
            .clip(shape)
            .background(accentColor.copy(alpha = 0.06f))
            .border(1.dp, accentColor.copy(alpha = 0.20f), shape)
            .padding(16.dp),
        content = content
    )
}
