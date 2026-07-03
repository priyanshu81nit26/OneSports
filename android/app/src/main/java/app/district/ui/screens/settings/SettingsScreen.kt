package app.district.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.ui.components.RiseCard
import app.district.ui.components.RiseDivider
import app.district.ui.components.RiseGroupedCard
import app.district.ui.components.RiseListRow
import app.district.ui.components.RisePrimaryButton
import app.district.ui.components.RiseScreen
import app.district.ui.components.riseFieldColors
import app.district.ui.theme.Rise

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onProfileLogout: () -> Unit,
    onAccountLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val pinVerified by viewModel.pinVerified.collectAsState()

    RiseScreen(Modifier.fillMaxSize()) {
        if (!pinVerified) {
            PinGateScreen(onBack = onBack, viewModel = viewModel)
        } else {
            SettingsContent(
                onBack = onBack,
                onEditProfile = onEditProfile,
                onAccountLogout = onAccountLogout,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun PinGateScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    val pinError by viewModel.pinError.collectAsState()
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
            }
        }

        Icon(Icons.Filled.Lock, null, tint = Rise.AccentSoft, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(20.dp))
        Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Rise.TextPrimary)
        Text("Enter your PIN to continue", color = Rise.TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))

        RiseCard {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6) pin = it },
                label = { Text("PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                isError = pinError != null,
                supportingText = pinError?.let { { Text(it, color = Rise.Danger) } },
                colors = riseFieldColors()
            )
        }

        Spacer(Modifier.height(20.dp))
        RisePrimaryButton(
            text = "Unlock",
            enabled = pin.length >= 4,
            onClick = { viewModel.verifyPin(pin) }
        )
    }
}

@Composable
private fun SettingsContent(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onAccountLogout: () -> Unit,
    viewModel: SettingsViewModel
) {
    val deleteState by viewModel.deleteState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(48.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Rise.TextPrimary)
        }

        Spacer(Modifier.height(24.dp))

        Text("Account", color = Rise.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        RiseGroupedCard {
            RiseListRow(
                title = "Set profile",
                icon = { Icon(Icons.Filled.Person, null, tint = Rise.TextPrimary) },
                onClick = onEditProfile
            )
            RiseDivider()
            RiseListRow(
                title = "Notification settings",
                icon = { Icon(Icons.Filled.Notifications, null, tint = Rise.TextPrimary) },
                onClick = { }
            )
            RiseDivider()
            RiseListRow(
                title = "Account settings",
                icon = { Icon(Icons.Filled.Person, null, tint = Rise.TextPrimary) },
                onClick = { showDeleteDialog = false }
            )
        }

        Spacer(Modifier.height(20.dp))
        Text("Support", color = Rise.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        RiseGroupedCard {
            RiseListRow(
                title = "About Rise",
                icon = { Icon(Icons.Filled.Info, null, tint = Rise.TextPrimary) },
                onClick = { }
            )
        }

        Spacer(Modifier.height(20.dp))
        Text("More", color = Rise.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        RiseGroupedCard {
            RiseListRow(
                title = "Logout",
                icon = { Icon(Icons.Filled.Logout, null, tint = Rise.Danger) },
                onClick = { showLogoutDialog = true }
            )
        }

        if (deleteState?.deletionPending == true) {
            Spacer(Modifier.height(16.dp))
            RiseCard {
                Text("Deletion scheduled", color = Rise.Danger, fontWeight = FontWeight.SemiBold)
                Text(
                    "You can revive your account within 15 days.",
                    color = Rise.TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(12.dp))
                RisePrimaryButton(text = "Revive account", onClick = { viewModel.cancelAccountDeletion() })
            }
        } else {
            Spacer(Modifier.height(16.dp))
            RiseGroupedCard {
                RiseListRow(
                    title = "Delete account",
                    icon = { Icon(Icons.Filled.Delete, null, tint = Rise.Danger) },
                    onClick = { showDeleteDialog = true }
                )
            }
        }

        actionMessage?.let { message ->
            Spacer(Modifier.height(16.dp))
            Text(message, color = Rise.TextSecondary, fontSize = 13.sp)
        }

        Spacer(Modifier.height(32.dp))
        Text("Rise", color = Rise.TextMuted, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("v1.0.0", color = Rise.TextMuted, fontSize = 12.sp)
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out from", color = Rise.TextPrimary, fontWeight = FontWeight.Bold) },
            containerColor = Rise.Card,
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logoutAccount(onAccountLogout)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("This device", color = Rise.Danger, fontWeight = FontWeight.SemiBold) }
                    RiseDivider()
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logoutAccount(onAccountLogout)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("All devices", color = Rise.Danger, fontWeight = FontWeight.SemiBold) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Rise.TextSecondary)
                }
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = { pin, confirmPin, reason, typed ->
                viewModel.requestAccountDeletion(pin, confirmPin, reason, typed)
                showDeleteDialog = false
            }
        )
    }
}

@Composable
private fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (pin: String, confirmPin: String, reason: String, typedDelete: String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var typedDelete by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Rise.Card,
        title = { Text("Delete account", color = Rise.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Tell us why you're leaving") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
                OutlinedTextField(
                    value = typedDelete,
                    onValueChange = { typedDelete = it },
                    label = { Text("Type delete to confirm") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pin, confirmPin, reason, typedDelete) },
                enabled = pin.length >= 4 && reason.isNotBlank() && typedDelete.isNotBlank()
            ) { Text("Delete", color = Rise.Danger) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Rise.TextSecondary) } }
    )
}
