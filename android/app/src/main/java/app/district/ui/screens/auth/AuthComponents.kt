package app.district.ui.screens.auth

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.district.ui.components.riseFieldColors
import app.district.ui.theme.Rise
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun authFieldColors() = riseFieldColors()

internal fun googleSignInErrorMessage(e: ApiException): String = when (e.statusCode) {
    CommonStatusCodes.DEVELOPER_ERROR ->
        "Google sign-in isn't set up for this build yet. Use email or phone to continue for now."
    CommonStatusCodes.NETWORK_ERROR ->
        "No network connection. Check your internet and try again."
    GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
        "Google sign-in was cancelled."
    CommonStatusCodes.SIGN_IN_REQUIRED ->
        "Please pick a Google account to continue."
    else ->
        "Sign-in didn't work. Check your details and try again."
}

@Composable
internal fun GoogleAccountFinishPrompt() {
    Text(
        "Google connected — let's finish your profile.",
        color = Rise.TextSecondary,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "Tap Continue to choose your username and secure PIN.",
        color = Rise.TextMuted,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun SignupProfileFields(
    firstName: String,
    onFirstName: (String) -> Unit,
    lastName: String,
    onLastName: (String) -> Unit,
    age: String,
    onAge: (String) -> Unit
) {
    Text("A few details about you", fontSize = 14.sp, color = Rise.AccentSoft, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
        androidx.compose.material3.OutlinedTextField(
            value = firstName,
            onValueChange = { if (it.length <= 24) onFirstName(it) },
            label = { Text("First name") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            colors = authFieldColors()
        )
        androidx.compose.material3.OutlinedTextField(
            value = lastName,
            onValueChange = { if (it.length <= 24) onLastName(it) },
            label = { Text("Last name") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            colors = authFieldColors()
        )
    }
    Spacer(Modifier.height(10.dp))
    androidx.compose.material3.OutlinedTextField(
        value = age,
        onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) onAge(it) },
        label = { Text("Age") },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        colors = authFieldColors()
    )
}
