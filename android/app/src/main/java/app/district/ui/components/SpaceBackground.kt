package app.district.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import app.district.ui.theme.Arcade
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val radius: Float, val phase: Float, val twinkle: Float)
private data class Planet(val x: Float, val y: Float, val radius: Float, val color: Color, val ringColor: Color, val orbit: Float)

/**
 * Full-screen animated cosmic backdrop for District. Pure Canvas drawing (no image
 * assets) so it is light-weight and always available. Layers:
 *  - deep-space gradient + two nebula glows
 *  - parallax twinkling star field
 *  - slowly orbiting planets
 *  - a drifting astronaut/rocket "mascot" character
 *  - periodic shooting stars
 *
 * Place arena content inside via the [content] slot (drawn above the background).
 */
@Composable
fun SpaceBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 90,
    content: @Composable BoxScope.() -> Unit
) {
    val stars = remember {
        val rng = Random(42)
        List(starCount) {
            Star(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                radius = 0.6f + rng.nextFloat() * 1.8f,
                phase = rng.nextFloat() * 6.28f,
                twinkle = 0.4f + rng.nextFloat() * 0.6f
            )
        }
    }
    val planets = remember {
        listOf(
            Planet(0.16f, 0.14f, 26f, Arcade.NeonViolet, Arcade.NeonMagenta, 0.018f),
            Planet(0.86f, 0.30f, 40f, Arcade.NeonBlue, Arcade.NeonCyan, 0.012f),
            Planet(0.74f, 0.82f, 18f, Arcade.NeonAmber, Arcade.NeonAmber, 0.022f)
        )
    }

    val transition = rememberInfiniteTransition(label = "space")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "spaceClock"
    )
    val twinkleClock by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "twinkle"
    )
    // Mascot travels a gentle figure-eight-ish path across the sky.
    val mascotProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing), RepeatMode.Restart),
        label = "mascot"
    )
    val shootProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "shoot"
    )

    Box(modifier = modifier.background(Arcade.backdrop)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(brush = Arcade.nebula(Offset(w * 0.2f, h * 0.18f), w * 0.7f, Arcade.NeonViolet))
            drawRect(brush = Arcade.nebula(Offset(w * 0.85f, h * 0.7f), w * 0.6f, Arcade.NeonBlue))

            // Star field with twinkle.
            stars.forEach { star ->
                val alpha = (0.35f + 0.65f * ((sin(twinkleClock + star.phase) + 1f) / 2f)) * star.twinkle
                drawCircle(
                    color = Arcade.StarWhite.copy(alpha = alpha.coerceIn(0.05f, 1f)),
                    radius = star.radius,
                    center = Offset(star.x * w, star.y * h)
                )
            }

            // Orbiting planets.
            planets.forEach { planet ->
                val angle = t * 6.28f
                val cx = planet.x * w + cos(angle) * (planet.orbit * w)
                val cy = planet.y * h + sin(angle) * (planet.orbit * w)
                drawCircle(planet.color.copy(alpha = 0.18f), planet.radius * 1.8f, Offset(cx, cy))
                drawCircle(planet.color, planet.radius, Offset(cx, cy))
                drawCircle(Color.White.copy(alpha = 0.25f), planet.radius * 0.45f, Offset(cx - planet.radius * 0.35f, cy - planet.radius * 0.35f))
                rotate(degrees = t * 360f, pivot = Offset(cx, cy)) {
                    drawOval(
                        color = planet.ringColor.copy(alpha = 0.5f),
                        topLeft = Offset(cx - planet.radius * 1.9f, cy - planet.radius * 0.5f),
                        size = androidx.compose.ui.geometry.Size(planet.radius * 3.8f, planet.radius * 1.0f),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Shooting star (sweeps across once per loop).
            if (shootProgress < 0.4f) {
                val p = shootProgress / 0.4f
                val sx = w * (0.1f + p * 0.8f)
                val sy = h * (0.12f + p * 0.25f)
                val tailX = sx - 60f
                val tailY = sy - 26f
                drawLine(
                    color = Arcade.NeonCyan.copy(alpha = (1f - p) * 0.9f),
                    start = Offset(tailX, tailY),
                    end = Offset(sx, sy),
                    strokeWidth = 3f
                )
                drawCircle(Arcade.StarWhite.copy(alpha = 1f - p), 3.2f, Offset(sx, sy))
            }

            // Mascot: a small rocket/astronaut pod drifting on a smooth path.
            val mx = w * (0.5f + 0.34f * sin(mascotProgress))
            val my = h * (0.42f + 0.20f * sin(mascotProgress * 2f))
            val bob = sin(mascotProgress * 4f) * 4f
            drawMascot(Offset(mx, my + bob), tilt = cos(mascotProgress) * 14f)
        }
        content()
    }
}

/** Draws a simple, friendly astronaut-pod mascot (helmet + body + thruster glow). */
private fun DrawScope.drawMascot(center: Offset, tilt: Float) {
    rotate(degrees = tilt, pivot = center) {
        // thruster glow
        drawCircle(Arcade.NeonAmber.copy(alpha = 0.35f), 16f, Offset(center.x, center.y + 26f))
        drawCircle(Arcade.NeonAmber.copy(alpha = 0.85f), 7f, Offset(center.x, center.y + 24f))
        // body
        drawCircle(Arcade.NeonCyan.copy(alpha = 0.20f), 30f, center)
        drawRoundRectPod(center)
        // helmet glass
        drawCircle(Arcade.StarWhite, 13f, Offset(center.x, center.y - 6f))
        drawCircle(Arcade.SpaceNavy, 10f, Offset(center.x, center.y - 6f))
        drawCircle(Arcade.NeonCyan.copy(alpha = 0.9f), 4f, Offset(center.x - 3f, center.y - 9f))
    }
}

private fun DrawScope.drawRoundRectPod(center: Offset) {
    drawCircle(Arcade.StarWhite, 18f, Offset(center.x, center.y + 4f))
    drawCircle(Arcade.NeonBlue.copy(alpha = 0.85f), 14f, Offset(center.x, center.y + 4f))
}
