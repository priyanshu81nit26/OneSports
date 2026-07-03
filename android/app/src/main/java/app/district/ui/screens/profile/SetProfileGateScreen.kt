package app.district.ui.screens.profile

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.district.ui.components.RiseInstaPermissionDialog
import app.district.ui.theme.Rise

@Composable
fun SetProfileGateScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }

    val permissions = remember {
        buildList {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        showDialog = false
        onContinue()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Rise.Black.copy(alpha = 0.92f))
    )

    if (showDialog) {
        RiseInstaPermissionDialog(
            title = "Set up your profile",
            message = "Rise needs camera and photo access so you can add a profile picture from your gallery or camera.",
            onAllow = { permissionLauncher.launch(permissions) },
            onDeny = {
                showDialog = false
                onBack()
            }
        )
    }
}
