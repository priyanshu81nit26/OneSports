package app.district.ui.screens.onboarding

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.district.data.PrefsManager
import app.district.ui.components.RiseAnimatedButton
import app.district.ui.components.RiseAuthCard
import app.district.ui.components.RiseAuthLayout
import app.district.ui.components.RiseButtonStyle
import app.district.ui.components.riseFieldColors
import app.district.ui.theme.Rise
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SignupDetailsScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val ui by viewModel.ui.collectAsState()
    val context = LocalContext.current
    var firstName by remember(ui.draft.firstName) { mutableStateOf(ui.draft.firstName) }
    var lastName by remember(ui.draft.lastName) { mutableStateOf(ui.draft.lastName) }
    var dobLabel by remember(ui.draft.dateOfBirthMillis) {
        mutableStateOf(
            if (ui.draft.dateOfBirthMillis > 0L) formatDob(ui.draft.dateOfBirthMillis) else ""
        )
    }
    var dobMillis by remember(ui.draft.dateOfBirthMillis) { mutableStateOf(ui.draft.dateOfBirthMillis) }
    var error by remember { mutableStateOf("") }

    RiseAuthLayout(showLogo = true, logoSize = 72.dp) {
        SignupTopBar("About you", "Step 1 of 3", onBack)
        RiseAuthCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { if (it.length <= 24) firstName = it; error = "" },
                    label = { Text("First name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { if (it.length <= 24) lastName = it; error = "" },
                    label = { Text("Last name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = dobLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date of birth") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            if (dobMillis > 0L) cal.timeInMillis = dobMillis
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val picked = Calendar.getInstance().apply {
                                        set(y, m, d, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    dobMillis = picked.timeInMillis
                                    dobLabel = formatDob(picked.timeInMillis)
                                    error = ""
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
                        }) {
                            Icon(Icons.Filled.CalendarMonth, null, tint = Rise.AccentSoft)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
                if (error.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = Rise.Danger, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                }
                Spacer(Modifier.height(20.dp))
                RiseAnimatedButton(
                    text = "Continue",
                    enabled = firstName.isNotBlank() && lastName.isNotBlank() && dobMillis > 0L,
                    onClick = {
                        if (firstName.isBlank() || lastName.isBlank() || dobMillis <= 0L) {
                            error = "Fill in all fields"
                        } else {
                            viewModel.saveDetails(firstName, lastName, dobMillis)
                            onNext()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SignupUsernameScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val ui by viewModel.ui.collectAsState()
    var username by remember(ui.draft.username) {
        mutableStateOf(ui.draft.username.ifBlank { "rise_user" })
    }

    RiseAuthLayout(showLogo = false) {
        SignupTopBar("Choose username", "Step 2 of 3", onBack)
        RiseAuthCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Pick a unique handle — at least 6 characters.",
                    color = Rise.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = PrefsManager.normalizeUsername(it).take(20)
                        viewModel.checkUsername(username)
                    },
                    label = { Text("Username") },
                    prefix = { Text("@", color = Rise.TextMuted) },
                    singleLine = true,
                    trailingIcon = {
                        when {
                            ui.usernameChecking -> CircularProgressIndicator(Modifier.size(18.dp), color = Rise.AccentSoft)
                            ui.usernameAvailable == true -> Icon(Icons.Filled.Check, null, tint = Rise.Success)
                            ui.usernameAvailable == false -> Icon(Icons.Filled.Close, null, tint = Rise.Danger)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
                ui.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Rise.Danger, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(20.dp))
                RiseAnimatedButton(
                    text = "Continue",
                    enabled = username.length >= 6 && ui.usernameAvailable == true && !ui.usernameChecking,
                    onClick = {
                        if (viewModel.confirmUsername(username)) onNext()
                    }
                )
            }
        }
    }
}

@Composable
fun SignupPasswordScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val ui by viewModel.ui.collectAsState()
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(ui.completed) {
        if (ui.completed) onComplete()
    }

    RiseAuthLayout(showLogo = false) {
        SignupTopBar("Secure your account", "Step 3 of 3", onBack)
        RiseAuthCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Create a password for @${ui.draft.username}",
                    color = Rise.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.clearError() },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it; viewModel.clearError() },
                    label = { Text("Confirm password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirm.isNotBlank() && confirm != password,
                    modifier = Modifier.fillMaxWidth(),
                    colors = riseFieldColors()
                )
                ui.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Rise.Danger, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(20.dp))
                RiseAnimatedButton(
                    text = if (ui.isLoading) "Creating account…" else "Create account",
                    enabled = !ui.isLoading && ui.draft.username.length >= 6 && password.length >= 6 && password == confirm,
                    onClick = { viewModel.completeSignup(password, confirm) }
                )
                Spacer(Modifier.height(10.dp))
                RiseAnimatedButton(
                    text = "Back",
                    onClick = onBack,
                    style = RiseButtonStyle.Outlined,
                    contentColor = Rise.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SignupTopBar(title: String, step: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
        }
    }
    Text(title, style = MaterialTheme.typography.headlineMedium, color = Rise.TextPrimary, modifier = Modifier.fillMaxWidth())
    Text(step, color = Rise.TextMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
    Spacer(Modifier.height(20.dp))
}

private fun formatDob(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(millis)
