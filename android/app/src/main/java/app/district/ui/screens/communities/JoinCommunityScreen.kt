package app.district.ui.screens.communities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.ui.components.RiseAnimatedButton
import app.district.ui.components.RiseAuthCard
import app.district.ui.components.RiseButtonStyle
import app.district.ui.components.RisePill
import app.district.ui.components.RiseScreen
import app.district.ui.components.riseFieldColors
import app.district.ui.theme.Rise

@Composable
fun JoinCommunityScreen(
    onBack: () -> Unit,
    onJoined: (String) -> Unit,
    viewModel: JoinCommunityViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    var modeCode by remember { mutableStateOf(true) }
    var joinCode by remember { mutableStateOf("") }
    var communityName by remember { mutableStateOf("") }

    LaunchedEffect(ui.joinedId) {
        ui.joinedId?.let(onJoined)
    }

    RiseScreen(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            Spacer(Modifier.height(36.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
                }
                Text("Join a community", color = Rise.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            RiseAuthCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                        RisePill("Join with code", modeCode, { modeCode = true; viewModel.clearError() })
                        RisePill("Join with name", !modeCode, { modeCode = false; viewModel.clearError() })
                    }
                    Spacer(Modifier.height(16.dp))
                    if (modeCode) {
                        Text("Enter the 6-digit code shared by the community admin.", color = Rise.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = { joinCode = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text("6-digit code") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            colors = riseFieldColors()
                        )
                    } else {
                        Text("Enter the exact community name (at least 6 characters).", color = Rise.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = communityName,
                            onValueChange = { if (it.length <= 32) communityName = it },
                            label = { Text("Community name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = riseFieldColors()
                        )
                    }
                    ui.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Rise.Danger, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(18.dp))
                    RiseAnimatedButton(
                        text = if (ui.busy) "Joining…" else "Join community",
                        enabled = !ui.busy && if (modeCode) joinCode.length == 6 else communityName.length >= 6,
                        onClick = {
                            if (modeCode) viewModel.joinByCode(joinCode) else viewModel.joinByName(communityName)
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    RiseAnimatedButton(text = "Back", onClick = onBack, style = RiseButtonStyle.Outlined, contentColor = Rise.TextSecondary)
                }
            }
        }
    }
}
