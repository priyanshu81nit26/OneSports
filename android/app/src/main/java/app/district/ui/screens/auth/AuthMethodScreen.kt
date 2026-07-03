package app.district.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.district.ui.components.RiseAnimatedButton
import app.district.ui.components.RiseAuthCard
import app.district.ui.components.RiseAuthLayout
import app.district.ui.components.RiseOutlinedButton
import app.district.ui.theme.Rise

@Composable
fun AuthMethodScreen(
    initialSignUp: Boolean,
    onPickMethod: (AuthMethod, Boolean) -> Unit,
    onBack: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(initialSignUp) }

    RiseAuthLayout(showLogo = true, logoSize = 72.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
            }
        }

        Text(
            if (isSignUp) "Create your account" else "Welcome back",
            style = MaterialTheme.typography.headlineMedium,
            color = Rise.TextPrimary
        )
        Text(
            "Choose how you'd like to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = Rise.TextSecondary,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(Modifier.height(28.dp))

        MethodRow("Continue with Google", "Quick sign-in with Google", Icons.Filled.AccountCircle) {
            onPickMethod(AuthMethod.GOOGLE, isSignUp)
        }
        Spacer(Modifier.height(12.dp))
        MethodRow("Continue with Phone", "We'll text you a one-time code", Icons.Filled.Phone) {
            onPickMethod(AuthMethod.PHONE, isSignUp)
        }
        if (!isSignUp) {
            Spacer(Modifier.height(12.dp))
            MethodRow("Username & password", "Sign in with your Rise handle", Icons.Filled.AccountCircle) {
                onPickMethod(AuthMethod.USERNAME, false)
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (isSignUp) "Already have an account?" else "New here?",
                color = Rise.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (isSignUp) "Sign in" else "Create account",
                color = Rise.AccentSoft,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isSignUp = !isSignUp }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun MethodRow(
    label: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    RiseAuthCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Rise.AccentSoft, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, color = Rise.TextMuted, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Rise.TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}
