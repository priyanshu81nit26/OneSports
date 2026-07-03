package app.district.ui.screens.communities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.data.ConnectionStatus
import app.district.ui.components.RisePrimaryButton
import app.district.ui.components.RiseCard
import app.district.ui.components.RiseScreen
import app.district.ui.theme.Rise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityDetailScreen(
    communityId: String,
    onBack: () -> Unit,
    viewModel: CommunityDetailViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    var updateText by remember { mutableStateOf("") }
    LaunchedEffect(communityId) { viewModel.load(communityId) }

    RiseScreen(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            Spacer(Modifier.height(36.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
                }
                Text("Community", color = Rise.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))

            when {
                ui.loading -> CircularProgressIndicator(color = Rise.AccentSoft)
                ui.community == null -> Text(ui.error ?: "We couldn't find this community", color = Rise.Danger)
                else -> {
                    val c = ui.community!!
                    RiseCard(Modifier.fillMaxWidth()) {
                        Text(c.emoji, fontSize = 40.sp)
                        Text(c.name, color = Rise.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("${c.type.emoji} ${c.type.label} · ${c.location}", color = Rise.TextSecondary, fontSize = 13.sp)
                        Text("${c.memberCount} members", color = Rise.AccentSoft, fontSize = 12.sp)
                        if (c.joinCode.isNotBlank() && c.isMember) {
                            Spacer(Modifier.height(6.dp))
                            Text("Join code (share with others)", color = Rise.TextSecondary, fontSize = 12.sp)
                            Text(c.joinCode, color = Rise.AccentSoft, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(c.description, color = Rise.TextMuted, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!c.isMember) {
                        RisePrimaryButton(
                            text = if (ui.busy) "Joining…" else "Join this community",
                            enabled = !ui.busy,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.join(communityId) }
                        )
                    } else {
                        Text("Updates", color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        ui.updates.forEach { update ->
                            RiseCard(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Text(update.authorName, color = Rise.AccentSoft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(update.message, color = Rise.TextPrimary, fontSize = 14.sp)
                                Text(formatTime(update.timestamp), color = Rise.TextMuted, fontSize = 10.sp)
                            }
                        }
                        OutlinedTextField(
                            value = updateText,
                            onValueChange = { updateText = it },
                            label = { Text("Share an update with members") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Rise.AccentSoft,
                                unfocusedBorderColor = Rise.Border,
                                focusedTextColor = Rise.TextPrimary,
                                unfocusedTextColor = Rise.TextPrimary
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        RisePrimaryButton(
                            text = "Post update",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                            viewModel.postUpdate(communityId, updateText)
                            updateText = ""
                        })

                        Spacer(Modifier.height(16.dp))
                        Text("Members", color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        ui.members.forEach { member ->
                            RiseCard(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(member.avatar, fontSize = 28.sp)
                                    Spacer(Modifier.padding(horizontal = 8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(member.name, color = Rise.TextPrimary, fontWeight = FontWeight.Medium)
                                        Text("@${member.username}", color = Rise.TextMuted, fontSize = 11.sp)
                                    }
                                    when (member.connectionStatus) {
                                        ConnectionStatus.CONNECTED -> Text("Connected", color = Rise.Success, fontSize = 11.sp)
                                        ConnectionStatus.PENDING -> Text("Request sent", color = Rise.Accent, fontSize = 11.sp)
                                        ConnectionStatus.NONE -> TextButton(onClick = { viewModel.connect(member) }) {
                                            Text("Send request", color = Rise.AccentSoft, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return ""
    return SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(ms))
}
