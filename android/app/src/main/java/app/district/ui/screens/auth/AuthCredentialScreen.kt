package app.district.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.R
import app.district.data.PrefsManager
import app.district.ui.components.RiseAnimatedButton
import app.district.ui.components.RiseAuthCard
import app.district.ui.components.RiseAuthLayout
import app.district.ui.components.riseFieldColors
import app.district.ui.theme.Rise
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

enum class AuthMethod { GOOGLE, PHONE, USERNAME }

@Composable
fun AuthCredentialScreen(
    method: AuthMethod,
    initialSignUp: Boolean,
    onAuthSuccess: (needsSetup: Boolean) -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val isSignUp = initialSignUp

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var phoneCountryCode by remember { mutableStateOf("+91") }
    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var autoVerifiedCredential by remember { mutableStateOf<PhoneAuthCredential?>(null) }
    var phoneMessage by remember { mutableStateOf("") }
    var googleFlowPending by remember { mutableStateOf(false) }

    val googleSignInClient = remember(context) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            val token = account.idToken
            if (token.isNullOrBlank()) {
                googleFlowPending = false
                viewModel.showError("Google sign-in did not return an ID token")
            } else {
                viewModel.loginWithGoogle(token)
            }
        } catch (e: ApiException) {
            googleFlowPending = false
            viewModel.showError(googleSignInErrorMessage(e))
        }
    }

    fun launchGoogle() {
        googleFlowPending = true
        viewModel.clearError()
        googleSignInClient.signOut().addOnCompleteListener {
            googleLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    LaunchedEffect(method) {
        if (method == AuthMethod.GOOGLE) launchGoogle()
    }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onAuthSuccess(uiState.needsSetup == true)
            viewModel.clearSuccess()
        }
    }

    fun normalizedPhoneNumber(): String {
        val countryDigits = phoneCountryCode.filter { it.isDigit() }.take(4)
        val localDigits = phoneNumber.filter { it.isDigit() }.take(15)
        return if (countryDigits.isBlank() || localDigits.isBlank()) "" else "+$countryDigits$localDigits"
    }

    fun isPhoneReady(): Boolean {
        val countryDigits = phoneCountryCode.filter { it.isDigit() }
        val localDigits = phoneNumber.filter { it.isDigit() }
        return countryDigits.length in 1..4 && localDigits.length in 4..15
    }

    fun startPhoneVerification() {
        val hostActivity = activity ?: run {
            viewModel.showError("Phone sign-in needs an activity context")
            return
        }
        if (!isPhoneReady()) {
            viewModel.showError("Enter country code and phone number")
            return
        }
        autoVerifiedCredential = null
        smsCode = ""
        phoneMessage = "Sending code…"
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                autoVerifiedCredential = credential
                phoneMessage = "Verified. Tap Continue."
            }
            override fun onVerificationFailed(e: FirebaseException) {
                phoneMessage = ""
                viewModel.showError("Could not send code. Try again.")
            }
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                phoneMessage = "Code sent — check your messages"
            }
        }
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(normalizedPhoneNumber())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(hostActivity)
                .setCallbacks(callbacks)
                .build()
        )
    }

    BackHandler(onBack = onBack)

    val title = when (method) {
        AuthMethod.GOOGLE -> if (isSignUp) "Sign up with Google" else "Sign in with Google"
        AuthMethod.PHONE -> if (isSignUp) "Sign up with phone" else "Sign in with phone"
        AuthMethod.USERNAME -> "Sign in with username"
    }

    RiseAuthLayout(showLogo = method != AuthMethod.GOOGLE, logoSize = 72.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
            }
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, color = Rise.TextPrimary)
        Spacer(Modifier.height(20.dp))

        RiseAuthCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                when (method) {
                    AuthMethod.GOOGLE -> {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            if (uiState.isLoading || googleFlowPending) {
                                CircularProgressIndicator(color = Rise.AccentSoft)
                            } else {
                                Text(
                                    "Pick a Google account to continue.",
                                    color = Rise.TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    AuthMethod.PHONE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = phoneCountryCode,
                                onValueChange = {
                                    val digits = it.filter { c -> c.isDigit() }.take(4)
                                    phoneCountryCode = if (digits.isBlank()) "+" else "+$digits"
                                },
                                label = { Text("Code") },
                                singleLine = true,
                                modifier = Modifier.width(92.dp),
                                colors = authFieldColors()
                            )
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it.filter { c -> c.isDigit() }.take(15) },
                                label = { Text("Phone number") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f),
                                colors = authFieldColors()
                            )
                        }
                        if (phoneMessage.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(phoneMessage, color = Rise.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        if (verificationId.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = smsCode,
                                onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) smsCode = it },
                                label = { Text("OTP code") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = authFieldColors()
                            )
                        }
                    }
                    AuthMethod.USERNAME -> {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = PrefsManager.normalizeUsername(it).take(20) },
                            label = { Text("Username") },
                            prefix = { Text("@", color = Rise.TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = authFieldColors()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        null,
                                        tint = Rise.TextMuted
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = authFieldColors()
                        )
                    }
                }

                uiState.error?.let { error ->
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = Rise.Danger, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(20.dp))

                val primaryEnabled = !uiState.isLoading && when (method) {
                    AuthMethod.GOOGLE -> true
                    AuthMethod.PHONE -> isPhoneReady() && (
                        (verificationId.isBlank() && autoVerifiedCredential == null) ||
                            autoVerifiedCredential != null ||
                            smsCode.length >= 4
                        )
                    AuthMethod.USERNAME -> username.length >= 6 && password.length >= 6
                }

                RiseAnimatedButton(
                    text = when (method) {
                        AuthMethod.GOOGLE -> "Pick Google account"
                        AuthMethod.PHONE -> when {
                            autoVerifiedCredential != null || verificationId.isNotBlank() -> "Continue"
                            else -> "Send code"
                        }
                        AuthMethod.USERNAME -> "Sign in"
                    },
                    enabled = primaryEnabled,
                    onClick = {
                        when (method) {
                            AuthMethod.GOOGLE -> launchGoogle()
                            AuthMethod.PHONE -> {
                                val credential = autoVerifiedCredential
                                when {
                                    credential != null -> viewModel.loginWithPhoneCredential(credential)
                                    verificationId.isBlank() -> startPhoneVerification()
                                    else -> viewModel.loginWithPhoneCredential(
                                        PhoneAuthProvider.getCredential(verificationId, smsCode)
                                    )
                                }
                            }
                            AuthMethod.USERNAME -> viewModel.loginWithUsername(username, password)
                        }
                    }
                )
            }
        }
    }
}
