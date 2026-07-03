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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Instagram-style centered white permission card. */
@Composable
fun RiseInstaPermissionDialog(
    title: String,
    message: String,
    allowLabel: String = "Allow",
    denyLabel: String = "Not now",
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    Dialog(
        onDismissRequest = onDeny,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                color = Color(0xFF262626),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                message,
                color = Color(0xFF737373),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            RiseAnimatedButton(
                text = allowLabel,
                onClick = onAllow,
                style = RiseButtonStyle.Primary,
                contentColor = Color.Black
            )
            Spacer(Modifier.height(10.dp))
            RiseAnimatedButton(
                text = denyLabel,
                onClick = onDeny,
                style = RiseButtonStyle.Outlined,
                contentColor = Color(0xFF0095F6)
            )
        }
    }
}
