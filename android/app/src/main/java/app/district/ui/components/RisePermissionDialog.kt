package app.district.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.district.ui.theme.Rise

@Composable
fun RisePermissionDialog(
    title: String,
    message: String,
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    Dialog(onDismissRequest = onDeny) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Rise.Card, RoundedCornerShape(20.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Rise.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Rise.TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            RiseAnimatedButton(text = "Allow", onClick = onAllow)
            Spacer(Modifier.height(10.dp))
            RiseAnimatedButton(
                text = "Not now",
                onClick = onDeny,
                style = RiseButtonStyle.Outlined,
                contentColor = Rise.TextMuted
            )
        }
    }
}
