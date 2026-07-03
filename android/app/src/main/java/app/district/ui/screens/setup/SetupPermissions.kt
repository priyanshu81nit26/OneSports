package app.district.ui.screens.setup

import android.content.Context
import android.content.Intent

/** District does not require usage-stats or overlay permissions. */
fun hasUsageStatsPermission(context: Context): Boolean = true

fun hasOverlayPermission(context: Context): Boolean = true

fun overlayPermissionIntent(context: Context): Intent = Intent()
