package app.district.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

/**
 * Responsive layout utilities for consistent UX across phone sizes and orientations.
 *
 * Usage:
 *   val dimens = rememberResponsiveDimens()
 *   Modifier.padding(dimens.screenPad)
 *   Text(fontSize = dimens.titleSize)
 */
enum class ScreenClass { COMPACT, MEDIUM, EXPANDED }

data class ResponsiveDimens(
    val screenClass: ScreenClass,
    val screenPad: Dp,
    val cardPad: Dp,
    val sectionGap: Dp,
    val titleSize: TextUnit,
    val bodySize: TextUnit,
    val smallSize: TextUnit,
    val iconSizeLg: Dp,
    val iconSizeMd: Dp,
    val cornerRadius: Dp,
    val buttonHeight: Dp
)

@Composable
fun rememberResponsiveDimens(): ResponsiveDimens {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp

    return when {
        widthDp < 360 -> ResponsiveDimens(
            screenClass = ScreenClass.COMPACT,
            screenPad = 14.dp,
            cardPad = 12.dp,
            sectionGap = 12.dp,
            titleSize = 20.sp,
            bodySize = 13.sp,
            smallSize = 11.sp,
            iconSizeLg = 40.dp,
            iconSizeMd = 24.dp,
            cornerRadius = 20.dp,
            buttonHeight = 48.dp
        )
        widthDp < 600 -> ResponsiveDimens(
            screenClass = ScreenClass.MEDIUM,
            screenPad = 20.dp,
            cardPad = 16.dp,
            sectionGap = 16.dp,
            titleSize = 24.sp,
            bodySize = 14.sp,
            smallSize = 12.sp,
            iconSizeLg = 52.dp,
            iconSizeMd = 28.dp,
            cornerRadius = 28.dp,
            buttonHeight = 56.dp
        )
        else -> ResponsiveDimens(
            screenClass = ScreenClass.EXPANDED,
            screenPad = 28.dp,
            cardPad = 20.dp,
            sectionGap = 20.dp,
            titleSize = 28.sp,
            bodySize = 15.sp,
            smallSize = 13.sp,
            iconSizeLg = 64.dp,
            iconSizeMd = 32.dp,
            cornerRadius = 34.dp,
            buttonHeight = 62.dp
        )
    }
}
