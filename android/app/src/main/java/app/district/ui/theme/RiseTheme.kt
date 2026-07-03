package app.district.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** District-inspired dark palette for Rise. */
object Rise {
    val Black = Color(0xFF000000)
    val Surface = Color(0xFF121212)
    val Card = Color(0xFF1C1C1E)
    val CardElevated = Color(0xFF2C2C2E)
    val Border = Color(0xFF3A3A3C)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0B0B5)
    val TextMuted = Color(0xFF8E8E93)
    val Accent = Color(0xFFE84393)
    val AccentSoft = Color(0xFFBB86FC)
    val Success = Color(0xFF34C759)
    val Danger = Color(0xFFFF453A)
    val ChipOutline = Color(0xFF48484A)
    val ProfileGradientStart = Color(0xFF3D1F54)
    val ProfileGradientEnd = Color(0xFF121212)
}

private val RiseDarkScheme = darkColorScheme(
    primary = Rise.AccentSoft,
    onPrimary = Rise.Black,
    secondary = Rise.Accent,
    onSecondary = Rise.TextPrimary,
    background = Rise.Black,
    onBackground = Rise.TextPrimary,
    surface = Rise.Surface,
    onSurface = Rise.TextPrimary,
    surfaceVariant = Rise.Card,
    onSurfaceVariant = Rise.TextSecondary,
    outline = Rise.Border,
    error = Rise.Danger,
    onError = Rise.TextPrimary
)

private val RiseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun RiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RiseDarkScheme,
        shapes = RiseShapes,
        typography = DistrictTypography,
        content = content
    )
}

