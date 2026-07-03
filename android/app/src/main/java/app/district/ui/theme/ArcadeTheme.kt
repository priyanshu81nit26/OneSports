package app.district.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

/**
 * "Cosmic Arena" palette — deep-space neon-arcade look for District's space-themed UI.
 *
 * Colours are professional and tuned for contrast on a dark backdrop (WCAG AA against the deep
 * navy background for all text accents below).
 */
object Arcade {
    // ----- Deep space backdrop -----
    val SpaceBlack = Color(0xFF05030F)
    val SpaceNavy = Color(0xFF0B1030)
    val SpaceIndigo = Color(0xFF161B47)
    val SpaceViolet = Color(0xFF241A52)
    val NebulaPink = Color(0xFF3A1D4D)

    // ----- Neon accents -----
    val NeonCyan = Color(0xFF38F2E6)
    val NeonBlue = Color(0xFF4D8DFF)
    val NeonViolet = Color(0xFFA77BFF)
    val NeonMagenta = Color(0xFFFF5FD1)
    val NeonLime = Color(0xFF9BFF6B)
    val NeonAmber = Color(0xFFFFC94D)
    val NeonRed = Color(0xFFFF5C7A)

    // ----- Surfaces / text -----
    val GlassFill = Color(0x2EFFFFFF)        // ~18% white for frosted panels on dark
    val GlassFillSoft = Color(0x1FFFFFFF)    // ~12%
    val GlassStroke = Color(0x59FFFFFF)      // ~35% white hairline
    val StarWhite = Color(0xFFF4F7FF)
    val StarDim = Color(0xFFB9C2E6)
    val StarFaint = Color(0xFF7C86B8)

    /** Vertical deep-space gradient used as the arena backdrop. */
    val backdrop: Brush
        get() = Brush.verticalGradient(
            0f to SpaceBlack,
            0.35f to SpaceNavy,
            0.7f to SpaceIndigo,
            1f to SpaceViolet
        )

    /** Soft radial nebula glow, layered above [backdrop]. */
    fun nebula(center: Offset, radius: Float, tint: Color = NeonViolet): Brush =
        Brush.radialGradient(
            colors = listOf(tint.copy(alpha = 0.30f), tint.copy(alpha = 0.10f), Color.Transparent),
            center = center,
            radius = radius
        )

    /** Accent palette for category chips and highlights across District screens. */
    fun categoryAccent(name: String): Color = when (name.lowercase()) {
        "math", "equation" -> NeonBlue
        "quant" -> NeonAmber
        "memory" -> NeonAmber
        "logic" -> NeonLime
        "word", "story" -> NeonMagenta
        "wordgrid" -> NeonMagenta
        "code" -> NeonCyan
        "sudoku" -> NeonViolet
        "cryptic" -> NeonAmber
        "crossword" -> NeonBlue
        "path" -> NeonLime
        "piece" -> NeonViolet
        "spatial", "maze" -> NeonViolet
        "guess" -> NeonCyan
        "intuitive", "intuition" -> NeonMagenta
        else -> NeonCyan
    }

    /** Horizontal neon sweep used on primary action buttons. */
    fun neonSweep(accent: Color): Brush = Brush.horizontalGradient(
        listOf(accent, accent.copy(alpha = 0.65f), accent)
    )
}
