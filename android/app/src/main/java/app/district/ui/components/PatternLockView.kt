package app.district.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import app.district.ui.theme.Indigo400
import app.district.ui.theme.Slate400
import kotlin.math.hypot

/**
 * A 3x3 Android-style pattern lock. Emits the connected dot indices (0..8, row-major)
 * when the user lifts their finger. Re-keying [resetSignal] clears the drawn path.
 */
@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    dotColor: Color = Slate400,
    selectedColor: Color = Indigo400,
    resetSignal: Int = 0,
    onPatternComplete: (List<Int>) -> Unit
) {
    val selected = remember { mutableStateListOf<Int>() }
    var currentPoint by remember { mutableStateOf<Offset?>(null) }

    androidx.compose.runtime.LaunchedEffect(resetSignal) {
        selected.clear()
        currentPoint = null
    }

    fun dotCenters(width: Float, height: Float): List<Offset> {
        val cells = 3
        val cellW = width / cells
        val cellH = height / cells
        return buildList {
            for (row in 0 until cells) {
                for (col in 0 until cells) {
                    add(Offset(cellW * col + cellW / 2f, cellH * row + cellH / 2f))
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(resetSignal) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selected.clear()
                        currentPoint = offset
                        val centers = dotCenters(this.size.width.toFloat(), this.size.height.toFloat())
                        hitDot(centers, offset)?.let { if (it !in selected) selected.add(it) }
                    },
                    onDrag = { change, _ ->
                        currentPoint = change.position
                        val centers = dotCenters(this.size.width.toFloat(), this.size.height.toFloat())
                        hitDot(centers, change.position)?.let { if (it !in selected) selected.add(it) }
                    },
                    onDragEnd = {
                        currentPoint = null
                        if (selected.isNotEmpty()) onPatternComplete(selected.toList())
                    },
                    onDragCancel = {
                        currentPoint = null
                    }
                )
            }
    ) {
        val centers = dotCenters(this.size.width, this.size.height)
        val radius = (this.size.minDimension / 3f) * 0.16f

        // Connecting lines between selected dots.
        for (i in 0 until selected.size - 1) {
            drawLine(
                color = selectedColor,
                start = centers[selected[i]],
                end = centers[selected[i + 1]],
                strokeWidth = radius * 0.5f
            )
        }
        // Trailing line to the finger.
        val last = selected.lastOrNull()
        val point = currentPoint
        if (last != null && point != null) {
            drawLine(
                color = selectedColor.copy(alpha = 0.5f),
                start = centers[last],
                end = point,
                strokeWidth = radius * 0.5f
            )
        }
        // Dots.
        centers.forEachIndexed { index, center ->
            val isSel = index in selected
            drawCircle(
                color = if (isSel) selectedColor else dotColor.copy(alpha = 0.4f),
                radius = if (isSel) radius * 1.3f else radius,
                center = center
            )
        }
    }
}

private fun hitDot(centers: List<Offset>, point: Offset): Int? {
    var best: Int? = null
    var bestDist = Float.MAX_VALUE
    centers.forEachIndexed { index, center ->
        val d = hypot(point.x - center.x, point.y - center.y)
        if (d < bestDist) {
            bestDist = d
            best = index
        }
    }
    // Require the touch to be reasonably close to a dot before registering it.
    val cell = if (centers.size >= 2) hypot(
        centers[1].x - centers[0].x,
        centers[1].y - centers[0].y
    ) else 0f
    return if (bestDist <= cell * 0.4f) best else null
}
