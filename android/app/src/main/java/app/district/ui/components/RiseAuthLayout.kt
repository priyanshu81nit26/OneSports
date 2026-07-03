package app.district.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.district.ui.theme.Rise

@Composable
fun RiseAuthLayout(
    modifier: Modifier = Modifier,
    showLogo: Boolean = true,
    logoSize: androidx.compose.ui.unit.Dp = 88.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Rise.Black)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            if (showLogo) {
                FocusLogo(Modifier.height(logoSize).fillMaxWidth(0.45f))
                Spacer(Modifier.height(28.dp))
            }
            content()
            Spacer(Modifier.height(72.dp))
        }
        SargamFooter(Modifier.align(Alignment.BottomEnd).padding(20.dp))
    }
}

@Composable
fun SargamFooter(modifier: Modifier = Modifier) {
    Text(
        "sargam.Ai",
        modifier = modifier,
        color = Rise.TextMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun RiseAuthCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Rise.Card, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}
