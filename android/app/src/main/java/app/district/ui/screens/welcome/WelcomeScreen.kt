package app.district.ui.screens.welcome

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.district.data.DeviceProfile
import app.district.ui.components.FocusLogo
import app.district.ui.components.RiseAnimatedButton
import app.district.ui.components.RiseAuthCard
import app.district.ui.components.RiseAuthLayout
import app.district.ui.components.RiseButtonStyle
import app.district.ui.theme.Rise
import coil.compose.AsyncImage

private const val STEP_HERO = 0
private const val STEP_DISCOVER = 1
private const val STEP_HOST = 2
private const val STEP_COMMUNITY = 3
private const val STEP_CHOICE = 4

private data class IntroSlide(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val accent: androidx.compose.ui.graphics.Color
)

private val introSlides = listOf(
    IntroSlide(
        icon = Icons.Filled.Stadium,
        title = "Discover sport near you",
        body = "Find football, cricket, basketball, and more happening in your city — all in one place.",
        accent = Rise.AccentSoft
    ),
    IntroSlide(
        icon = Icons.Filled.SportsScore,
        title = "Host or join events",
        body = "Organise pickup games, open registrations, and book your spot in seconds.",
        accent = Rise.Success
    ),
    IntroSlide(
        icon = Icons.Filled.Groups,
        title = "Rise with your community",
        body = "Join groups, save events, and connect with players who share your passion.",
        accent = Rise.Accent
    )
)

@Composable
fun WelcomeScreen(
    profiles: List<DeviceProfile>,
    hasSeenIntro: Boolean,
    isLoggedIn: Boolean,
    isSetupDone: Boolean,
    onIntroComplete: () -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onContinueSetup: () -> Unit,
    onResumeSession: () -> Unit
) {
    var step by remember(hasSeenIntro) {
        mutableIntStateOf(if (hasSeenIntro) STEP_CHOICE else STEP_HERO)
    }

    BackHandler {
        when {
            step in STEP_DISCOVER..STEP_COMMUNITY -> step -= 1
            step == STEP_CHOICE && !hasSeenIntro -> step = STEP_COMMUNITY
            else -> Unit
        }
    }

    fun advance() {
        when (step) {
            STEP_HERO -> step = STEP_DISCOVER
            STEP_DISCOVER, STEP_HOST -> step += 1
            STEP_COMMUNITY -> {
                step = STEP_CHOICE
                onIntroComplete()
            }
        }
    }

    RiseAuthLayout(
        showLogo = step in STEP_DISCOVER..STEP_COMMUNITY,
        logoSize = 72.dp
    ) {
        when (step) {
            STEP_HERO -> HeroStep(onContinue = { advance() })
            in STEP_DISCOVER..STEP_COMMUNITY -> {
                val slide = introSlides[step - 1]
                IntroSlidePage(
                    slide = slide,
                    stepIndex = step - 1,
                    totalSlides = introSlides.size,
                    onNext = { advance() }
                )
            }
            STEP_CHOICE -> AccountChoiceStep(
                profiles = profiles,
                isLoggedIn = isLoggedIn,
                isSetupDone = isSetupDone,
                onSignIn = onSignIn,
                onSignUp = onSignUp,
                onContinueSetup = onContinueSetup,
                onResumeSession = onResumeSession
            )
        }
    }
}

@Composable
private fun HeroStep(onContinue: () -> Unit) {
    FocusLogo(Modifier.height(120.dp).fillMaxWidth(0.42f))
    Spacer(Modifier.height(28.dp))
    Text(
        "Welcome to Rise",
        style = MaterialTheme.typography.displayMedium,
        color = Rise.TextPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    Text(
        "Your city for sport & events — organise, join communities, and rise together.",
        style = MaterialTheme.typography.bodyLarge,
        color = Rise.TextSecondary,
        textAlign = TextAlign.Center,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    )
    Spacer(Modifier.height(36.dp))
    RiseAnimatedButton(text = "Get started", onClick = onContinue)
}

@Composable
private fun IntroSlidePage(
    slide: IntroSlide,
    stepIndex: Int,
    totalSlides: Int,
    onNext: () -> Unit
) {
    Box(
        Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(slide.accent.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(slide.icon, null, tint = slide.accent, modifier = Modifier.size(34.dp))
    }
    Spacer(Modifier.height(24.dp))
    Text(
        slide.title,
        style = MaterialTheme.typography.headlineMedium,
        color = Rise.TextPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    Text(
        slide.body,
        style = MaterialTheme.typography.bodyLarge,
        color = Rise.TextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
    )
    Spacer(Modifier.height(28.dp))
    PageDots(total = totalSlides, selected = stepIndex)
    Spacer(Modifier.height(28.dp))
    RiseAnimatedButton(
        text = if (stepIndex == totalSlides - 1) "Let's go" else "Next",
        onClick = onNext
    )
}

@Composable
private fun PageDots(total: Int, selected: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { index ->
            Box(
                Modifier
                    .size(if (index == selected) 10.dp else 7.dp)
                    .clip(CircleShape)
                    .background(if (index == selected) Rise.AccentSoft else Rise.Border)
            )
        }
    }
}

@Composable
private fun AccountChoiceStep(
    profiles: List<DeviceProfile>,
    isLoggedIn: Boolean,
    isSetupDone: Boolean,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onContinueSetup: () -> Unit,
    onResumeSession: () -> Unit
) {
    Text(
        "Ready to rise?",
        style = MaterialTheme.typography.headlineMedium,
        color = Rise.TextPrimary,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        "Sign in or create a new account to continue.",
        style = MaterialTheme.typography.bodyMedium,
        color = Rise.TextSecondary,
        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
    )

    if (profiles.isNotEmpty()) {
        profiles.forEach { profile ->
            SavedProfileRow(profile) {
                when {
                    isLoggedIn && isSetupDone -> onResumeSession()
                    isLoggedIn && !isSetupDone -> onContinueSetup()
                    else -> onSignIn()
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (isLoggedIn && !isSetupDone) {
        RiseAnimatedButton(
            text = "Continue account setup",
            onClick = onContinueSetup,
            style = RiseButtonStyle.Primary
        )
        Spacer(Modifier.height(12.dp))
    }

    RiseAnimatedButton(
        text = "Already signed in",
        onClick = onSignIn,
        style = RiseButtonStyle.Secondary
    )
    Spacer(Modifier.height(12.dp))
    RiseAnimatedButton(
        text = "Create new account",
        onClick = onSignUp,
        style = RiseButtonStyle.Outlined,
        contentColor = Rise.AccentSoft
    )
}

@Composable
private fun SavedProfileRow(profile: DeviceProfile, onClick: () -> Unit) {
    RiseAuthCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (profile.photoUri.isNotBlank()) {
                AsyncImage(
                    model = profile.photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                )
            } else {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Rise.CardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = Rise.TextSecondary)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    profile.displayName.ifBlank { profile.username },
                    color = Rise.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (profile.username.isNotBlank()) {
                    Text("@${profile.username}", color = Rise.TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Rise.TextMuted)
        }
    }
}
