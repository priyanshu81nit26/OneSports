package app.district.ui.screens.connections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.ui.components.RiseCard
import app.district.ui.components.RiseOutlinedButton
import app.district.ui.components.RisePrimaryButton
import app.district.ui.components.RiseScreen
import app.district.ui.theme.Rise

@Composable
fun ConnectionsScreen(
    onBack: () -> Unit,
    viewModel: ConnectionsViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()

    RiseScreen(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(Modifier.height(36.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
                }
                Text("Connections", color = Rise.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))

            when {
                ui.loading -> CircularProgressIndicator(color = Rise.AccentSoft)
                ui.error != null -> {
                    Text(ui.error ?: "", color = Rise.Danger, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    RisePrimaryButton(text = "Try again", onClick = { viewModel.refresh() })
                }
                else -> {
                    if (ui.pending.isNotEmpty()) {
                        Text("Requests", color = Rise.Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        ui.pending.forEach { req ->
                            RiseCard(Modifier.padding(bottom = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(req.avatar, fontSize = 28.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(req.name, color = Rise.TextPrimary, fontWeight = FontWeight.Medium)
                                        Text("@${req.username}", color = Rise.TextMuted, fontSize = 11.sp)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    RisePrimaryButton(
                                        text = "Accept",
                                        enabled = !ui.busy,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.respond(req.requestId, true) }
                                    )
                                    RiseOutlinedButton(
                                        text = "Decline",
                                        enabled = !ui.busy,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.respond(req.requestId, false) }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    Text("Your circle", color = Rise.TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    if (ui.connections.isEmpty()) {
                        RiseCard {
                            Text("No connections yet", color = Rise.TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Send requests from community member lists to grow your local circle.",
                                color = Rise.TextMuted,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        ui.connections.forEach { conn ->
                            RiseCard(Modifier.padding(bottom = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(conn.avatar, fontSize = 28.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(conn.name, color = Rise.TextPrimary, fontWeight = FontWeight.Medium)
                                        Text("@${conn.username}", color = Rise.TextMuted, fontSize = 11.sp)
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
