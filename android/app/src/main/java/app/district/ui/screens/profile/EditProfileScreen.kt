package app.district.ui.screens.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.data.PrefsManager
import app.district.ui.components.RiseAnimatedButton
import app.district.ui.components.RiseAuthCard
import app.district.ui.components.RiseAuthLayout
import app.district.ui.components.RiseInstaPermissionDialog
import app.district.ui.components.riseFieldColors
import app.district.ui.theme.Rise
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    val context = LocalContext.current
    var username by remember(ui.profile) { mutableStateOf(ui.profile?.username.orEmpty()) }
    var bio by remember(ui.profile) { mutableStateOf(ui.profile?.bio.orEmpty()) }
    var photoUri by remember(ui.profile) { mutableStateOf(ui.profile?.photoUri.orEmpty()) }
    var showPermissionDialog by remember { mutableStateOf<PendingMediaAction?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) photoUri = uri.toString()
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) photoUri = saveProfileBitmap(context, bitmap)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val action = showPermissionDialog
        showPermissionDialog = null
        if (granted) {
            when (action) {
                PendingMediaAction.GALLERY -> galleryLauncher.launch("image/*")
                PendingMediaAction.CAMERA -> cameraLauncher.launch(null)
                null -> Unit
            }
        }
    }

    fun openGallery() {
        val permission = galleryPermission()
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*")
        } else {
            showPermissionDialog = PendingMediaAction.GALLERY
        }
    }

    fun openCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            showPermissionDialog = PendingMediaAction.CAMERA
        }
    }

    RiseAuthLayout(showLogo = false) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
            }
            Text("Set profile", style = MaterialTheme.typography.headlineMedium, color = Rise.TextPrimary)
        }
        Spacer(Modifier.height(16.dp))

        when {
            ui.loading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Rise.AccentSoft)
            }
            else -> Column(Modifier.verticalScroll(rememberScrollState())) {
                RiseAuthCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (photoUri.isNotBlank()) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(72.dp).clip(CircleShape)
                                )
                            } else {
                                Box(
                                    Modifier.size(72.dp).clip(CircleShape).background(Rise.CardElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, null, tint = Rise.TextSecondary, modifier = Modifier.size(32.dp))
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { openGallery() },
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) { Text("Gallery", color = Rise.TextPrimary) }
                                OutlinedButton(
                                    onClick = { openCamera() },
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) { Text("Camera", color = Rise.TextPrimary) }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                if (ui.canChangeUsername) username = PrefsManager.normalizeUsername(it).take(20)
                            },
                            enabled = ui.canChangeUsername,
                            label = { Text("Username") },
                            prefix = { Text("@", color = Rise.TextMuted) },
                            supportingText = {
                                if (!ui.canChangeUsername) {
                                    Text("Change again in ${ui.usernameDaysLeft} days", color = Rise.TextMuted)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = riseFieldColors()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { if (it.length <= 160) bio = it },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            colors = riseFieldColors()
                        )
                        ui.error?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = Rise.Danger, style = MaterialTheme.typography.bodySmall)
                        }
                        if (ui.saved) {
                            Spacer(Modifier.height(8.dp))
                            Text("Profile saved", color = Rise.Success, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(18.dp))
                        RiseAnimatedButton(
                            text = if (ui.saving) "Saving…" else "Save profile",
                            enabled = !ui.saving && username.length >= 6,
                            onClick = { viewModel.save(username, bio, photoUri) }
                        )
                    }
                }
            }
        }
    }

    showPermissionDialog?.let { action ->
        val (title, message, permission) = when (action) {
            PendingMediaAction.GALLERY -> Triple(
                "Allow photo access",
                "Rise needs access to your photos so you can choose a profile picture.",
                galleryPermission()
            )
            PendingMediaAction.CAMERA -> Triple(
                "Allow camera access",
                "Rise needs camera access so you can take a profile photo.",
                Manifest.permission.CAMERA
            )
        }
        RiseInstaPermissionDialog(
            title = title,
            message = message,
            onAllow = { permissionLauncher.launch(permission) },
            onDeny = { showPermissionDialog = null }
        )
    }
}

private enum class PendingMediaAction { GALLERY, CAMERA }

private fun galleryPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
    else Manifest.permission.READ_EXTERNAL_STORAGE

private fun saveProfileBitmap(context: Context, bitmap: Bitmap): String {
    val dir = File(context.filesDir, "profile_photos").apply { mkdirs() }
    val file = File(dir, "profile_${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
    return file.absolutePath
}
