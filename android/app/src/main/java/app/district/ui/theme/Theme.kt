package app.district.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Slate900 = Color(0xFF1B2433)
val Slate800 = Color(0xFF2F3A4A)
val Slate700 = Color(0xFFD6E3F6)
val Slate600 = Color(0xFF8291A8)
val Slate400 = Color(0xFF6F7F96)
val Slate200 = Color(0xFFEAF2FF)
val Slate50 = Color(0xFFFFFFFF)

val Indigo500 = Color(0xFF147CE5)
val Indigo400 = Color(0xFF2F9AF7)
val Indigo300 = Color(0xFF66B7FF)
val Indigo200 = Color(0xFFD9EBFF)

val Emerald500 = Color(0xFF10B981)
val Emerald400 = Color(0xFF34D399)

val Rose500 = Color(0xFFF43F5E)
val Rose400 = Color(0xFFFB7185)

val Amber500 = Color(0xFFF59E0B)
val Amber400 = Color(0xFFFBBF24)

val GlassWhite = Color(0xDFFFFFFF)
val GlassBorder = Color(0x99FFFFFF)

// Premium bluish accents surfaced in MaterialTheme for easy access
val PremiumBlue = Color(0xFF2B8AFF)
val PremiumBlueDark = Color(0xFF1A6FE0)
val PremiumBlueLight = Color(0xFF4DA3FF)
val PremiumSurface = Color(0xFFF5F9FF)

private val DistrictColorScheme = lightColorScheme(
    primary = PremiumBlue,
    onPrimary = Slate50,
    primaryContainer = PremiumBlueDark,
    onPrimaryContainer = Slate50,
    secondary = Emerald400,
    onSecondary = Slate900,
    secondaryContainer = Emerald500,
    tertiary = Amber400,
    onTertiary = Slate900,
    background = Color(0xFFEEF5FF),
    onBackground = Slate900,
    surface = PremiumSurface,
    onSurface = Slate900,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400,
    error = Rose500,
    onError = Slate50,
    outline = Slate600,
    outlineVariant = GlassBorder,
)

private val DistrictShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun DistrictTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DistrictColorScheme,
        shapes = DistrictShapes,
        typography = DistrictTypography,
        content = content,
    )
}
