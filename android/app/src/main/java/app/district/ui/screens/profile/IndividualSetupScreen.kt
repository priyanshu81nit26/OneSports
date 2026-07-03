package app.district.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.district.data.AccountType
import app.district.data.AuthErrors
import app.district.data.DeviceProfile
import app.district.data.DeviceProfileStore
import app.district.data.DistrictRepository
import app.district.data.LockType
import app.district.data.PrefsManager
import app.district.data.ProfileRole
import app.district.ui.components.RiseAuthCard
import app.district.ui.components.RiseAuthLayout
import app.district.ui.components.RisePrimaryButton
import app.district.ui.components.riseFieldColors
import app.district.ui.theme.Rise
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun IndividualSetupScreen(
    prefs: PrefsManager,
    repo: DistrictRepository,
    profileStore: DeviceProfileStore,
    defaultName: String,
    defaultAge: Int = 0,
    defaultPhotoUri: String = "",
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val name = defaultName.ifBlank { "Me" }
    val age = defaultAge.takeIf { it > 0 } ?: 18
    var username by remember(defaultName) { mutableStateOf(setupUsernameSeed(defaultName)) }
    var photoUri by remember { mutableStateOf(defaultPhotoUri) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var reserving by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) photoUri = uri.toString()
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) photoUri = saveIndividualProfileBitmap(context, bitmap)
    }

    BackHandler(onBack = onBack)

    RiseAuthLayout(showLogo = true, logoSize = 72.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
            }
        }

        Text(
            "Set up your profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Rise.TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Choose a username, photo, and PIN",
            color = Rise.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 6.dp).fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { 1f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(99.dp)),
            color = Rise.AccentSoft,
            trackColor = Rise.CardElevated
        )
        Spacer(Modifier.height(20.dp))

        RiseAuthCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ProfilePhotoPreview(photoUri = photoUri, size = 52)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Rise.TextPrimary)
                        Text("Your Rise profile", color = Rise.TextMuted, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { raw ->
                        username = PrefsManager.normalizeUsername(raw).take(20)
                        error = ""
                    },
                    label = { Text("Choose a username") },
                    prefix = { Text("@", color = Rise.TextMuted) },
                    singleLine = true,
                    isError = username.isNotBlank() && username.length < 6,
                    supportingText = if (username.isNotBlank() && username.length < 6) {
                        { Text("At least 6 characters", color = Rise.Danger, fontSize = 11.sp) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )

                Spacer(Modifier.height(16.dp))
                Text("Profile picture", color = Rise.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ProfilePhotoPreview(photoUri = photoUri, size = 64)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text("Gallery", color = Rise.TextPrimary)
                        }
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text("Camera", color = Rise.TextPrimary)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                IndividualPinField(pin, { pin = it }, "PIN (4-6 digits)")
                Spacer(Modifier.height(10.dp))
                IndividualPinField(confirmPin, { confirmPin = it }, "Confirm PIN")

                if (error.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(error, color = Rise.Danger, fontSize = 12.sp)
                }

                Spacer(Modifier.height(20.dp))
                RisePrimaryButton(
                    text = if (reserving) "Checking availability…" else "Enter Rise",
                    enabled = !reserving && username.length >= 6 && pin.length >= 4 && pin == confirmPin,
                    onClick = {
                        scope.launch {
                            val cleanUsername = PrefsManager.normalizeUsername(username)
                            if (cleanUsername.length < 6) {
                                error = "Username must be at least 6 characters."
                                return@launch
                            }
                            if (pin.length < 4 || pin != confirmPin) {
                                error = "PINs must match and be 4-6 digits."
                                return@launch
                            }
                            reserving = true
                            val account = repo.currentAccount()
                            val claimResult = runCatching {
                                if (account != null) repo.claimUsername(account.uid, cleanUsername)
                            }
                            reserving = false
                            if (claimResult.isFailure) {
                                val ex = claimResult.exceptionOrNull()
                                val msg = ex?.message.orEmpty()
                                val isTaken = msg.contains("taken", ignoreCase = true)
                                val isPermission = msg.contains("PERMISSION_DENIED", ignoreCase = true)
                                    || msg.contains("Missing or insufficient permissions", ignoreCase = true)
                                when {
                                    isTaken -> {
                                        error = "That username is already taken — try another."
                                        return@launch
                                    }
                                    isPermission -> Unit
                                    else -> {
                                        error = AuthErrors.message(ex)
                                        return@launch
                                    }
                                }
                            }
                            prefs.setUsername(cleanUsername)
                            username = cleanUsername
                            prefs.setAccountType(AccountType.INDIVIDUAL)
                            val id = prefs.createProfile(
                                role = ProfileRole.USER,
                                name = name,
                                age = age,
                                avatar = "",
                                photoUri = photoUri,
                                username = cleanUsername,
                                blockedApps = emptySet(),
                                makeActive = true
                            )
                            prefs.setProfileLock(id, LockType.PIN, pin)
                            prefs.setSetupDone(true)
                            account?.let {
                                profileStore.upsert(
                                    DeviceProfile(
                                        uid = it.uid,
                                        username = cleanUsername,
                                        displayName = name,
                                        photoUri = photoUri
                                    )
                                )
                            }
                            onComplete()
                        }
                    }
                )
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun ProfilePhotoPreview(photoUri: String, size: Int) {
    if (photoUri.isNotBlank()) {
        AsyncImage(
            model = photoUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Rise.CardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, null, tint = Rise.TextSecondary, modifier = Modifier.size((size / 2).dp))
        }
    }
}

@Composable
private fun IndividualPinField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onChange(it) },
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
        colors = riseFieldColors()
    )
}

private fun setupUsernameSeed(name: String): String {
    val base = PrefsManager.normalizeUsername(name.replace(" ", "_"))
    return when {
        base.length >= 6 -> base.take(20)
        base.isBlank() -> "rise_user"
        else -> "${base}_user".take(20)
    }
}

private fun saveIndividualProfileBitmap(context: Context, bitmap: Bitmap): String {
    val dir = File(context.filesDir, "profile_photos").apply { mkdirs() }
    val file = File(dir, "individual_${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
}
