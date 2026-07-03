package app.district.ui.screens.communities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.district.data.CommunityType
import app.district.data.CreateCommunityRequest
import app.district.ui.components.RisePrimaryButton
import app.district.ui.components.RiseCard
import app.district.ui.components.RiseScreen
import app.district.ui.theme.Rise

@Composable
fun CreateCommunityScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateCommunityViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CommunityType.GYM) }
    var emoji by remember { mutableStateOf("🏋️") }

    LaunchedEffect(ui.createdId) { ui.createdId?.let(onCreated) }

    RiseScreen(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            Spacer(Modifier.height(36.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Rise.TextPrimary)
                }
                Text("Start a community", color = Rise.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            RiseCard(Modifier.fillMaxWidth()) {
                Text("Type", color = Rise.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CommunityType.entries.forEach { t ->
                        val selected = type == t
                        Text(
                            "${t.emoji} ${t.label}",
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) Rise.AccentSoft.copy(alpha = 0.25f) else Rise.CardElevated)
                                .clickable {
                                    type = t
                                    emoji = t.emoji
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (selected) Rise.AccentSoft else Rise.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                field("Community name", name) { if (it.length <= 32) name = it }
                Text("At least 6 characters — must be unique.", color = Rise.TextMuted, fontSize = 12.sp)
                field("Description", description, 3) { description = it }
                field("Area or building name (e.g. Green Valley Society)", location) { location = it }
                ui.error?.let { Text(it, color = Rise.Danger, fontSize = 12.sp) }
                Spacer(Modifier.height(12.dp))
                RisePrimaryButton(
                    text = if (ui.saving) "Creating…" else "Create community",
                    enabled = name.length >= 6 && !ui.saving,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.create(
                            CreateCommunityRequest(
                                name = name.trim(),
                                description = description.trim(),
                                type = type,
                                location = location.trim(),
                                emoji = emoji
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun field(label: String, value: String, minLines: Int = 1, onValue: (String) -> Unit) {
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        minLines = minLines,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Rise.AccentSoft,
            unfocusedBorderColor = Rise.Border,
            focusedTextColor = Rise.TextPrimary,
            unfocusedTextColor = Rise.TextPrimary,
            focusedLabelColor = Rise.TextSecondary,
            unfocusedLabelColor = Rise.TextMuted
        )
    )
}
