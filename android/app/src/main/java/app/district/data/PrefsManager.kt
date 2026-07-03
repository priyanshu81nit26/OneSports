package app.district.data

import android.content.Context
import android.os.SystemClock
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "District_prefs")

data class AppUsageSummary(
    val packageName: String,
    val secondsUsed: Long,
    val blockedAttempts: Int
)

data class ProfileDetailsSaveResult(
    val saved: Boolean,
    val message: String = ""
)

@Singleton
class PrefsManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val KEY_PARENT_PIN_HASH = stringPreferencesKey("parent_pin_hash")
        val KEY_IS_SETUP_DONE = booleanPreferencesKey("is_setup_done")
        val KEY_INTRO_SEEN = booleanPreferencesKey("intro_seen")
        val KEY_BLOCKED_APPS = stringSetPreferencesKey("blocked_apps")
        val KEY_LOCKOUT_UNTIL = longPreferencesKey("lockout_until")
        val KEY_LOCKOUT_BOOT_REF = longPreferencesKey("lockout_boot_ref")
        val KEY_CHILD_ID = stringPreferencesKey("child_id")
        val KEY_SESSION_PASS_UNTIL = longPreferencesKey("session_pass_until")
        val KEY_SESSION_PASS_BOOT_REF = longPreferencesKey("session_pass_boot_ref")
        val KEY_SESSION_PASS_APP = stringPreferencesKey("session_pass_app")
        val KEY_PROGRESSIVE_STAGE = intPreferencesKey("progressive_stage")
        val KEY_CHALLENGE_IN_PROGRESS = booleanPreferencesKey("challenge_in_progress")
        val KEY_CHALLENGE_APP = stringPreferencesKey("challenge_app")
        val KEY_CHALLENGE_STARTED_AT = longPreferencesKey("challenge_started_at")
        val KEY_CHALLENGE_COUNT = stringPreferencesKey("challenge_count")
        val KEY_DIFFICULTY = stringPreferencesKey("difficulty")
        val KEY_PIN_ATTEMPTS = intPreferencesKey("pin_attempts")
        val KEY_PIN_LOCKED_UNTIL = longPreferencesKey("pin_locked_until")
        val KEY_MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
        val KEY_SESSION_PASS_DURATION_MIN = intPreferencesKey("session_pass_duration_min")
        val KEY_PROGRESSIVE_SESSION_PASS_MIN = intPreferencesKey("progressive_session_pass_min")
        val KEY_LOCKOUT_DURATION_MIN = intPreferencesKey("lockout_duration_min")
        private const val MAX_PIN_ATTEMPTS = 5
        private const val PIN_LOCKOUT_MS = 5 * 60 * 1000L
        const val ALL_BLOCKED_APPS_PASS = "*"
        const val DEFAULT_SESSION_PASS_MIN = 3
        const val MAX_PROGRESSIVE_SESSION_PASS_MIN = 15
        const val DEMO_LOCKOUT_MIN = 15
        const val DEFAULT_LOCKOUT_MIN = DEMO_LOCKOUT_MIN
        const val MAX_PROGRESSIVE_STAGE = 3
        const val BLOCKLIST_FREEZE_DAYS = 21L
        const val BLOCKLIST_FREEZE_MS = BLOCKLIST_FREEZE_DAYS * 24L * 60L * 60L * 1000L
        const val PUZZLE_REPEAT_COOLDOWN_MS = 7L * 24L * 60L * 60L * 1000L
        const val SETTINGS_FREEZE_MS = 5L * 24L * 60L * 60L * 1000L
        const val USERNAME_CHANGE_COOLDOWN_MS = 15L * 24L * 60L * 60L * 1000L
        private const val PIN_HASH_VERSION = "pbkdf2-sha256"
        private const val PIN_HASH_ITERATIONS = 120_000
        private const val PIN_HASH_BYTES = 32

        val KEY_DAILY_BUDGET_MIN = intPreferencesKey("daily_budget_min")
        val KEY_DAILY_BUDGET_ENABLED = booleanPreferencesKey("daily_budget_enabled")
        val KEY_DAILY_USAGE_DATE = stringPreferencesKey("daily_usage_date")
        val KEY_DAILY_USAGE_SECONDS = longPreferencesKey("daily_usage_seconds")
        val KEY_CURRENT_SESSION_START = longPreferencesKey("current_session_start")
        val KEY_CURRENT_SESSION_APP = stringPreferencesKey("current_session_app")
        val KEY_NUDGE_THRESHOLD_MIN = intPreferencesKey("nudge_threshold_min")
        val KEY_NUDGE_ENABLED = booleanPreferencesKey("nudge_enabled")
        val KEY_LAST_NUDGE_TIME = longPreferencesKey("last_nudge_time")
        val KEY_REWARD_POINTS = intPreferencesKey("reward_points")
        val KEY_TOTAL_POINTS_EARNED = intPreferencesKey("total_points_earned")
        val KEY_VOLUNTARY_BACKS = intPreferencesKey("voluntary_backs")
        val KEY_NO_HINT_SOLVES = intPreferencesKey("no_hint_solves")
        val KEY_PRACTICE_SOLVED = intPreferencesKey("practice_solved")
        val KEY_PRACTICE_ATTEMPTED = intPreferencesKey("practice_attempted")
        val KEY_WEEKLY_STREAK_BONUS_DATE = stringPreferencesKey("weekly_streak_bonus_date")
        val KEY_ACHIEVEMENTS = stringSetPreferencesKey("achievements")
        val KEY_BUDGET_WARNING_SHOWN = booleanPreferencesKey("budget_warning_shown")
        val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
        val KEY_CHILD_XP = intPreferencesKey("child_xp")
        val KEY_CHILD_LEVEL = intPreferencesKey("child_level")

        // XP wallet (spendable on swags), lifetime gross XP (leaderboard), and weekly XP (championship)
        val KEY_XP_WALLET = intPreferencesKey("xp_wallet")
        val KEY_LIFETIME_XP = intPreferencesKey("lifetime_xp")
        val KEY_WEEKLY_XP = intPreferencesKey("weekly_xp")
        val KEY_WEEKLY_XP_WEEK = stringPreferencesKey("weekly_xp_week")
        val KEY_SWAG_REDEMPTIONS_JSON = stringPreferencesKey("swag_redemptions_json")
        val KEY_CHILD_AVATAR = stringPreferencesKey("child_avatar")
        val KEY_CHILD_PHOTO_URI = stringPreferencesKey("child_photo_uri")
        val KEY_CHILD_TITLE = stringPreferencesKey("child_title")
        val KEY_CHILD_ONBOARDED = booleanPreferencesKey("child_onboarded")
        val KEY_STREAK_DAYS = intPreferencesKey("streak_days")
        val KEY_LAST_CHALLENGE_DATE = stringPreferencesKey("last_challenge_date")
        val KEY_LONGEST_STREAK = intPreferencesKey("longest_streak")
        val KEY_TOTAL_CHALLENGES_SOLVED = intPreferencesKey("total_challenges_solved")
        val KEY_TOTAL_CORRECT = intPreferencesKey("total_correct")
        val KEY_SPEED_BONUS_COUNT = intPreferencesKey("speed_bonus_count")
        val KEY_PERFECT_SESSION_COUNT = intPreferencesKey("perfect_session_count")
        val KEY_COMBO_BEST = intPreferencesKey("combo_best")
        val KEY_SCREEN_TIME_SAVED_SEC = longPreferencesKey("screen_time_saved_sec")
        val KEY_TOTAL_BLOCKED_ATTEMPTS = intPreferencesKey("total_blocked_attempts")
        val KEY_CHILD_NAME = stringPreferencesKey("child_name")
        val KEY_CHILDREN_JSON = stringPreferencesKey("children_json")
        val KEY_ACTIVE_PROFILE_ROLE = stringPreferencesKey("active_profile_role")
        val KEY_ACTIVE_PROFILE_AGE = intPreferencesKey("active_profile_age")
        val KEY_ACCOUNT_TYPE = stringPreferencesKey("account_type")
        val KEY_DEVICE_ID = stringPreferencesKey("device_id")
        val KEY_AUTH_USER_ID = stringPreferencesKey("auth_user_id")
        val KEY_AUTH_EMAIL = stringPreferencesKey("auth_email")
        val KEY_AUTH_PHONE = stringPreferencesKey("auth_phone")
        val KEY_ACCOUNT_SNAPSHOTS_JSON = stringPreferencesKey("account_snapshots_json")
        val KEY_REQUIRE_APP_OPEN_LOCK = booleanPreferencesKey("require_app_open_lock")
        val KEY_ACCOUNT_FIRST_NAME = stringPreferencesKey("account_first_name")
        val KEY_ACCOUNT_LAST_NAME = stringPreferencesKey("account_last_name")
        val KEY_ACCOUNT_AGE = intPreferencesKey("account_age")
        val KEY_ACCOUNT_PHOTO_URI = stringPreferencesKey("account_photo_uri")
        val KEY_ACCOUNT_AVATAR = stringPreferencesKey("account_avatar")
        val KEY_ACCOUNT_FAMILY_ROLE = stringPreferencesKey("account_family_role")
        val KEY_DASHBOARD_LOCKOUT_NOTICE_APP = stringPreferencesKey("dashboard_lockout_notice_app")
        val KEY_DASHBOARD_LOCKOUT_NOTICE_MINUTES = intPreferencesKey("dashboard_lockout_notice_minutes")
        val KEY_SETTINGS_FROZEN_UNTIL = longPreferencesKey("settings_frozen_until")
        val KEY_MONK_MODE_UNTIL = longPreferencesKey("monk_mode_until")
        val KEY_MONK_MODE_SONGS = stringPreferencesKey("monk_mode_songs")

        // FocusIQ adaptive model, stored per-profile in one JSON map keyed by child id.
        val KEY_FOCUSIQ_BY_PROFILE = stringPreferencesKey("focusiq_by_profile")

        // Daily Problems progress + streak, stored per-profile in one JSON map keyed by child id.
        val KEY_DAILY_BY_PROFILE = stringPreferencesKey("daily_by_profile")

        // Social username (unique, set during onboarding)
        val KEY_USERNAME = stringPreferencesKey("social_username")

        // Simit (Chrome extension) local cache
        val KEY_SIMIT_RULES_JSON = stringPreferencesKey("simit_rules_json")
        val KEY_SIMIT_FOCUS_JSON = stringPreferencesKey("simit_focus_json")
        val KEY_SIMIT_PAIR_CODE = stringPreferencesKey("simit_pair_code")

        // REST (full build) auth tokens. Unused by the Firebase build.
        val KEY_API_ACCESS_TOKEN = stringPreferencesKey("api_access_token")
        val KEY_API_REFRESH_TOKEN = stringPreferencesKey("api_refresh_token")
        val KEY_API_USER_ID = stringPreferencesKey("api_user_id")

        const val DEFAULT_DAILY_BUDGET_MIN = 60
        const val DEFAULT_NUDGE_THRESHOLD_MIN = 20
        const val POINTS_VOLUNTARY_BACK = 50
        const val POINTS_NO_HINT_SOLVE = 30
        const val POINTS_WEEKLY_STREAK = 100
        const val POINTS_PER_BONUS_MINUTE = 20
        const val XP_PER_CORRECT = 10
        const val XP_SPEED_BONUS = 5
        const val XP_NO_HINT_BONUS = 15
        const val XP_PERFECT_SESSION = 50
        const val XP_STREAK_DAILY = 20

        fun xpForLevel(level: Int): Int = 100 + (level - 1) * 50

        fun levelTitle(level: Int): String = when {
            level >= 101 -> "Raja"
            level >= 91 -> "Olymp III"
            level >= 76 -> "Olymp II"
            level >= 51 -> "Olymp I"
            level >= 41 -> "Master III"
            level >= 26 -> "Master II"
            level >= 16 -> "Master I"
            level >= 11 -> "Expert"
            level >= 6 -> "Specialist"
            level >= 3 -> "Practitioner"
            else -> "Beginner"
        }

        fun normalizedProgressiveStage(stage: Int): Int = stage.coerceIn(0, MAX_PROGRESSIVE_STAGE)

        fun normalizedChallengeTier(stage: Int): Int = stage.coerceIn(0, PuzzleRules.MAX_TIER)

        fun accessMinutesForStage(stage: Int): Int = when (normalizedProgressiveStage(stage)) {
            0 -> PuzzleRules.DEFAULT_TIER1_MIN
            1 -> PuzzleRules.DEFAULT_TIER2_MIN
            else -> PuzzleRules.DEFAULT_TIER3_MIN
        }

        fun nextProgressiveStage(stage: Int): Int =
            (normalizedProgressiveStage(stage) + 1).coerceAtMost(MAX_PROGRESSIVE_STAGE)

        fun rewardAccessMinutesAfterChallenge(stage: Int): Int =
            accessMinutesForStage(normalizedChallengeTier(stage))

        fun lockoutMinutesForStage(stage: Int): Int = when (normalizedChallengeTier(stage)) {
            0 -> PuzzleRules.DEFAULT_TIER1_LOCKOUT_MIN
            1 -> PuzzleRules.DEFAULT_TIER2_LOCKOUT_MIN
            else -> PuzzleRules.DEFAULT_TIER3_LOCKOUT_MIN
        }

        fun challengeDifficultyForStage(stage: Int): Int = when (normalizedChallengeTier(stage)) {
            0 -> 3
            1 -> 5
            else -> 8
        }

        fun challengeNumberForStage(stage: Int): Int =
            normalizedChallengeTier(stage) + 1

        fun normalizeUsername(value: String): String =
            value.trim()
                .lowercase()
                .filter { it.isLetterOrDigit() || it == '_' }
                .take(20)

        fun normalizeFamilyRole(value: String): String =
            if (value.trim().equals("mother", ignoreCase = true)) "mother" else "father"

        fun nextProgressivePassMin(current: Int): Int = when {
            current < 5 -> 5
            current < 10 -> 10
            else -> 10
        }

        fun challengeDifficultyForPass(minutes: Int): Int = when {
            minutes <= 3 -> 3
            minutes <= 5 -> 5
            else -> 8
        }

        val AVATARS = listOf(
            "\uD83E\uDDD1\u200D\uD83D\uDCBB",
            "\uD83E\uDD88",
            "\uD83E\uDDD9",
            "\uD83E\uDD16",
            "\uD83D\uDE80",
            "\uD83C\uDFA8",
            "\uD83E\uDDEC",
            "\uD83D\uDC09"
        )

        val ACHIEVEMENT_DEFS = mapOf(
            "first_solve" to "\uD83C\uDF1F First Solve",
            "streak_3" to "\uD83D\uDD25 3-Day Streak",
            "streak_7" to "\u26A1 Week Warrior",
            "streak_30" to "\uD83D\uDC51 Monthly Master",
            "no_hint_5" to "\uD83E\uDDE0 Brain Power x5",
            "no_hint_20" to "\uD83D\uDCAF Hint-Free Hero",
            "perfect_10" to "\u2728 10 Perfect Sessions",
            "level_5" to "\uD83C\uDF31 Level 5",
            "level_10" to "\u2B50 Level 10",
            "level_20" to "\uD83D\uDD25 Level 20",
            "voluntary_5" to "\uD83E\uDDD8 5 Breaks Taken",
            "speed_demon_10" to "\u23F1\uFE0F Speed Demon x10",
            "challenges_100" to "\uD83C\uDFC6 100 Challenges",
            "challenges_500" to "\uD83D\uDE80 500 Challenges"
        )

        fun hashPin(pin: String, salt: String = ""): String =
            pbkdf2Pin(pin, salt, PIN_HASH_ITERATIONS)

        fun pinMatches(pin: String, salt: String, storedHash: String): Boolean {
            if (storedHash.isBlank()) return false
            return if (isModernPinHash(storedHash)) {
                val iterations = storedHash.split(":").getOrNull(1)?.toIntOrNull() ?: PIN_HASH_ITERATIONS
                secureEquals(pbkdf2Pin(pin, salt, iterations), storedHash)
            } else {
                secureEquals(legacyHashPin(pin, salt), storedHash)
            }
        }

        fun isModernPinHash(storedHash: String): Boolean =
            storedHash.startsWith("$PIN_HASH_VERSION:")

        private fun pbkdf2Pin(pin: String, salt: String, iterations: Int): String {
            val spec = PBEKeySpec(
                pin.toCharArray(),
                normalizedPinSalt(salt),
                iterations.coerceAtLeast(60_000),
                PIN_HASH_BYTES * 8
            )
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
            return "$PIN_HASH_VERSION:$iterations:${Base64.getEncoder().encodeToString(bytes)}"
        }

        private fun legacyHashPin(pin: String, salt: String = ""): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val salted = "$salt:$pin"
            val bytes = digest.digest(salted.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        private fun secureEquals(a: String, b: String): Boolean =
            MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))

        private fun normalizedPinSalt(salt: String): ByteArray =
            salt.ifBlank { "District-empty-pin-salt-migration" }.toByteArray(Charsets.UTF_8)

        fun generateSalt(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    val isSetupDone: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SETUP_DONE] ?: false }
    val hasSeenIntro: Flow<Boolean> = context.dataStore.data.map { it[KEY_INTRO_SEEN] ?: false }
    val accountType: Flow<AccountType> = context.dataStore.data.map {
        AccountType.fromStorage(it[KEY_ACCOUNT_TYPE])
    }
    val authUserId: Flow<String> = context.dataStore.data.map { it[KEY_AUTH_USER_ID] ?: "" }
    val authEmail: Flow<String> = context.dataStore.data.map { it[KEY_AUTH_EMAIL] ?: "" }
    val authPhone: Flow<String> = context.dataStore.data.map { it[KEY_AUTH_PHONE] ?: "" }
    val requireAppOpenLock: Flow<Boolean> = context.dataStore.data.map { it[KEY_REQUIRE_APP_OPEN_LOCK] ?: true }
    val accountFirstName: Flow<String> = context.dataStore.data.map { it[KEY_ACCOUNT_FIRST_NAME] ?: "" }
    val accountLastName: Flow<String> = context.dataStore.data.map { it[KEY_ACCOUNT_LAST_NAME] ?: "" }
    val accountAge: Flow<Int> = context.dataStore.data.map { it[KEY_ACCOUNT_AGE] ?: 0 }
    val accountPhotoUri: Flow<String> = context.dataStore.data.map { it[KEY_ACCOUNT_PHOTO_URI] ?: "" }
    val accountAvatar: Flow<String> = context.dataStore.data.map { it[KEY_ACCOUNT_AVATAR] ?: AVATARS[0] }
    val accountFamilyRole: Flow<String> = context.dataStore.data.map { normalizeFamilyRole(it[KEY_ACCOUNT_FAMILY_ROLE] ?: "father") }
    val parentPinHash: Flow<String> = context.dataStore.data.map { it[KEY_PARENT_PIN_HASH] ?: "" }
    val blockedApps: Flow<Set<String>> = context.dataStore.data.map { it[KEY_BLOCKED_APPS] ?: emptySet() }
    val childId: Flow<String> = context.dataStore.data.map { it[KEY_CHILD_ID] ?: "" }
    val sessionPassApp: Flow<String> = context.dataStore.data.map { it[KEY_SESSION_PASS_APP] ?: "" }

    val lockoutUntil: Flow<Long> = context.dataStore.data.map {
        val wallTime = it[KEY_LOCKOUT_UNTIL] ?: 0L
        if (wallTime > System.currentTimeMillis()) wallTime else 0L
    }

    val sessionPassUntil: Flow<Long> = context.dataStore.data.map {
        val wallTime = it[KEY_SESSION_PASS_UNTIL] ?: 0L
        if (wallTime > System.currentTimeMillis()) wallTime else 0L
    }

    val monkModeUntil: Flow<Long> = context.dataStore.data.map {
        val wallTime = it[KEY_MONK_MODE_UNTIL] ?: 0L
        if (wallTime > System.currentTimeMillis()) wallTime else 0L
    }

    suspend fun setSetupDone(done: Boolean) {
        context.dataStore.edit { it[KEY_IS_SETUP_DONE] = done }
    }

    suspend fun setIntroSeen(seen: Boolean = true) {
        context.dataStore.edit { it[KEY_INTRO_SEEN] = seen }
    }

    suspend fun setAccountType(type: AccountType) {
        context.dataStore.edit { it[KEY_ACCOUNT_TYPE] = type.storageValue }
    }

    suspend fun setRequireAppOpenLock(required: Boolean) {
        context.dataStore.edit { it[KEY_REQUIRE_APP_OPEN_LOCK] = required }
    }

    suspend fun setAccountProfileDraft(
        firstName: String,
        lastName: String,
        age: Int,
        photoUri: String,
        avatar: String,
        familyRole: String = "father"
    ) {
        context.dataStore.edit {
            it[KEY_ACCOUNT_FIRST_NAME] = firstName.trim()
            it[KEY_ACCOUNT_LAST_NAME] = lastName.trim()
            if (age > 0) it[KEY_ACCOUNT_AGE] = age.coerceIn(1, 120) else it.remove(KEY_ACCOUNT_AGE)
            it[KEY_ACCOUNT_PHOTO_URI] = photoUri.trim()
            it[KEY_ACCOUNT_AVATAR] = avatar.ifBlank { AVATARS[0] }
            it[KEY_ACCOUNT_FAMILY_ROLE] = normalizeFamilyRole(familyRole)
        }
    }

    suspend fun prepareNewAccount(
        type: AccountType,
        firstName: String,
        lastName: String,
        age: Int,
        photoUri: String,
        avatar: String,
        familyRole: String = "father"
    ) {
        context.dataStore.edit {
            clearUserScopedPreferences(it)
            clearActiveRestrictionPreferences(it)
            it[KEY_ACCOUNT_TYPE] = type.storageValue
            it[KEY_REQUIRE_APP_OPEN_LOCK] = true
            it[KEY_ACCOUNT_FIRST_NAME] = firstName.trim()
            it[KEY_ACCOUNT_LAST_NAME] = lastName.trim()
            if (age > 0) it[KEY_ACCOUNT_AGE] = age.coerceIn(1, 120) else it.remove(KEY_ACCOUNT_AGE)
            it[KEY_ACCOUNT_PHOTO_URI] = photoUri.trim()
            it[KEY_ACCOUNT_AVATAR] = avatar.ifBlank { AVATARS[0] }
            it[KEY_ACCOUNT_FAMILY_ROLE] = normalizeFamilyRole(familyRole)
        }
    }

    suspend fun getOrCreateDeviceId(): String {
        val stored = context.dataStore.data.first()[KEY_DEVICE_ID].orEmpty()
        if (stored.isNotBlank()) return stored
        val generated = "device_" + UUID.randomUUID().toString().replace("-", "").take(16)
        context.dataStore.edit { it[KEY_DEVICE_ID] = generated }
        return generated
    }

    suspend fun setParentPin(pin: String) {
        val salt = generateSalt()
        context.dataStore.edit {
            it[KEY_PIN_SALT] = salt
            it[KEY_PARENT_PIN_HASH] = hashPin(pin, salt)
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val lockedUntil = prefs[KEY_PIN_LOCKED_UNTIL] ?: 0L
        if (lockedUntil > System.currentTimeMillis()) return false

        val storedHash = prefs[KEY_PARENT_PIN_HASH] ?: ""
        val salt = prefs[KEY_PIN_SALT] ?: ""
        val matches = pinMatches(pin, salt, storedHash)

        context.dataStore.edit {
            if (matches) {
                it[KEY_PIN_ATTEMPTS] = 0
                if (storedHash.isNotBlank() && !isModernPinHash(storedHash)) {
                    it[KEY_PARENT_PIN_HASH] = hashPin(pin, salt)
                }
            } else {
                val attempts = (it[KEY_PIN_ATTEMPTS] ?: 0) + 1
                it[KEY_PIN_ATTEMPTS] = attempts
                if (attempts >= MAX_PIN_ATTEMPTS) {
                    it[KEY_PIN_LOCKED_UNTIL] = System.currentTimeMillis() + PIN_LOCKOUT_MS
                    it[KEY_PIN_ATTEMPTS] = 0
                }
            }
        }
        return matches
    }

    val pinAttempts: Flow<Int> = context.dataStore.data.map { it[KEY_PIN_ATTEMPTS] ?: 0 }
    val pinLockedUntil: Flow<Long> = context.dataStore.data.map { it[KEY_PIN_LOCKED_UNTIL] ?: 0L }
    val monitoringEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_MONITORING_ENABLED] ?: true }
    val sessionPassDurationMin: Flow<Int> = context.dataStore.data.map {
        it[KEY_SESSION_PASS_DURATION_MIN] ?: PuzzleRules.DEFAULT_TIER1_MIN
    }
    val progressiveStage: Flow<Int> = context.dataStore.data.map {
        normalizedProgressiveStage(it[KEY_PROGRESSIVE_STAGE] ?: 0)
    }
    val progressiveSessionPassMin: Flow<Int> = context.dataStore.data.map {
        it[KEY_PROGRESSIVE_SESSION_PASS_MIN] ?: accessMinutesForStage(it[KEY_PROGRESSIVE_STAGE] ?: 0)
    }
    val lockoutDurationMin: Flow<Int> = context.dataStore.data.map {
        val stage = normalizedChallengeTier(it[KEY_PROGRESSIVE_STAGE] ?: 0)
        val children = parseChildrenJson(it[KEY_CHILDREN_JSON] ?: "[]")
        val active = children.findChild(it[KEY_CHILD_ID].orEmpty())
        val rules = PuzzleRules.fromJson(active?.optJSONObject("puzzleRules"))
        rules.lockoutMinutesForTier(stage)
    }
    val dashboardLockoutNoticeApp: Flow<String> = context.dataStore.data.map { it[KEY_DASHBOARD_LOCKOUT_NOTICE_APP] ?: "" }
    val dashboardLockoutNoticeMinutes: Flow<Int> = context.dataStore.data.map { it[KEY_DASHBOARD_LOCKOUT_NOTICE_MINUTES] ?: 0 }
    val settingsFrozenUntil: Flow<Long> = context.dataStore.data.map { 0L }
    val challengeInProgress: Flow<Boolean> = context.dataStore.data.map { it[KEY_CHALLENGE_IN_PROGRESS] ?: false }
    val challengeApp: Flow<String> = context.dataStore.data.map { it[KEY_CHALLENGE_APP] ?: "" }
    val activePuzzleRules: Flow<PuzzleRules> = context.dataStore.data.map {
        val children = parseChildrenJson(it[KEY_CHILDREN_JSON] ?: "[]")
        val active = children.findChild(it[KEY_CHILD_ID].orEmpty())
        PuzzleRules.fromJson(active?.optJSONObject("puzzleRules"))
    }
    val blockedAppFreezeUntil: Flow<Map<String, Long>> = context.dataStore.data.map {
        val children = parseChildrenJson(it[KEY_CHILDREN_JSON] ?: "[]")
        val active = children.findChild(it[KEY_CHILD_ID].orEmpty())
        active?.readLongMap("blockedAppFrozenUntil") ?: emptyMap()
    }

    suspend fun markSettingsConfiguredFreeze(): Long {
        context.dataStore.edit { it[KEY_SETTINGS_FROZEN_UNTIL] = 0L }
        updateActiveChildRecord { child -> child.put("settingsFrozenUntil", 0L) }
        return 0L
    }

    /**
     * @param totalMinutes Duration in minutes (1..1440).
     * @param songUris Up to 3 content URIs for songs the user picked from Downloads.
     */
    suspend fun startMonkMode(totalMinutes: Int, songUris: List<String> = emptyList()): Long {
        val until = System.currentTimeMillis() + totalMinutes.coerceIn(1, 24 * 60) * 60_000L
        val songs = songUris.take(3).joinToString("|")
        context.dataStore.edit {
            it[KEY_MONK_MODE_UNTIL] = until
            it[KEY_MONK_MODE_SONGS] = songs
        }
        updateActiveChildRecord { child ->
            child.put("monkModeUntil", until)
            child.put("monkModeSongs", songs)
        }
        return until
    }

    suspend fun setMonkModeSongs(songUris: List<String>) {
        val songs = songUris.filter { it.isNotBlank() }.take(3).joinToString("|")
        context.dataStore.edit {
            it[KEY_MONK_MODE_SONGS] = songs
        }
        updateActiveChildRecord { child ->
            child.put("monkModeSongs", songs)
        }
    }

    suspend fun stopMonkMode() {
        context.dataStore.edit {
            it[KEY_MONK_MODE_UNTIL] = 0L
        }
        updateActiveChildRecord { child ->
            child.put("monkModeUntil", 0L)
        }
    }

    suspend fun activeMonkModeUntil(): Long {
        val until = context.dataStore.data.first()[KEY_MONK_MODE_UNTIL] ?: 0L
        return if (until > System.currentTimeMillis()) until else 0L
    }

    /** Returns the saved song URIs for the active profile's Monk Mode playlist (may be empty). */
    val monkModeSongs: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_MONK_MODE_SONGS].orEmpty()
        if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    /** Synchronous snapshot of saved song URIs. */
    suspend fun activeMonkModeSongs(): List<String> {
        val raw = context.dataStore.data.first()[KEY_MONK_MODE_SONGS].orEmpty()
        return if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    // ---- Simit local cache ----

    val simitRulesJson: Flow<String> = context.dataStore.data.map {
        it[KEY_SIMIT_RULES_JSON] ?: "[]"
    }

    val simitFocusJson: Flow<String> = context.dataStore.data.map {
        it[KEY_SIMIT_FOCUS_JSON] ?: "[]"
    }

    val simitPairCode: Flow<String> = context.dataStore.data.map {
        it[KEY_SIMIT_PAIR_CODE] ?: ""
    }

    val simitSiteCount: Flow<Int> = simitRulesJson.map { parseSimitRules(it).size }

    val simitFocusCount: Flow<Int> = simitFocusJson.map { parseSimitFocus(it).size }

    suspend fun saveSimitLocal(
        rules: List<SimitSiteRule>,
        focusWindows: List<SimitFocusWindow>,
        pairCode: String
    ) {
        val rulesArr = JSONArray()
        rules.forEach { r ->
            rulesArr.put(JSONObject(r.toMap()))
        }
        val focusArr = JSONArray()
        focusWindows.forEach { w ->
            focusArr.put(JSONObject(w.toMap()))
        }
        context.dataStore.edit {
            it[KEY_SIMIT_RULES_JSON] = rulesArr.toString()
            it[KEY_SIMIT_FOCUS_JSON] = focusArr.toString()
            if (pairCode.isNotBlank()) it[KEY_SIMIT_PAIR_CODE] = pairCode
        }
    }

    suspend fun loadSimitRulesLocal(): List<SimitSiteRule> {
        val raw = context.dataStore.data.first()[KEY_SIMIT_RULES_JSON] ?: "[]"
        return parseSimitRules(raw)
    }

    suspend fun loadSimitFocusLocal(): List<SimitFocusWindow> {
        val raw = context.dataStore.data.first()[KEY_SIMIT_FOCUS_JSON] ?: "[]"
        return parseSimitFocus(raw)
    }

    private fun parseSimitRules(json: String): List<SimitSiteRule> {
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                SimitSiteRule.fromMap(obj.toStringMap())
            }
        }.getOrDefault(emptyList())
    }

    private fun parseSimitFocus(json: String): List<SimitFocusWindow> {
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                SimitFocusWindow.fromMap(obj.toStringMap())
            }
        }.getOrDefault(emptyList())
    }

    private fun JSONObject.toStringMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        keys().forEach { key ->
            map[key] = when (val v = get(key)) {
                is JSONArray -> (0 until v.length()).map { v.getString(it) }
                JSONObject.NULL -> null
                else -> v
            }
        }
        return map
    }

    suspend fun settingsActivityLockUntil(): Long {
        val data = context.dataStore.data.first()
        val lockout = data[KEY_LOCKOUT_UNTIL] ?: 0L
        return if (lockout > System.currentTimeMillis()) lockout else 0L
    }

    suspend fun isChallengeRunning(): Boolean =
        context.dataStore.data.first()[KEY_CHALLENGE_IN_PROGRESS] ?: false

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MONITORING_ENABLED] = enabled }
    }

    suspend fun setSessionPassDurationMin(minutes: Int) {
        context.dataStore.edit {
            val safeMinutes = minutes.coerceIn(1, 240)
            it[KEY_SESSION_PASS_DURATION_MIN] = safeMinutes
            it[KEY_PROGRESSIVE_SESSION_PASS_MIN] = safeMinutes
        }
    }

    suspend fun setLockoutDurationMin(minutes: Int) {
        context.dataStore.edit { it[KEY_LOCKOUT_DURATION_MIN] = minutes.coerceIn(1, 240) }
    }

    suspend fun advanceProgressiveSessionPassMin() {
        val data = context.dataStore.data.first()
        val current = normalizedProgressiveStage(data[KEY_PROGRESSIVE_STAGE] ?: 0)
        context.dataStore.edit {
            val next = nextProgressiveStage(current)
            it[KEY_PROGRESSIVE_STAGE] = next
            it[KEY_PROGRESSIVE_SESSION_PASS_MIN] = accessMinutesForStage(next)
        }
    }

    suspend fun resetProgressiveSessionPassMin() {
        context.dataStore.edit {
            it[KEY_PROGRESSIVE_STAGE] = 0
            it[KEY_PROGRESSIVE_SESSION_PASS_MIN] = accessMinutesForStage(0)
            it[KEY_SESSION_PASS_DURATION_MIN] = accessMinutesForStage(0)
        }
    }

    suspend fun setBlockedApps(apps: Set<String>): Set<String> {
        val activeId = context.dataStore.data.first()[KEY_CHILD_ID].orEmpty()
        if (activeId.isNotBlank()) {
            return setProfileBlockedApps(activeId, apps)
        }
        context.dataStore.edit { it[KEY_BLOCKED_APPS] = apps }
        return apps
    }

    suspend fun getActivePuzzleRules(): PuzzleRules {
        val data = context.dataStore.data.first()
        val activeId = data[KEY_CHILD_ID].orEmpty()
        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val active = children.findChild(activeId)
        return PuzzleRules.fromJson(active?.optJSONObject("puzzleRules"))
    }

    suspend fun currentChallengeTier(): Int {
        val data = context.dataStore.data.first()
        return normalizedChallengeTier(data[KEY_PROGRESSIVE_STAGE] ?: 0)
    }

    suspend fun challengePuzzleCountForCurrentStage(): Int {
        val tier = currentChallengeTier()
        return getActivePuzzleRules().puzzlesForTier(tier).coerceIn(1, 10)
    }

    suspend fun challengeRewardMinutesForCurrentStage(): Int {
        val tier = currentChallengeTier()
        return getActivePuzzleRules().minutesForTier(tier).coerceIn(1, 240)
    }

    suspend fun challengeLockoutMinutesForCurrentStage(): Int {
        val tier = currentChallengeTier()
        return getActivePuzzleRules().lockoutMinutesForTier(tier).coerceIn(1, 240)
    }

    suspend fun challengeDifficultyForCurrentStage(): Int =
        challengeDifficultyForStage(currentChallengeTier())

    suspend fun challengeNumberForCurrentStage(): Int =
        challengeNumberForStage(currentChallengeTier())

    suspend fun isProgressiveCycleComplete(): Boolean {
        val stage = context.dataStore.data.first()[KEY_PROGRESSIVE_STAGE] ?: 0
        return normalizedProgressiveStage(stage) > PuzzleRules.MAX_TIER
    }

    suspend fun applyTierCompletionCooldown(appPackage: String): Int {
        val minutes = getActivePuzzleRules().cooldownMin.coerceIn(1, 24 * 60)
        context.dataStore.edit {
            it[KEY_SESSION_PASS_APP] = ""
            it[KEY_SESSION_PASS_UNTIL] = 0L
            it[KEY_SESSION_PASS_BOOT_REF] = 0L
            it[KEY_PROGRESSIVE_STAGE] = 0
            it[KEY_PROGRESSIVE_SESSION_PASS_MIN] = accessMinutesForStage(0)
            it[KEY_SESSION_PASS_DURATION_MIN] = accessMinutesForStage(0)
            it[KEY_LOCKOUT_UNTIL] = System.currentTimeMillis() + minutes * 60 * 1000L
            it[KEY_LOCKOUT_BOOT_REF] = SystemClock.elapsedRealtime()
            it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_APP)
            it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_MINUTES)
            it[KEY_CHALLENGE_IN_PROGRESS] = false
            it[KEY_CHALLENGE_APP] = ""
            it[KEY_CHALLENGE_STARTED_AT] = 0L
        }
        saveActiveRestrictionState()
        addScreenTimeSaved(minutes * 60L)
        recordBlockedAttempt(appPackage)
        return minutes
    }

    suspend fun setLockoutUntil(timestamp: Long) {
        context.dataStore.edit {
            it[KEY_LOCKOUT_UNTIL] = timestamp
            it[KEY_LOCKOUT_BOOT_REF] = SystemClock.elapsedRealtime()
        }
        saveActiveRestrictionState()
    }

    suspend fun setDashboardLockoutNotice(appPackage: String, minutes: Int) {
        context.dataStore.edit {
            it[KEY_DASHBOARD_LOCKOUT_NOTICE_APP] = appPackage
            it[KEY_DASHBOARD_LOCKOUT_NOTICE_MINUTES] = minutes
        }
    }

    suspend fun clearDashboardLockoutNotice() {
        context.dataStore.edit {
            it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_APP)
            it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_MINUTES)
        }
    }

    suspend fun setChildId(id: String) {
        context.dataStore.edit { it[KEY_CHILD_ID] = id }
    }

    suspend fun setSessionPass(app: String, until: Long) {
        context.dataStore.edit {
            it[KEY_SESSION_PASS_APP] = app
            it[KEY_SESSION_PASS_UNTIL] = until
            it[KEY_SESSION_PASS_BOOT_REF] = SystemClock.elapsedRealtime()
        }
        saveActiveRestrictionState()
    }

    suspend fun clearSessionPass() {
        context.dataStore.edit {
            it[KEY_SESSION_PASS_APP] = ""
            it[KEY_SESSION_PASS_UNTIL] = 0L
            it[KEY_SESSION_PASS_BOOT_REF] = 0L
        }
        saveActiveRestrictionState()
    }

    suspend fun grantBlockedAppsAccess(minutes: Int, baseTime: Long = System.currentTimeMillis()) {
        context.dataStore.edit {
            it[KEY_SESSION_PASS_APP] = ALL_BLOCKED_APPS_PASS
            it[KEY_SESSION_PASS_UNTIL] = baseTime + minutes * 60 * 1000L
            it[KEY_SESSION_PASS_BOOT_REF] = SystemClock.elapsedRealtime()
            it[KEY_SESSION_PASS_DURATION_MIN] = minutes.coerceAtLeast(1)
        }
        saveActiveRestrictionState()
    }

    suspend fun grantInitialBlockedAppsAccess(baseTime: Long = System.currentTimeMillis()): Int {
        val minutes = getActivePuzzleRules().minutesForTier(0).coerceIn(1, 240)
        grantBlockedAppsAccess(minutes, baseTime)
        return minutes
    }

    suspend fun grantNextBlockedAppsAccessAfterSolve(baseTime: Long = System.currentTimeMillis()): Int {
        val currentStage = context.dataStore.data.first()[KEY_PROGRESSIVE_STAGE] ?: 0
        val tier = normalizedChallengeTier(currentStage)
        val minutes = getActivePuzzleRules().minutesForTier(tier).coerceIn(1, 240)
        context.dataStore.edit {
            it[KEY_PROGRESSIVE_STAGE] = nextProgressiveStage(currentStage)
            it[KEY_PROGRESSIVE_SESSION_PASS_MIN] = minutes
            it[KEY_SESSION_PASS_DURATION_MIN] = minutes
            it[KEY_SESSION_PASS_APP] = ALL_BLOCKED_APPS_PASS
            it[KEY_SESSION_PASS_UNTIL] = baseTime + minutes * 60 * 1000L
            it[KEY_SESSION_PASS_BOOT_REF] = SystemClock.elapsedRealtime()
            it[KEY_CHALLENGE_IN_PROGRESS] = false
            it[KEY_CHALLENGE_APP] = ""
            it[KEY_CHALLENGE_STARTED_AT] = 0L
        }
        saveActiveRestrictionState()
        return minutes
    }

    suspend fun grantBlockedAppsAccessAfterSolve(
        passMillis: Long,
        baseTime: Long = System.currentTimeMillis()
    ): Int {
        val currentStage = context.dataStore.data.first()[KEY_PROGRESSIVE_STAGE] ?: 0
        val minutesDisplay = ((passMillis + 59_999L) / 60_000L).toInt().coerceAtLeast(1)
        context.dataStore.edit {
            it[KEY_PROGRESSIVE_STAGE] = nextProgressiveStage(currentStage)
            it[KEY_PROGRESSIVE_SESSION_PASS_MIN] = minutesDisplay
            it[KEY_SESSION_PASS_DURATION_MIN] = minutesDisplay
            it[KEY_SESSION_PASS_APP] = ALL_BLOCKED_APPS_PASS
            it[KEY_SESSION_PASS_UNTIL] = baseTime + passMillis.coerceAtLeast(30_000L)
            it[KEY_SESSION_PASS_BOOT_REF] = SystemClock.elapsedRealtime()
            it[KEY_CHALLENGE_IN_PROGRESS] = false
            it[KEY_CHALLENGE_APP] = ""
            it[KEY_CHALLENGE_STARTED_AT] = 0L
        }
        saveActiveRestrictionState()
        return minutesDisplay
    }

    suspend fun startChallenge(appPackage: String) {
        context.dataStore.edit {
            it[KEY_CHALLENGE_IN_PROGRESS] = true
            it[KEY_CHALLENGE_APP] = appPackage
            it[KEY_CHALLENGE_STARTED_AT] = System.currentTimeMillis()
        }
        saveActiveRestrictionState()
    }

    suspend fun clearChallenge() {
        context.dataStore.edit {
            it[KEY_CHALLENGE_IN_PROGRESS] = false
            it[KEY_CHALLENGE_APP] = ""
            it[KEY_CHALLENGE_STARTED_AT] = 0L
        }
        saveActiveRestrictionState()
    }

    suspend fun applyChallengeFailure(appPackage: String): Int {
        val minutes = challengeLockoutMinutesForCurrentStage()
        context.dataStore.edit {
            it[KEY_SESSION_PASS_APP] = ""
            it[KEY_SESSION_PASS_UNTIL] = 0L
            it[KEY_SESSION_PASS_BOOT_REF] = 0L
            it[KEY_PROGRESSIVE_STAGE] = 0
            it[KEY_PROGRESSIVE_SESSION_PASS_MIN] = accessMinutesForStage(0)
            it[KEY_SESSION_PASS_DURATION_MIN] = accessMinutesForStage(0)
            it[KEY_LOCKOUT_UNTIL] = System.currentTimeMillis() + minutes * 60 * 1000L
            it[KEY_LOCKOUT_BOOT_REF] = SystemClock.elapsedRealtime()
            it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_APP)
            it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_MINUTES)
            it[KEY_CHALLENGE_IN_PROGRESS] = false
            it[KEY_CHALLENGE_APP] = ""
            it[KEY_CHALLENGE_STARTED_AT] = 0L
        }
        saveActiveRestrictionState()
        addScreenTimeSaved(minutes * 60L)
        recordBlockedAttempt(appPackage)
        return minutes
    }

    suspend fun resetActiveRestrictionsForChildSwitch() {
        context.dataStore.edit {
            it[KEY_SESSION_PASS_APP] = ""
            it[KEY_SESSION_PASS_UNTIL] = 0L
            it[KEY_SESSION_PASS_BOOT_REF] = 0L
            it[KEY_LOCKOUT_UNTIL] = 0L
            it[KEY_LOCKOUT_BOOT_REF] = 0L
            it[KEY_PROGRESSIVE_STAGE] = 0
            it[KEY_PROGRESSIVE_SESSION_PASS_MIN] = accessMinutesForStage(0)
            it[KEY_SESSION_PASS_DURATION_MIN] = accessMinutesForStage(0)
            it[KEY_CHALLENGE_IN_PROGRESS] = false
            it[KEY_CHALLENGE_APP] = ""
            it[KEY_CHALLENGE_STARTED_AT] = 0L
            it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_APP)
            it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_MINUTES)
            it[KEY_CURRENT_SESSION_START] = 0L
            it[KEY_CURRENT_SESSION_APP] = ""
        }
        saveActiveRestrictionState()
    }

    val dailyBudgetEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_DAILY_BUDGET_ENABLED] ?: false }
    val dailyBudgetMin: Flow<Int> = context.dataStore.data.map { it[KEY_DAILY_BUDGET_MIN] ?: DEFAULT_DAILY_BUDGET_MIN }
    val dailyUsageSeconds: Flow<Long> = context.dataStore.data.map { it[KEY_DAILY_USAGE_SECONDS] ?: 0L }
    val dailyUsageDate: Flow<String> = context.dataStore.data.map { it[KEY_DAILY_USAGE_DATE] ?: "" }
    val currentSessionStart: Flow<Long> = context.dataStore.data.map { it[KEY_CURRENT_SESSION_START] ?: 0L }
    val currentSessionApp: Flow<String> = context.dataStore.data.map { it[KEY_CURRENT_SESSION_APP] ?: "" }
    val nudgeEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_NUDGE_ENABLED] ?: true }
    val nudgeThresholdMin: Flow<Int> = context.dataStore.data.map { it[KEY_NUDGE_THRESHOLD_MIN] ?: DEFAULT_NUDGE_THRESHOLD_MIN }
    val lastNudgeTime: Flow<Long> = context.dataStore.data.map { it[KEY_LAST_NUDGE_TIME] ?: 0L }
    val rewardPoints: Flow<Int> = context.dataStore.data.map { it[KEY_REWARD_POINTS] ?: 0 }
    val totalPointsEarned: Flow<Int> = context.dataStore.data.map { it[KEY_TOTAL_POINTS_EARNED] ?: 0 }
    val voluntaryBacks: Flow<Int> = context.dataStore.data.map { it[KEY_VOLUNTARY_BACKS] ?: 0 }
    val noHintSolves: Flow<Int> = context.dataStore.data.map { it[KEY_NO_HINT_SOLVES] ?: 0 }
    val practiceSolved: Flow<Int> = context.dataStore.data.map { it[KEY_PRACTICE_SOLVED] ?: 0 }
    val practiceAttempted: Flow<Int> = context.dataStore.data.map { it[KEY_PRACTICE_ATTEMPTED] ?: 0 }
    val achievements: Flow<Set<String>> = context.dataStore.data.map { it[KEY_ACHIEVEMENTS] ?: emptySet() }
    val budgetWarningShown: Flow<Boolean> = context.dataStore.data.map { it[KEY_BUDGET_WARNING_SHOWN] ?: false }

    suspend fun setDailyBudgetEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DAILY_BUDGET_ENABLED] = enabled }
        updateActiveChildRecord { child ->
            child.put("dailyBudgetEnabled", enabled)
        }
    }

    suspend fun setDailyBudgetMin(minutes: Int) {
        val safeMinutes = minutes.coerceIn(10, 480)
        context.dataStore.edit { it[KEY_DAILY_BUDGET_MIN] = safeMinutes }
        updateActiveChildRecord { child ->
            child.put("dailyBudgetMin", safeMinutes)
        }
    }

    suspend fun addDailyUsage(seconds: Long) {
        val today = java.time.LocalDate.now().toString()
        context.dataStore.edit {
            val storedDate = it[KEY_DAILY_USAGE_DATE] ?: ""
            if (storedDate != today) {
                it[KEY_DAILY_USAGE_DATE] = today
                it[KEY_DAILY_USAGE_SECONDS] = seconds
                it[KEY_BUDGET_WARNING_SHOWN] = false
            } else {
                it[KEY_DAILY_USAGE_SECONDS] = (it[KEY_DAILY_USAGE_SECONDS] ?: 0L) + seconds
            }
        }
    }

    suspend fun getDailyRemainingSeconds(): Long {
        val today = java.time.LocalDate.now().toString()
        val data = context.dataStore.data.first()
        val storedDate = data[KEY_DAILY_USAGE_DATE] ?: ""
        if (storedDate != today) return (data[KEY_DAILY_BUDGET_MIN] ?: DEFAULT_DAILY_BUDGET_MIN) * 60L
        val budgetSec = (data[KEY_DAILY_BUDGET_MIN] ?: DEFAULT_DAILY_BUDGET_MIN) * 60L
        val usedSec = data[KEY_DAILY_USAGE_SECONDS] ?: 0L
        return (budgetSec - usedSec).coerceAtLeast(0)
    }

    suspend fun startAppSession(app: String) {
        context.dataStore.edit {
            it[KEY_CURRENT_SESSION_START] = System.currentTimeMillis()
            it[KEY_CURRENT_SESSION_APP] = app
        }
    }

    suspend fun endAppSession(): Long {
        val data = context.dataStore.data.first()
        val start = data[KEY_CURRENT_SESSION_START] ?: 0L
        if (start == 0L) return 0L
        val app = data[KEY_CURRENT_SESSION_APP].orEmpty()
        val elapsed = ((System.currentTimeMillis() - start) / 1000L).coerceAtLeast(0)
        context.dataStore.edit {
            it[KEY_CURRENT_SESSION_START] = 0L
            it[KEY_CURRENT_SESSION_APP] = ""
        }
        addDailyUsage(elapsed)
        if (app.isNotBlank() && elapsed > 0L) {
            addProfileAppUsage(app, elapsed)
        }
        return elapsed
    }

    suspend fun setLastNudgeTime(time: Long) {
        context.dataStore.edit { it[KEY_LAST_NUDGE_TIME] = time }
    }

    suspend fun setNudgeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NUDGE_ENABLED] = enabled }
        updateActiveChildRecord { child ->
            child.put("nudgeEnabled", enabled)
        }
    }

    suspend fun setNudgeThresholdMin(minutes: Int) {
        val safeMinutes = minutes.coerceIn(5, 120)
        context.dataStore.edit { it[KEY_NUDGE_THRESHOLD_MIN] = safeMinutes }
        updateActiveChildRecord { child ->
            child.put("nudgeThresholdMin", safeMinutes)
        }
    }

    suspend fun addRewardPoints(points: Int) {
        context.dataStore.edit {
            it[KEY_REWARD_POINTS] = (it[KEY_REWARD_POINTS] ?: 0) + points
            it[KEY_TOTAL_POINTS_EARNED] = (it[KEY_TOTAL_POINTS_EARNED] ?: 0) + points
        }
    }

    suspend fun spendRewardPoints(points: Int): Boolean {
        val current = context.dataStore.data.first()[KEY_REWARD_POINTS] ?: 0
        if (current < points) return false
        context.dataStore.edit { it[KEY_REWARD_POINTS] = current - points }
        return true
    }

    suspend fun recordVoluntaryBack() {
        context.dataStore.edit {
            it[KEY_VOLUNTARY_BACKS] = (it[KEY_VOLUNTARY_BACKS] ?: 0) + 1
        }
        addRewardPoints(POINTS_VOLUNTARY_BACK)
    }

    suspend fun recordNoHintSolve() {
        context.dataStore.edit {
            it[KEY_NO_HINT_SOLVES] = (it[KEY_NO_HINT_SOLVES] ?: 0) + 1
        }
        addRewardPoints(POINTS_NO_HINT_SOLVE)
    }

    suspend fun checkWeeklyStreakBonus(streakDays: Int) {
        if (streakDays < 7) return
        val today = java.time.LocalDate.now().toString()
        val lastBonus = context.dataStore.data.first()[KEY_WEEKLY_STREAK_BONUS_DATE] ?: ""
        if (lastBonus == today) return
        context.dataStore.edit { it[KEY_WEEKLY_STREAK_BONUS_DATE] = today }
        addRewardPoints(POINTS_WEEKLY_STREAK)
    }

    suspend fun addAchievement(id: String) {
        context.dataStore.edit {
            val current = it[KEY_ACHIEVEMENTS] ?: emptySet()
            it[KEY_ACHIEVEMENTS] = current + id
        }
    }

    suspend fun setBudgetWarningShown(shown: Boolean) {
        context.dataStore.edit { it[KEY_BUDGET_WARNING_SHOWN] = shown }
    }

    val childXp: Flow<Int> = context.dataStore.data.map { it[KEY_CHILD_XP] ?: 0 }
    val childLevel: Flow<Int> = context.dataStore.data.map { it[KEY_CHILD_LEVEL] ?: 1 }

    /** Spendable XP balance used by the Swag Store. */
    val xpWallet: Flow<Int> = context.dataStore.data.map { it[KEY_XP_WALLET] ?: 0 }

    /** Cumulative lifetime XP (never decreases) — used as leaderboard totalXp. */
    val lifetimeXp: Flow<Int> = context.dataStore.data.map { it[KEY_LIFETIME_XP] ?: 0 }

    /** XP earned in the current ISO week (resets weekly) — drives championship qualification. */
    val weeklyXp: Flow<Int> = context.dataStore.data.map { prefs ->
        val week = prefs[KEY_WEEKLY_XP_WEEK] ?: ""
        if (week == currentIsoWeekId()) prefs[KEY_WEEKLY_XP] ?: 0 else 0
    }

    val swagRedemptionsJson: Flow<String> = context.dataStore.data.map { it[KEY_SWAG_REDEMPTIONS_JSON] ?: "[]" }
    val childAvatar: Flow<String> = context.dataStore.data.map { it[KEY_CHILD_AVATAR] ?: AVATARS[0] }
    val childPhotoUri: Flow<String> = context.dataStore.data.map { it[KEY_CHILD_PHOTO_URI] ?: "" }
    val childTitle: Flow<String> = context.dataStore.data.map { it[KEY_CHILD_TITLE] ?: levelTitle(1) }
    val childOnboarded: Flow<Boolean> = context.dataStore.data.map { it[KEY_CHILD_ONBOARDED] ?: false }
    val streakDays: Flow<Int> = context.dataStore.data.map { it[KEY_STREAK_DAYS] ?: 0 }
    val longestStreak: Flow<Int> = context.dataStore.data.map { it[KEY_LONGEST_STREAK] ?: 0 }

    // ---- FocusIQ: on-device adaptive mastery + impulse model (see docs/FOCUSIQ.md) ----

    private fun focusIqKeyFor(id: String): String = id.ifBlank { "_default" }

    private fun parseFocusIqMap(raw: String?): JSONObject =
        if (raw.isNullOrBlank()) JSONObject()
        else runCatching { JSONObject(raw) }.getOrElse { JSONObject() }

    private fun activeFocusIqState(prefs: androidx.datastore.preferences.core.Preferences): FocusIqState {
        val id = focusIqKeyFor(prefs[KEY_CHILD_ID] ?: "")
        return FocusIqState.fromJson(parseFocusIqMap(prefs[KEY_FOCUSIQ_BY_PROFILE]).optJSONObject(id))
    }

    val focusIqState: Flow<FocusIqState> =
        context.dataStore.data.map { activeFocusIqState(it) }

    /** Composite 0..100 Focus Score for the active profile, recomputed reactively. */
    val focusScore: Flow<Int> = context.dataStore.data.map { prefs ->
        FocusIq.focusScore(activeFocusIqState(prefs), prefs[KEY_STREAK_DAYS] ?: 0, System.currentTimeMillis())
    }

    /** Feeds answered puzzles into the per-subject Elo mastery model. */
    suspend fun recordFocusIqResult(category: PracticeCategory, tier: Int, correct: Int, total: Int) {
        if (total <= 0) return
        val wins = correct.coerceIn(0, total)
        val losses = (total - wins).coerceAtLeast(0)
        context.dataStore.edit { prefs ->
            val id = focusIqKeyFor(prefs[KEY_CHILD_ID] ?: "")
            val map = parseFocusIqMap(prefs[KEY_FOCUSIQ_BY_PROFILE])
            val state = FocusIqState.fromJson(map.optJSONObject(id))
            val difficulty = FocusIq.difficultyForTier(tier)
            var rating = state.ratingFor(category)
            repeat(wins) { rating = FocusIq.updatedRating(rating, difficulty, correct = true) }
            repeat(losses) { rating = FocusIq.updatedRating(rating, difficulty, correct = false) }
            val ratings = state.ratings.toMutableMap().apply { put(category.id, rating) }
            map.put(id, state.copy(ratings = ratings, attempts = state.attempts + total).toJson())
            prefs[KEY_FOCUSIQ_BY_PROFILE] = map.toString()
        }
    }

    /** Records a blocked-app open attempt, raising the time-decayed impulse score. */
    suspend fun registerImpulseAttempt(now: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            val id = focusIqKeyFor(prefs[KEY_CHILD_ID] ?: "")
            val map = parseFocusIqMap(prefs[KEY_FOCUSIQ_BY_PROFILE])
            val state = FocusIqState.fromJson(map.optJSONObject(id))
            val impulse = FocusIq.registerImpulse(state.impulse, state.impulseUpdatedAt, now)
            map.put(id, state.copy(impulse = impulse, impulseUpdatedAt = now).toJson())
            prefs[KEY_FOCUSIQ_BY_PROFILE] = map.toString()
        }
    }

    suspend fun focusIqSnapshot(): FocusIqState = activeFocusIqState(context.dataStore.data.first())

    /** Adaptive difficulty tier the active profile should face for [category]. */
    suspend fun recommendedTier(category: PracticeCategory): Int =
        FocusIq.recommendTier(focusIqSnapshot().ratingFor(category))

    // ---- Daily Problems: shared date-seeded board + per-profile progress & streak ----

    private fun parseDailyMap(raw: String?): JSONObject =
        if (raw.isNullOrBlank()) JSONObject()
        else runCatching { JSONObject(raw) }.getOrElse { JSONObject() }

    private fun activeDailyState(prefs: androidx.datastore.preferences.core.Preferences): DailyState {
        val id = focusIqKeyFor(prefs[KEY_CHILD_ID] ?: "")
        return DailyState.fromJson(parseDailyMap(prefs[KEY_DAILY_BY_PROFILE]).optJSONObject(id))
    }

    val dailyState: Flow<DailyState> = context.dataStore.data.map { activeDailyState(it) }

    /**
     * Records a correctly-solved daily puzzle for today. When all [totalForDay] daily puzzles are
     * solved, advances the daily streak (continues if yesterday was completed, else resets to 1).
     * Returns the updated [DailyState].
     *
     * DEMO (option A): streak is tracked on-device. FUTURE / Play Store (option B): mirror to
     * Firestore + Cloud Functions for cross-device streaks and a global daily leaderboard.
     */
    suspend fun recordDailySolved(puzzleId: String, totalForDay: Int): DailyState {
        val today = java.time.LocalDate.now().toString()
        val yesterday = java.time.LocalDate.now().minusDays(1).toString()
        var result = DailyState()
        var newlyCompleted = false
        context.dataStore.edit { prefs ->
            val id = focusIqKeyFor(prefs[KEY_CHILD_ID] ?: "")
            val map = parseDailyMap(prefs[KEY_DAILY_BY_PROFILE])
            val stored = DailyState.fromJson(map.optJSONObject(id))

            // Roll today's solved set over if our stored progress is from a previous day.
            val solvedToday = (stored.solvedForToday(today) + puzzleId)
            var streak = stored.streak
            var longest = stored.longest
            var lastCompleted = stored.lastCompletedDate

            val nowComplete = totalForDay > 0 && solvedToday.size >= totalForDay
            if (nowComplete && lastCompleted != today) {
                streak = if (lastCompleted == yesterday) streak + 1 else 1
                longest = maxOf(longest, streak)
                lastCompleted = today
                newlyCompleted = true
            }

            result = DailyState(
                date = today,
                solvedIds = solvedToday,
                streak = streak,
                longest = longest,
                lastCompletedDate = lastCompleted
            )
            map.put(id, result.toJson())
            prefs[KEY_DAILY_BY_PROFILE] = map.toString()
        }
        if (newlyCompleted) addRewardPoints(POINTS_WEEKLY_STREAK / 2)
        return result
    }

    // ---- Social username ----

    suspend fun setUsername(username: String) {
        context.dataStore.edit { it[KEY_USERNAME] = username.lowercase() }
    }

    suspend fun getUsername(): String {
        return context.dataStore.data.first()[KEY_USERNAME] ?: ""
    }

    val usernameFlow: Flow<String> = context.dataStore.data.map { it[KEY_USERNAME] ?: "" }

    val totalChallengesSolved: Flow<Int> = context.dataStore.data.map { it[KEY_TOTAL_CHALLENGES_SOLVED] ?: 0 }
    val totalCorrect: Flow<Int> = context.dataStore.data.map { it[KEY_TOTAL_CORRECT] ?: 0 }
    val comboBest: Flow<Int> = context.dataStore.data.map { it[KEY_COMBO_BEST] ?: 0 }
    val screenTimeSavedSec: Flow<Long> = context.dataStore.data.map { it[KEY_SCREEN_TIME_SAVED_SEC] ?: 0L }
    val totalBlockedAttempts: Flow<Int> = context.dataStore.data.map { it[KEY_TOTAL_BLOCKED_ATTEMPTS] ?: 0 }
    val childNameLocal: Flow<String> = context.dataStore.data.map { it[KEY_CHILD_NAME] ?: "" }
    val perfectSessionCount: Flow<Int> = context.dataStore.data.map { it[KEY_PERFECT_SESSION_COUNT] ?: 0 }
    val speedBonusCount: Flow<Int> = context.dataStore.data.map { it[KEY_SPEED_BONUS_COUNT] ?: 0 }

    suspend fun setChildOnboarded(done: Boolean) {
        context.dataStore.edit { it[KEY_CHILD_ONBOARDED] = done }
    }

    suspend fun setChildAvatar(avatar: String) {
        context.dataStore.edit { it[KEY_CHILD_AVATAR] = avatar }
        updateActiveChildRecord { child ->
            child.put("avatar", avatar)
        }
    }

    suspend fun setChildPhotoUri(uri: String) {
        context.dataStore.edit { it[KEY_CHILD_PHOTO_URI] = uri }
        updateActiveChildRecord { child ->
            child.put("photoUri", uri)
        }
    }

    suspend fun setChildNameLocal(name: String) {
        context.dataStore.edit { it[KEY_CHILD_NAME] = name }
        updateActiveChildRecord { child ->
            child.put("name", name)
        }
    }

    suspend fun updateActiveChildProfile(name: String, photoUri: String) {
        val safeName = name.trim().ifBlank { "Child" }
        context.dataStore.edit {
            it[KEY_CHILD_NAME] = safeName
            it[KEY_CHILD_PHOTO_URI] = photoUri
            it[KEY_CHILD_ONBOARDED] = true
        }
        updateActiveChildRecord { child ->
            child.put("name", safeName)
            child.put("photoUri", photoUri)
        }
    }

    suspend fun updateActiveProfileDetails(
        name: String,
        username: String,
        bio: String,
        phone: String,
        photoUri: String
    ): ProfileDetailsSaveResult {
        val data = context.dataStore.data.first()
        val activeId = data[KEY_CHILD_ID].orEmpty()
        if (activeId.isBlank()) return ProfileDetailsSaveResult(false, "No active profile found.")

        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val index = children.indexOfChild(activeId)
        if (index < 0) return ProfileDetailsSaveResult(false, "Profile could not be found.")

        val obj = children.getJSONObject(index)
        val safeName = name.trim().ifBlank { obj.optString("name", "Profile") }
        val currentUsername = normalizeUsername(obj.optString("username", ""))
        val requestedUsername = normalizeUsername(username).ifBlank { currentUsername }
        if (requestedUsername.length < 6) {
            return ProfileDetailsSaveResult(false, "Username needs at least 6 letters, numbers, or underscores.")
        }

        val now = System.currentTimeMillis()
        val usernameChanged = requestedUsername != currentUsername
        val lastUsernameChange = obj.optLong("usernameUpdatedAt", 0L)
        if (usernameChanged && lastUsernameChange > 0L && now - lastUsernameChange < USERNAME_CHANGE_COOLDOWN_MS) {
            val remainingMs = USERNAME_CHANGE_COOLDOWN_MS - (now - lastUsernameChange)
            val days = ((remainingMs + 86_399_999L) / 86_400_000L).coerceAtLeast(1L)
            return ProfileDetailsSaveResult(false, "Username can be changed again in $days day${if (days == 1L) "" else "s"}.")
        }
        if (usernameChanged) {
            for (i in 0 until children.length()) {
                val other = children.optJSONObject(i) ?: continue
                if (other.optString("id") == activeId) continue
                if (normalizeUsername(other.optString("username", "")) == requestedUsername) {
                    return ProfileDetailsSaveResult(false, "That username is already used by another profile.")
                }
            }
        }

        val savedEmail = obj.optString("email", "").ifBlank { data[KEY_AUTH_EMAIL].orEmpty() }
        val savedPhone = phone.trim().ifBlank { obj.optString("phone", "").ifBlank { data[KEY_AUTH_PHONE].orEmpty() } }
        obj.put("name", safeName)
        obj.put("username", requestedUsername)
        obj.put("email", savedEmail)
        obj.put("phone", savedPhone)
        obj.put("bio", bio.trim().take(180))
        obj.put("photoUri", photoUri)
        if (usernameChanged) obj.put("usernameUpdatedAt", now)
        children.put(index, obj)

        context.dataStore.edit {
            it[KEY_CHILDREN_JSON] = children.toString()
            it[KEY_CHILD_NAME] = safeName
            it[KEY_CHILD_PHOTO_URI] = photoUri
            it[KEY_CHILD_ONBOARDED] = true
            it[KEY_USERNAME] = requestedUsername
        }
        return ProfileDetailsSaveResult(true)
    }

    suspend fun awardXp(amount: Int): Pair<Int, Boolean> {
        var leveledUp = false
        val week = currentIsoWeekId()
        context.dataStore.edit {
            val currentXp = (it[KEY_CHILD_XP] ?: 0) + amount
            val currentLevel = it[KEY_CHILD_LEVEL] ?: 1
            val needed = xpForLevel(currentLevel)
            if (currentXp >= needed) {
                it[KEY_CHILD_XP] = currentXp - needed
                it[KEY_CHILD_LEVEL] = currentLevel + 1
                it[KEY_CHILD_TITLE] = levelTitle(currentLevel + 1)
                leveledUp = true
            } else {
                it[KEY_CHILD_XP] = currentXp
            }

            // Spendable wallet + lifetime gross XP.
            it[KEY_XP_WALLET] = (it[KEY_XP_WALLET] ?: 0) + amount
            it[KEY_LIFETIME_XP] = (it[KEY_LIFETIME_XP] ?: 0) + amount

            // Weekly XP with automatic week-boundary reset.
            val storedWeek = it[KEY_WEEKLY_XP_WEEK] ?: ""
            val base = if (storedWeek == week) (it[KEY_WEEKLY_XP] ?: 0) else 0
            it[KEY_WEEKLY_XP] = base + amount
            it[KEY_WEEKLY_XP_WEEK] = week
        }
        return Pair(amount, leveledUp)
    }

    /** Spend XP from the wallet on a swag item. Records the redemption. Returns false if too poor. */
    suspend fun redeemSwag(itemId: String, itemName: String, xpCost: Int): Boolean {
        val current = context.dataStore.data.first()[KEY_XP_WALLET] ?: 0
        if (current < xpCost) return false
        val existing = context.dataStore.data.first()[KEY_SWAG_REDEMPTIONS_JSON] ?: "[]"
        val arr = runCatching { org.json.JSONArray(existing) }.getOrDefault(org.json.JSONArray())
        arr.put(
            org.json.JSONObject()
                .put("itemId", itemId)
                .put("name", itemName)
                .put("xpCost", xpCost)
                .put("redeemedAt", System.currentTimeMillis())
                .put("status", "PENDING")
        )
        context.dataStore.edit {
            it[KEY_XP_WALLET] = (it[KEY_XP_WALLET] ?: 0) - xpCost
            it[KEY_SWAG_REDEMPTIONS_JSON] = arr.toString()
        }
        return true
    }

    /** ISO-8601 week id like "2026-W27" (week starts Monday). */
    private fun currentIsoWeekId(): String {
        val now = java.time.LocalDate.now()
        val week = now.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val year = now.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR)
        return "%d-W%02d".format(year, week)
    }

    suspend fun recordChallengeResult(correct: Int, total: Int, timeSeconds: Long, hintsUsed: Int) {
        val today = java.time.LocalDate.now().toString()
        var xpEarned = correct * XP_PER_CORRECT
        context.dataStore.edit {
            it[KEY_TOTAL_CHALLENGES_SOLVED] = (it[KEY_TOTAL_CHALLENGES_SOLVED] ?: 0) + total
            it[KEY_TOTAL_CORRECT] = (it[KEY_TOTAL_CORRECT] ?: 0) + correct

            val lastDate = it[KEY_LAST_CHALLENGE_DATE] ?: ""
            if (lastDate != today) {
                val yesterday = java.time.LocalDate.now().minusDays(1).toString()
                val streak = if (lastDate == yesterday) (it[KEY_STREAK_DAYS] ?: 0) + 1 else 1
                it[KEY_STREAK_DAYS] = streak
                it[KEY_LAST_CHALLENGE_DATE] = today
                val longest = it[KEY_LONGEST_STREAK] ?: 0
                if (streak > longest) it[KEY_LONGEST_STREAK] = streak
                xpEarned += XP_STREAK_DAILY
            }

            if (correct == total) {
                it[KEY_PERFECT_SESSION_COUNT] = (it[KEY_PERFECT_SESSION_COUNT] ?: 0) + 1
                xpEarned += XP_PERFECT_SESSION
            }

            if (hintsUsed == 0 && correct == total) {
                xpEarned += XP_NO_HINT_BONUS
            }

            if (timeSeconds < total * 15L) {
                it[KEY_SPEED_BONUS_COUNT] = (it[KEY_SPEED_BONUS_COUNT] ?: 0) + 1
                xpEarned += XP_SPEED_BONUS
            }
        }

        awardXp(xpEarned)
        addRewardPoints(correct * 10)
        checkAndAwardAchievements()
    }

    suspend fun recordPracticeResult(correct: Int, total: Int, timeSeconds: Long, hintsUsed: Int) {
        context.dataStore.edit {
            it[KEY_PRACTICE_SOLVED] = (it[KEY_PRACTICE_SOLVED] ?: 0) + correct.coerceAtLeast(0)
            it[KEY_PRACTICE_ATTEMPTED] = (it[KEY_PRACTICE_ATTEMPTED] ?: 0) + total.coerceAtLeast(0)
        }
        recordChallengeResult(correct, total, timeSeconds, hintsUsed)
    }

    suspend fun recordBlockedAttempt(appPackage: String = "") {
        context.dataStore.edit {
            it[KEY_TOTAL_BLOCKED_ATTEMPTS] = (it[KEY_TOTAL_BLOCKED_ATTEMPTS] ?: 0) + 1
        }
        if (appPackage.isNotBlank()) {
            incrementProfileBlockedAttempt(appPackage)
        }
    }

    suspend fun addScreenTimeSaved(seconds: Long) {
        context.dataStore.edit {
            it[KEY_SCREEN_TIME_SAVED_SEC] = (it[KEY_SCREEN_TIME_SAVED_SEC] ?: 0L) + seconds
        }
    }

    private suspend fun checkAndAwardAchievements() {
        val data = context.dataStore.data.first()
        val current = data[KEY_ACHIEVEMENTS] ?: emptySet()
        val newBadges = mutableSetOf<String>()

        val totalSolved = data[KEY_TOTAL_CHALLENGES_SOLVED] ?: 0
        val noHints = data[KEY_NO_HINT_SOLVES] ?: 0
        val perfects = data[KEY_PERFECT_SESSION_COUNT] ?: 0
        val level = data[KEY_CHILD_LEVEL] ?: 1
        val streak = data[KEY_STREAK_DAYS] ?: 0
        val backs = data[KEY_VOLUNTARY_BACKS] ?: 0
        val speeds = data[KEY_SPEED_BONUS_COUNT] ?: 0

        if (totalSolved >= 1 && "first_solve" !in current) newBadges.add("first_solve")
        if (streak >= 3 && "streak_3" !in current) newBadges.add("streak_3")
        if (streak >= 7 && "streak_7" !in current) newBadges.add("streak_7")
        if (streak >= 30 && "streak_30" !in current) newBadges.add("streak_30")
        if (noHints >= 5 && "no_hint_5" !in current) newBadges.add("no_hint_5")
        if (noHints >= 20 && "no_hint_20" !in current) newBadges.add("no_hint_20")
        if (perfects >= 10 && "perfect_10" !in current) newBadges.add("perfect_10")
        if (level >= 5 && "level_5" !in current) newBadges.add("level_5")
        if (level >= 10 && "level_10" !in current) newBadges.add("level_10")
        if (level >= 20 && "level_20" !in current) newBadges.add("level_20")
        if (backs >= 5 && "voluntary_5" !in current) newBadges.add("voluntary_5")
        if (speeds >= 10 && "speed_demon_10" !in current) newBadges.add("speed_demon_10")
        if (totalSolved >= 100 && "challenges_100" !in current) newBadges.add("challenges_100")
        if (totalSolved >= 500 && "challenges_500" !in current) newBadges.add("challenges_500")

        if (newBadges.isNotEmpty()) {
            context.dataStore.edit {
                it[KEY_ACHIEVEMENTS] = (it[KEY_ACHIEVEMENTS] ?: emptySet()) + newBadges
            }
            addRewardPoints(newBadges.size * 25)
        }
    }

    suspend fun redeemBonusTime(minutes: Int): Boolean {
        val cost = minutes * POINTS_PER_BONUS_MINUTE
        if (!spendRewardPoints(cost)) return false
        val currentPass = context.dataStore.data.first()[KEY_SESSION_PASS_UNTIL] ?: 0L
        val base = if (currentPass > System.currentTimeMillis()) currentPass else System.currentTimeMillis()
        setSessionPass(
            context.dataStore.data.first()[KEY_SESSION_PASS_APP] ?: "",
            base + minutes * 60 * 1000L
        )
        return true
    }

    val childrenJson: Flow<String> = context.dataStore.data.map { it[KEY_CHILDREN_JSON] ?: "[]" }

    suspend fun saveChildrenJson(json: String) {
        context.dataStore.edit { it[KEY_CHILDREN_JSON] = json }
    }

    // ---- Unified family profile system ----
    // Father, mother and children are all stored in KEY_CHILDREN_JSON as profile
    // records. A "profile" generalizes the legacy "child" record by adding role,
    // age, a per-profile lock (PIN/pattern) and parent-customizable puzzle rules.

    val profiles: Flow<List<FamilyProfile>> = childrenJson.map { FamilyProfile.parse(it) }

    val activeProfileRole: Flow<ProfileRole> = context.dataStore.data.map {
        ProfileRole.fromStorage(it[KEY_ACTIVE_PROFILE_ROLE])
    }

    val activeProfileAge: Flow<Int> = context.dataStore.data.map {
        it[KEY_ACTIVE_PROFILE_AGE] ?: 10
    }

    suspend fun getProfiles(): List<FamilyProfile> =
        FamilyProfile.parse(context.dataStore.data.first()[KEY_CHILDREN_JSON] ?: "[]")

    suspend fun getProfile(profileId: String): FamilyProfile? =
        findProfileRecord(profileId)?.let { FamilyProfile.fromJson(it) }

    suspend fun findMatchingProfileId(
        role: ProfileRole,
        name: String,
        age: Int,
        username: String
    ): String? {
        val safeName = name.trim()
        val safeUsername = normalizeUsername(username)
        return getProfiles().firstOrNull { profile ->
            profile.role == role &&
                safeUsername.isNotBlank() &&
                normalizeUsername(profile.username) == safeUsername
        }?.id ?: getProfiles().firstOrNull { profile ->
            profile.role == role &&
                profile.name.equals(safeName, ignoreCase = true) &&
                profile.age == age
        }?.id
    }

    suspend fun updateProfileIdentity(
        profileId: String,
        name: String,
        firstName: String,
        lastName: String,
        username: String,
        age: Int,
        avatar: String,
        photoUri: String,
        makeActive: Boolean = false
    ) {
        val safeName = name.trim().ifBlank {
            listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Profile" }
        }
        val safeUsername = normalizeUsername(username)
        updateProfileRecord(profileId) { obj ->
            obj.put("name", safeName)
            obj.put("firstName", firstName.trim().ifBlank { safeName.substringBefore(" ") })
            obj.put("lastName", lastName.trim().ifBlank { safeName.substringAfter(" ", "") })
            if (safeUsername.length >= 6) obj.put("username", safeUsername)
            obj.put("age", age)
            obj.put("avatar", avatar.ifBlank { ProfileLock.DEFAULT_AVATAR })
            obj.put("photoUri", photoUri)
        }
        if (makeActive) switchActiveProfile(profileId)
    }

    suspend fun dedupeProfiles() {
        val data = context.dataStore.data.first()
        val source = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val kept = JSONArray()
        val seenUsernames = mutableSetOf<String>()
        val seenExactProfiles = mutableSetOf<String>()
        var changed = false
        for (i in 0 until source.length()) {
            val obj = source.optJSONObject(i) ?: continue
            val role = ProfileRole.fromStorage(obj.optString("role")).storageValue
            val username = normalizeUsername(obj.optString("username", ""))
            val exactKey = listOf(
                role,
                obj.optString("name", "").trim().lowercase(),
                obj.optInt("age", -1).toString()
            ).joinToString("|")
            val duplicateByUsername = username.isNotBlank() && !seenUsernames.add(username)
            val duplicateByExact = !seenExactProfiles.add(exactKey)
            if (duplicateByUsername || duplicateByExact) {
                changed = true
            } else {
                kept.put(obj)
            }
        }
        if (changed) {
            context.dataStore.edit { it[KEY_CHILDREN_JSON] = kept.toString() }
        }
    }

    /**
     * Creates a new family profile. Returns its generated id. Defaults are seeded so
     * [switchActiveProfile] can hydrate the active session immediately.
     */
    suspend fun createProfile(
        role: ProfileRole,
        name: String,
        age: Int,
        avatar: String = ProfileLock.DEFAULT_AVATAR,
        photoUri: String = "",
        firstName: String = "",
        lastName: String = "",
        username: String = "",
        email: String = "",
        phone: String = "",
        bio: String = "",
        blockedApps: Set<String> = emptySet(),
        makeActive: Boolean = false
    ): String {
        val id = "p_" + UUID.randomUUID().toString().replace("-", "").take(14)
        val safeFirstName = firstName.trim()
        val safeLastName = lastName.trim()
        val combinedName = listOf(safeFirstName, safeLastName).filter { it.isNotBlank() }.joinToString(" ")
        val safeName = combinedName.ifBlank {
            name.trim().ifBlank {
                when (role) {
                    ProfileRole.USER -> "Me"
                    ProfileRole.PARENT -> "Parent"
                    else -> "Child"
                }
            }
        }
        val data = context.dataStore.data.first()
        val arr = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val safeUsername = uniqueProfileUsername(username, safeName, arr)
        val isPrimaryProfile = (role == ProfileRole.USER || role == ProfileRole.PARENT) && arr.length() == 0
        val profileEmail = email.trim().ifBlank {
            if (isPrimaryProfile) data[KEY_AUTH_EMAIL].orEmpty() else ""
        }
        val profilePhone = phone.trim().ifBlank {
            if (isPrimaryProfile) data[KEY_AUTH_PHONE].orEmpty() else ""
        }
        val freezeUntil = System.currentTimeMillis() + BLOCKLIST_FREEZE_MS
        val obj = JSONObject().apply {
            put("id", id)
            put("role", role.storageValue)
            put("name", safeName)
            put("username", safeUsername)
            put("email", profileEmail)
            put("phone", profilePhone)
            put("bio", bio.trim())
            put("usernameUpdatedAt", 0L)
            put("firstName", safeFirstName.ifBlank { safeName.substringBefore(" ") })
            put("lastName", safeLastName.ifBlank { safeName.substringAfter(" ", "") })
            put("age", age)
            put("avatar", avatar.ifBlank { ProfileLock.DEFAULT_AVATAR })
            put("photoUri", photoUri)
            put("lockType", LockType.NONE.storageValue)
            put("lockHash", "")
            put("lockSalt", "")
            put("biometricEnabled", false)
            put("blockedApps", blockedApps.toJsonArray())
            put("blockedAppFrozenUntil", JSONObject().apply {
                blockedApps.forEach { put(it, freezeUntil) }
            })
            put("appUsageSeconds", JSONObject())
            put("blockedAttemptsByApp", JSONObject())
            put("sessionPassApp", "")
            put("sessionPassUntil", 0L)
            put("sessionPassDurationMin", accessMinutesForStage(0))
            put("progressiveStage", 0)
            put("progressiveSessionPassMin", accessMinutesForStage(0))
            put("lockoutUntil", 0L)
            put("challengeInProgress", false)
            put("challengeApp", "")
            put("challengeStartedAt", 0L)
            put("puzzleAttemptHistory", JSONObject())
            put("puzzleRules", PuzzleRules().toJson())
            put("avatarTitle", levelTitle(1))
            put("title", levelTitle(1))
            put("xp", 0)
            put("level", 1)
            put("streakDays", 0)
            put("longestStreak", 0)
            put("totalChallengesSolved", 0)
            put("totalCorrect", 0)
            put("speedBonusCount", 0)
            put("perfectSessionCount", 0)
            put("comboBest", 0)
            put("screenTimeSavedSec", 0L)
            put("totalBlockedAttempts", 0)
            put("rewardPoints", 0)
            put("totalPointsEarned", 0)
            put("voluntaryBacks", 0)
            put("noHintSolves", 0)
            put("practiceSolved", 0)
            put("practiceAttempted", 0)
            put("dailyBudgetEnabled", false)
            put("dailyBudgetMin", DEFAULT_DAILY_BUDGET_MIN)
            put("nudgeEnabled", true)
            put("nudgeThresholdMin", DEFAULT_NUDGE_THRESHOLD_MIN)
            put("settingsFrozenUntil", 0L)
            put("monkModeUntil", 0L)
        }
        arr.put(obj)
        context.dataStore.edit { it[KEY_CHILDREN_JSON] = arr.toString() }
        if (makeActive) switchActiveProfile(id)
        return id
    }

    private fun uniqueProfileUsername(preferred: String, fallbackName: String, profiles: JSONArray): String {
        val existing = buildSet {
            for (i in 0 until profiles.length()) {
                val username = profiles.optJSONObject(i)?.optString("username").orEmpty()
                if (username.isNotBlank()) add(username.lowercase())
            }
        }
        val fallback = normalizeUsername(fallbackName.replace(" ", "_")).ifBlank { "profile" }.let {
            if (it.length >= 6) it else "${it}_user"
        }
        val base = normalizeUsername(preferred).ifBlank { fallback }.ifBlank { "profile_user" }.let {
            if (it.length >= 6) it else "${it}_user"
        }.take(20)
        if (base !in existing) return base
        for (suffix in 2..999) {
            val candidate = "${base.take(18)}$suffix"
            if (candidate !in existing) return candidate
        }
        return "profile_${UUID.randomUUID().toString().replace("-", "").take(8)}"
    }

    suspend fun setProfileLock(profileId: String, lockType: LockType, secret: String) {
        val salt = ProfileLock.generateSalt()
        updateProfileRecord(profileId) { obj ->
            obj.put("lockType", lockType.storageValue)
            obj.put("lockSalt", salt)
            obj.put("lockHash", ProfileLock.hash(secret, salt))
            obj.remove("lockSecret")
        }
    }

    suspend fun clearProfileLock(profileId: String) {
        updateProfileRecord(profileId) { obj ->
            obj.put("lockType", LockType.NONE.storageValue)
            obj.put("lockSalt", "")
            obj.put("lockHash", "")
            obj.remove("lockSecret")
        }
    }

    suspend fun verifyProfileLock(profileId: String, secret: String): Boolean {
        val rec = findProfileRecord(profileId) ?: return false
        val hash = rec.optString("lockHash")
        if (hash.isBlank()) return true
        val salt = rec.optString("lockSalt")
        val matches = ProfileLock.matches(secret, salt, hash)
        if (matches && !ProfileLock.isModernHash(hash)) {
            updateProfileRecord(profileId) { obj ->
                obj.put("lockHash", ProfileLock.hash(secret, salt))
                obj.remove("lockSecret")
            }
        }
        return matches
    }

    /** Parent recovery is intentionally disabled; locks are stored as salted hashes only. */
    suspend fun getProfileSecret(profileId: String): String =
        ""

    /**
     * Verifies the user's PIN against their profile lock or legacy stored hash.
     */
    suspend fun verifyUserPin(secret: String): Boolean = verifyParentOverride(secret)

    /**
     * Admin override: returns true if [secret] matches a user/parent profile lock or legacy PIN.
     */
    suspend fun verifyParentOverride(secret: String): Boolean {
        val data = context.dataStore.data.first()
        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        for (i in 0 until children.length()) {
            val obj = children.optJSONObject(i) ?: continue
            val role = ProfileRole.fromStorage(obj.optString("role"))
            if (role != ProfileRole.USER && role != ProfileRole.PARENT) continue
            val hash = obj.optString("lockHash")
            if (hash.isBlank()) continue
            val salt = obj.optString("lockSalt")
            if (ProfileLock.matches(secret, salt, hash)) return true
        }
        val legacyHash = data[KEY_PARENT_PIN_HASH].orEmpty()
        if (legacyHash.isNotBlank()) {
            val legacySalt = data[KEY_PIN_SALT].orEmpty()
            if (pinMatches(secret, legacySalt, legacyHash)) return true
        }
        return false
    }

    suspend fun setProfilePhoto(profileId: String, photoUri: String) {
        updateProfileRecord(profileId) { it.put("photoUri", photoUri) }
        val activeId = context.dataStore.data.first()[KEY_CHILD_ID].orEmpty()
        if (activeId == profileId) {
            context.dataStore.edit { it[KEY_CHILD_PHOTO_URI] = photoUri }
        }
    }

    suspend fun setProfileBlockedApps(profileId: String, apps: Set<String>): Set<String> {
        val data = context.dataStore.data.first()
        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val index = children.indexOfChild(profileId)
        if (index < 0) return apps

        val now = System.currentTimeMillis()
        val obj = children.getJSONObject(index)
        val currentApps = obj.readStringSet("blockedApps")
        val currentFreeze = obj.readLongMap("blockedAppFrozenUntil")
        val frozenApps = currentApps.filter { (currentFreeze[it] ?: 0L) > now }.toSet()
        val finalApps = apps + frozenApps
        val nextFreeze = JSONObject()

        finalApps.forEach { app ->
            val existingUntil = currentFreeze[app] ?: 0L
            val freezeUntil = if (app in currentApps && existingUntil > 0L) {
                existingUntil
            } else {
                now + BLOCKLIST_FREEZE_MS
            }
            nextFreeze.put(app, freezeUntil)
        }

        obj.put("blockedApps", finalApps.toJsonArray())
        obj.put("blockedAppFrozenUntil", nextFreeze)
        children.put(index, obj)

        context.dataStore.edit {
            it[KEY_CHILDREN_JSON] = children.toString()
            if ((it[KEY_CHILD_ID] ?: "") == profileId) {
                it[KEY_BLOCKED_APPS] = finalApps
            }
        }
        return finalApps
    }

    suspend fun updateProfilePuzzleRules(profileId: String, rules: PuzzleRules) {
        updateProfileRecord(profileId) { it.put("puzzleRules", rules.toJson()) }
    }

    suspend fun getRecentPuzzleAttemptIds(
        windowMs: Long = PUZZLE_REPEAT_COOLDOWN_MS,
        now: Long = System.currentTimeMillis()
    ): Set<String> {
        val data = context.dataStore.data.first()
        val activeId = data[KEY_CHILD_ID].orEmpty()
        if (activeId.isBlank()) return emptySet()

        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val index = children.indexOfChild(activeId)
        if (index < 0) return emptySet()

        val profile = children.getJSONObject(index)
        val history = profile.optJSONObject("puzzleAttemptHistory") ?: JSONObject()
        val cutoff = now - windowMs
        val recent = mutableSetOf<String>()
        val pruned = JSONObject()
        var changed = profile.optJSONObject("puzzleAttemptHistory") == null

        val keys = history.keys()
        while (keys.hasNext()) {
            val id = keys.next()
            val attemptedAt = history.optLong(id, 0L)
            if (id.isNotBlank() && attemptedAt >= cutoff) {
                recent.add(id)
                pruned.put(id, attemptedAt)
            } else {
                changed = true
            }
        }

        if (changed) {
            profile.put("puzzleAttemptHistory", pruned)
            children.put(index, profile)
            context.dataStore.edit { it[KEY_CHILDREN_JSON] = children.toString() }
        }

        return recent
    }

    suspend fun rememberPuzzleAttempts(
        puzzleIds: Collection<String>,
        attemptedAt: Long = System.currentTimeMillis()
    ) {
        val safeIds = puzzleIds.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (safeIds.isEmpty()) return
        updateActiveChildRecord { profile ->
            val cutoff = attemptedAt - PUZZLE_REPEAT_COOLDOWN_MS
            val current = profile.optJSONObject("puzzleAttemptHistory") ?: JSONObject()
            val next = JSONObject()
            val keys = current.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                val timestamp = current.optLong(id, 0L)
                if (id.isNotBlank() && timestamp >= cutoff) next.put(id, timestamp)
            }
            safeIds.forEach { next.put(it, attemptedAt) }
            profile.put("puzzleAttemptHistory", next)
        }
    }

    suspend fun setProfileBiometric(profileId: String, enabled: Boolean) {
        updateProfileRecord(profileId) { it.put("biometricEnabled", enabled) }
    }

    suspend fun getActiveProfileAnalytics(): List<AppUsageSummary> {
        val data = context.dataStore.data.first()
        val activeId = data[KEY_CHILD_ID].orEmpty()
        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val profile = children.findChild(activeId) ?: return emptyList()
        val usage = profile.readLongMap("appUsageSeconds")
        val attempts = profile.readIntMap("blockedAttemptsByApp")
        val packages = (usage.keys + attempts.keys + profile.readStringSet("blockedApps")).toSet()
        return packages
            .map { app -> AppUsageSummary(app, usage[app] ?: 0L, attempts[app] ?: 0) }
            .sortedWith(compareByDescending<AppUsageSummary> { it.secondsUsed }.thenByDescending { it.blockedAttempts })
    }

    private suspend fun addProfileAppUsage(appPackage: String, seconds: Long) {
        val activeId = context.dataStore.data.first()[KEY_CHILD_ID].orEmpty()
        if (activeId.isBlank()) return
        updateProfileRecord(activeId) { obj ->
            val usage = obj.optJSONObject("appUsageSeconds") ?: JSONObject()
            usage.put(appPackage, usage.optLong(appPackage, 0L) + seconds)
            obj.put("appUsageSeconds", usage)
        }
    }

    private suspend fun incrementProfileBlockedAttempt(appPackage: String) {
        val activeId = context.dataStore.data.first()[KEY_CHILD_ID].orEmpty()
        if (activeId.isBlank()) return
        updateProfileRecord(activeId) { obj ->
            val attempts = obj.optJSONObject("blockedAttemptsByApp") ?: JSONObject()
            attempts.put(appPackage, attempts.optInt(appPackage, 0) + 1)
            obj.put("blockedAttemptsByApp", attempts)
        }
    }

    /** Switches the active session to [profileId], hydrating all per-profile state. */
    suspend fun switchActiveProfile(profileId: String) {
        // Persist the outgoing profile first. Monk Mode remains attached to that profile until
        // its timer expires; switching away must not silently stop it.
        val rec = findProfileRecord(profileId) ?: return
        val name = rec.optString("name", "Member")
        val apps = rec.readStringSet("blockedApps")
        val photo = rec.optString("photoUri", "")
        switchActiveChild(profileId, name, apps, photo)
        context.dataStore.edit {
            it[KEY_ACTIVE_PROFILE_ROLE] = rec.optString("role", ProfileRole.CHILD.storageValue)
            it[KEY_ACTIVE_PROFILE_AGE] = rec.optInt("age", 10)
        }
    }

    private suspend fun findProfileRecord(profileId: String): JSONObject? {
        val data = context.dataStore.data.first()
        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        return children.findChild(profileId)
    }

    private suspend fun updateProfileRecord(profileId: String, update: (JSONObject) -> Unit) {
        val data = context.dataStore.data.first()
        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val index = children.indexOfChild(profileId)
        if (index < 0) return
        val obj = children.getJSONObject(index)
        update(obj)
        children.put(index, obj)
        context.dataStore.edit { it[KEY_CHILDREN_JSON] = children.toString() }
    }

    suspend fun switchAuthenticatedUser(uid: String, email: String, phone: String = "") {
        val safeUid = uid.trim()
        if (safeUid.isBlank()) return

        val data = context.dataStore.data.first()
        val currentUid = data[KEY_AUTH_USER_ID].orEmpty()
        if (currentUid == safeUid) {
            context.dataStore.edit {
                it[KEY_AUTH_USER_ID] = safeUid
                it[KEY_AUTH_EMAIL] = email
                it[KEY_AUTH_PHONE] = phone
            }
            return
        }

        val snapshots = parseObject(data[KEY_ACCOUNT_SNAPSHOTS_JSON] ?: "{}")
        if (currentUid.isNotBlank()) {
            snapshots.put(currentUid, buildAccountSnapshot(data))
        }
        val targetSnapshot = snapshots.optJSONObject(safeUid)

        context.dataStore.edit {
            it[KEY_ACCOUNT_SNAPSHOTS_JSON] = snapshots.toString()
            it[KEY_AUTH_USER_ID] = safeUid
            it[KEY_AUTH_EMAIL] = email
            it[KEY_AUTH_PHONE] = phone
            clearUserScopedPreferences(it)
            if (targetSnapshot != null) {
                restoreAccountSnapshot(it, targetSnapshot)
            }
        }
    }

    suspend fun clearAuthenticatedUser() {
        val data = context.dataStore.data.first()
        val currentUid = data[KEY_AUTH_USER_ID].orEmpty()
        val snapshots = parseObject(data[KEY_ACCOUNT_SNAPSHOTS_JSON] ?: "{}")
        if (currentUid.isNotBlank()) {
            snapshots.put(currentUid, buildAccountSnapshot(data))
        }

        context.dataStore.edit {
            clearUserScopedPreferences(it)
            clearActiveRestrictionPreferences(it)
            it[KEY_ACCOUNT_SNAPSHOTS_JSON] = snapshots.toString()
            it.remove(KEY_AUTH_USER_ID)
            it.remove(KEY_AUTH_EMAIL)
            it.remove(KEY_AUTH_PHONE)
        }
    }

    suspend fun exportAccountSnapshotJson(): String =
        buildAccountSnapshot(context.dataStore.data.first()).toString()

    suspend fun removeLegacyProfileSecrets() {
        val data = context.dataStore.data.first()
        val childrenJson = data[KEY_CHILDREN_JSON] ?: "[]"
        val accountSnapshotsJson = data[KEY_ACCOUNT_SNAPSHOTS_JSON] ?: "{}"
        val cleanedChildren = sanitizeProfileLocksJson(childrenJson)
        val cleanedSnapshots = sanitizeAccountSnapshotsJson(accountSnapshotsJson)
        if (cleanedChildren == childrenJson && cleanedSnapshots == accountSnapshotsJson) return

        context.dataStore.edit {
            it[KEY_CHILDREN_JSON] = cleanedChildren
            it[KEY_ACCOUNT_SNAPSHOTS_JSON] = cleanedSnapshots
        }
    }

    suspend fun restoreAccountSnapshotJson(snapshotJson: String) {
        val snapshot = parseObject(snapshotJson)
        if (snapshot.length() == 0) return
        context.dataStore.edit {
            restoreAccountSnapshot(it, snapshot)
        }
    }

    suspend fun signOutActiveProfile() {
        persistActiveChildSnapshot()
        context.dataStore.edit {
            it.remove(KEY_CHILD_ID)
            it.remove(KEY_CHILD_NAME)
            it.remove(KEY_CHILD_AVATAR)
            it.remove(KEY_CHILD_PHOTO_URI)
            it.remove(KEY_CHILD_TITLE)
            it.remove(KEY_ACTIVE_PROFILE_ROLE)
            it.remove(KEY_ACTIVE_PROFILE_AGE)
            it.remove(KEY_PRACTICE_SOLVED)
            it.remove(KEY_PRACTICE_ATTEMPTED)
            it.remove(KEY_SETTINGS_FROZEN_UNTIL)
            it[KEY_BLOCKED_APPS] = emptySet()
            it[KEY_CHILD_ONBOARDED] = false
            clearActiveRestrictionPreferences(it)
        }
    }

    suspend fun seedDemoAccount(email: String) {
        val normalizedEmail = email.trim().lowercase()
        val isIndividualDemo = normalizedEmail == "test1@gmail.com"
        val isFamilyDemo = normalizedEmail == "test2@gmail.com"
        if (!isIndividualDemo && !isFamilyDemo) return

        context.dataStore.edit {
            clearUserScopedPreferences(it)
            clearActiveRestrictionPreferences(it)
            it[KEY_ACCOUNT_TYPE] = if (isIndividualDemo) {
                AccountType.INDIVIDUAL.storageValue
            } else {
                AccountType.FAMILY.storageValue
            }
        }

        if (isIndividualDemo) {
            setAccountProfileDraft(
                firstName = "Test",
                lastName = "Individual",
                age = 22,
                photoUri = "",
                avatar = AVATARS[0]
            )
            val id = createProfile(
                role = ProfileRole.PARENT,
                name = "Test Individual",
                firstName = "Test",
                lastName = "Individual",
                age = 22,
                avatar = AVATARS[0],
                username = "test_individual",
                makeActive = true
            )
            setProfileLock(id, LockType.PIN, "111111")
        } else {
            setAccountProfileDraft(
                firstName = "",
                lastName = "",
                age = 0,
                photoUri = "",
                avatar = AVATARS[0]
            )
            val father = createProfile(
                role = ProfileRole.PARENT,
                name = "Father Demo",
                firstName = "Father",
                lastName = "Demo",
                age = 38,
                avatar = AVATARS[0],
                username = "father_demo"
            )
            val mother = createProfile(
                role = ProfileRole.PARENT,
                name = "Mother Demo",
                firstName = "Mother",
                lastName = "Demo",
                age = 35,
                avatar = AVATARS[1],
                username = "mother_demo"
            )
            val childOne = createProfile(
                role = ProfileRole.CHILD,
                name = "C1 Demo",
                firstName = "C1",
                lastName = "Demo",
                age = 12,
                avatar = AVATARS[2],
                username = "c1_demo"
            )
            val childTwo = createProfile(
                role = ProfileRole.CHILD,
                name = "C2 Demo",
                firstName = "C2",
                lastName = "Demo",
                age = 9,
                avatar = AVATARS[3],
                username = "c2_demo"
            )
            listOf(father, mother, childOne, childTwo).forEach { id ->
                setProfileLock(id, LockType.PIN, "111111")
            }
            signOutActiveProfile()
        }

        setParentPin("111111")
        setChildOnboarded(true)
        setSetupDone(true)
    }

    // ---- REST (full build) auth token persistence ----
    val apiAccessToken: Flow<String> = context.dataStore.data.map { it[KEY_API_ACCESS_TOKEN] ?: "" }
    val apiRefreshToken: Flow<String> = context.dataStore.data.map { it[KEY_API_REFRESH_TOKEN] ?: "" }
    val apiUserId: Flow<String> = context.dataStore.data.map { it[KEY_API_USER_ID] ?: "" }

    suspend fun setApiTokens(accessToken: String, refreshToken: String, userId: String) {
        context.dataStore.edit {
            it[KEY_API_ACCESS_TOKEN] = accessToken
            it[KEY_API_REFRESH_TOKEN] = refreshToken
            it[KEY_API_USER_ID] = userId
        }
    }

    suspend fun updateApiAccessToken(accessToken: String, refreshToken: String) {
        context.dataStore.edit {
            it[KEY_API_ACCESS_TOKEN] = accessToken
            it[KEY_API_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearApiTokens() {
        context.dataStore.edit {
            it[KEY_API_ACCESS_TOKEN] = ""
            it[KEY_API_REFRESH_TOKEN] = ""
            it[KEY_API_USER_ID] = ""
        }
    }

    suspend fun switchActiveChild(
        childId: String,
        childName: String,
        blockedApps: Set<String>,
        photoUri: String = ""
    ) {
        val previousActiveId = context.dataStore.data.first()[KEY_CHILD_ID].orEmpty()
        val sameProfile = previousActiveId == childId
        persistActiveChildSnapshot()

        val data = context.dataStore.data.first()
        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val target = children.findChild(childId)
        val targetName = target?.optString("name", childName)?.takeIf { it.isNotBlank() } ?: childName
        val targetApps = target?.readStringSet("blockedApps") ?: blockedApps
        val targetPhotoUri = target?.optString("photoUri", photoUri).orEmpty()
        val targetAvatar = target?.optString("avatar", AVATARS[0])?.takeIf { it.isNotBlank() } ?: AVATARS[0]
        val targetLevel = target?.optInt("level", 1) ?: 1
        val targetSessionPassApp = target?.optString("sessionPassApp", "").orEmpty()
        val targetSessionPassUntil = target?.optLong("sessionPassUntil", 0L) ?: 0L
        val targetProgressiveStage = target?.optInt("progressiveStage", 0) ?: 0
        val targetProgressivePassMin = target?.optInt("progressiveSessionPassMin", accessMinutesForStage(0))
            ?: accessMinutesForStage(0)
        val targetSessionPassDurationMin = target?.optInt("sessionPassDurationMin", targetProgressivePassMin)
            ?: targetProgressivePassMin
        val targetLockoutUntil = target?.optLong("lockoutUntil", 0L) ?: 0L
        val targetChallengeInProgress = target?.optBoolean("challengeInProgress", false) ?: false
        val targetChallengeApp = target?.optString("challengeApp", "").orEmpty()
        val targetChallengeStartedAt = target?.optLong("challengeStartedAt", 0L) ?: 0L
        val targetSettingsFrozenUntil = 0L
        val targetMonkModeUntil = target?.optLong("monkModeUntil", 0L) ?: 0L
        val targetMonkModeSongs = target?.optString("monkModeSongs", "").orEmpty()

        context.dataStore.edit {
            it[KEY_CHILD_ID] = childId
            it[KEY_CHILD_NAME] = targetName
            it[KEY_BLOCKED_APPS] = targetApps
            it[KEY_CHILD_AVATAR] = targetAvatar
            it[KEY_CHILD_PHOTO_URI] = targetPhotoUri
            it[KEY_CHILD_ONBOARDED] = true
            it[KEY_CHILD_XP] = target?.optInt("xp", 0) ?: 0
            it[KEY_CHILD_LEVEL] = targetLevel
            it[KEY_CHILD_TITLE] = target?.optString("title", levelTitle(targetLevel)) ?: levelTitle(targetLevel)
            it[KEY_STREAK_DAYS] = target?.optInt("streakDays", 0) ?: 0
            it[KEY_LONGEST_STREAK] = target?.optInt("longestStreak", 0) ?: 0
            it[KEY_TOTAL_CHALLENGES_SOLVED] = target?.optInt("totalChallengesSolved", 0) ?: 0
            it[KEY_TOTAL_CORRECT] = target?.optInt("totalCorrect", 0) ?: 0
            it[KEY_SPEED_BONUS_COUNT] = target?.optInt("speedBonusCount", 0) ?: 0
            it[KEY_PERFECT_SESSION_COUNT] = target?.optInt("perfectSessionCount", 0) ?: 0
            it[KEY_COMBO_BEST] = target?.optInt("comboBest", 0) ?: 0
            it[KEY_SCREEN_TIME_SAVED_SEC] = target?.optLong("screenTimeSavedSec", 0L) ?: 0L
            it[KEY_TOTAL_BLOCKED_ATTEMPTS] = target?.optInt("totalBlockedAttempts", 0) ?: 0
            it[KEY_REWARD_POINTS] = target?.optInt("rewardPoints", 0) ?: 0
            it[KEY_TOTAL_POINTS_EARNED] = target?.optInt("totalPointsEarned", 0) ?: 0
            it[KEY_VOLUNTARY_BACKS] = target?.optInt("voluntaryBacks", 0) ?: 0
            it[KEY_NO_HINT_SOLVES] = target?.optInt("noHintSolves", 0) ?: 0
            it[KEY_PRACTICE_SOLVED] = target?.optInt("practiceSolved", 0) ?: 0
            it[KEY_PRACTICE_ATTEMPTED] = target?.optInt("practiceAttempted", 0) ?: 0
            it[KEY_ACHIEVEMENTS] = target?.readStringSet("achievements") ?: emptySet()
            it[KEY_DAILY_BUDGET_ENABLED] = target?.optBoolean("dailyBudgetEnabled", data[KEY_DAILY_BUDGET_ENABLED] ?: false)
                ?: (data[KEY_DAILY_BUDGET_ENABLED] ?: false)
            it[KEY_DAILY_BUDGET_MIN] = target?.optInt("dailyBudgetMin", data[KEY_DAILY_BUDGET_MIN] ?: DEFAULT_DAILY_BUDGET_MIN)
                ?: (data[KEY_DAILY_BUDGET_MIN] ?: DEFAULT_DAILY_BUDGET_MIN)
            it[KEY_NUDGE_ENABLED] = target?.optBoolean("nudgeEnabled", data[KEY_NUDGE_ENABLED] ?: true)
                ?: (data[KEY_NUDGE_ENABLED] ?: true)
            it[KEY_NUDGE_THRESHOLD_MIN] = target?.optInt("nudgeThresholdMin", data[KEY_NUDGE_THRESHOLD_MIN] ?: DEFAULT_NUDGE_THRESHOLD_MIN)
                ?: (data[KEY_NUDGE_THRESHOLD_MIN] ?: DEFAULT_NUDGE_THRESHOLD_MIN)
            it[KEY_SESSION_PASS_APP] = targetSessionPassApp
            it[KEY_SESSION_PASS_UNTIL] = targetSessionPassUntil
            it[KEY_SESSION_PASS_BOOT_REF] = if (targetSessionPassUntil > 0L) SystemClock.elapsedRealtime() else 0L
            it[KEY_LOCKOUT_UNTIL] = targetLockoutUntil
            it[KEY_LOCKOUT_BOOT_REF] = if (targetLockoutUntil > 0L) SystemClock.elapsedRealtime() else 0L
            it[KEY_PROGRESSIVE_STAGE] = normalizedProgressiveStage(targetProgressiveStage)
            it[KEY_PROGRESSIVE_SESSION_PASS_MIN] = targetProgressivePassMin.coerceAtLeast(1)
            it[KEY_SESSION_PASS_DURATION_MIN] = targetSessionPassDurationMin.coerceAtLeast(1)
            it[KEY_CHALLENGE_IN_PROGRESS] = targetChallengeInProgress
            it[KEY_CHALLENGE_APP] = targetChallengeApp
            it[KEY_CHALLENGE_STARTED_AT] = targetChallengeStartedAt
            it[KEY_SETTINGS_FROZEN_UNTIL] = targetSettingsFrozenUntil
            it[KEY_MONK_MODE_UNTIL] = targetMonkModeUntil
            it[KEY_MONK_MODE_SONGS] = targetMonkModeSongs
            if (!sameProfile) {
                it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_APP)
                it.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_MINUTES)
                it[KEY_CURRENT_SESSION_START] = 0L
                it[KEY_CURRENT_SESSION_APP] = ""
            }
        }
    }

    private suspend fun persistActiveChildSnapshot() {
        val data = context.dataStore.data.first()
        val activeChildId = data[KEY_CHILD_ID].orEmpty()
        if (activeChildId.isBlank()) return

        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val index = children.indexOfChild(activeChildId)
        if (index < 0) return

        val child = children.getJSONObject(index)
        child.put("name", data[KEY_CHILD_NAME] ?: child.optString("name", "Child"))
        child.put("avatar", data[KEY_CHILD_AVATAR] ?: child.optString("avatar", AVATARS[0]))
        child.put("photoUri", data[KEY_CHILD_PHOTO_URI] ?: child.optString("photoUri", ""))
        child.put("blockedApps", (data[KEY_BLOCKED_APPS] ?: emptySet()).toJsonArray())
        child.put("xp", data[KEY_CHILD_XP] ?: 0)
        child.put("level", data[KEY_CHILD_LEVEL] ?: 1)
        child.put("title", data[KEY_CHILD_TITLE] ?: levelTitle(data[KEY_CHILD_LEVEL] ?: 1))
        child.put("streakDays", data[KEY_STREAK_DAYS] ?: 0)
        child.put("longestStreak", data[KEY_LONGEST_STREAK] ?: 0)
        child.put("totalChallengesSolved", data[KEY_TOTAL_CHALLENGES_SOLVED] ?: 0)
        child.put("totalCorrect", data[KEY_TOTAL_CORRECT] ?: 0)
        child.put("speedBonusCount", data[KEY_SPEED_BONUS_COUNT] ?: 0)
        child.put("perfectSessionCount", data[KEY_PERFECT_SESSION_COUNT] ?: 0)
        child.put("comboBest", data[KEY_COMBO_BEST] ?: 0)
        child.put("screenTimeSavedSec", data[KEY_SCREEN_TIME_SAVED_SEC] ?: 0L)
        child.put("totalBlockedAttempts", data[KEY_TOTAL_BLOCKED_ATTEMPTS] ?: 0)
        child.put("rewardPoints", data[KEY_REWARD_POINTS] ?: 0)
        child.put("totalPointsEarned", data[KEY_TOTAL_POINTS_EARNED] ?: 0)
        child.put("voluntaryBacks", data[KEY_VOLUNTARY_BACKS] ?: 0)
        child.put("noHintSolves", data[KEY_NO_HINT_SOLVES] ?: 0)
        child.put("practiceSolved", data[KEY_PRACTICE_SOLVED] ?: 0)
        child.put("practiceAttempted", data[KEY_PRACTICE_ATTEMPTED] ?: 0)
        child.put("achievements", (data[KEY_ACHIEVEMENTS] ?: emptySet()).toJsonArray())
        child.put("dailyBudgetEnabled", data[KEY_DAILY_BUDGET_ENABLED] ?: false)
        child.put("dailyBudgetMin", data[KEY_DAILY_BUDGET_MIN] ?: DEFAULT_DAILY_BUDGET_MIN)
        child.put("nudgeEnabled", data[KEY_NUDGE_ENABLED] ?: true)
        child.put("nudgeThresholdMin", data[KEY_NUDGE_THRESHOLD_MIN] ?: DEFAULT_NUDGE_THRESHOLD_MIN)
        child.put("settingsFrozenUntil", 0L)
        child.put("monkModeUntil", data[KEY_MONK_MODE_UNTIL] ?: 0L)
        child.put("monkModeSongs", data[KEY_MONK_MODE_SONGS].orEmpty())
        child.writeRestrictionState(data)
        children.put(index, child)

        context.dataStore.edit {
            it[KEY_CHILDREN_JSON] = children.toString()
        }
    }

    private suspend fun saveActiveRestrictionState() {
        val data = context.dataStore.data.first()
        val activeChildId = data[KEY_CHILD_ID].orEmpty()
        if (activeChildId.isBlank()) return
        updateProfileRecord(activeChildId) { child ->
            child.writeRestrictionState(data)
        }
    }

    private suspend fun updateActiveChildRecord(update: (JSONObject) -> Unit) {
        val data = context.dataStore.data.first()
        val activeChildId = data[KEY_CHILD_ID].orEmpty()
        if (activeChildId.isBlank()) return

        val children = parseChildrenJson(data[KEY_CHILDREN_JSON] ?: "[]")
        val index = children.indexOfChild(activeChildId)
        if (index < 0) return

        val child = children.getJSONObject(index)
        update(child)
        children.put(index, child)
        context.dataStore.edit {
            it[KEY_CHILDREN_JSON] = children.toString()
        }
    }

    private fun buildAccountSnapshot(data: Preferences): JSONObject = JSONObject().apply {
        put("isSetupDone", data[KEY_IS_SETUP_DONE] ?: false)
        put("authEmail", data[KEY_AUTH_EMAIL] ?: "")
        put("authPhone", data[KEY_AUTH_PHONE] ?: "")
        put("requireAppOpenLock", data[KEY_REQUIRE_APP_OPEN_LOCK] ?: true)
        put("accountType", data[KEY_ACCOUNT_TYPE] ?: AccountType.INDIVIDUAL.storageValue)
        put("accountFirstName", data[KEY_ACCOUNT_FIRST_NAME] ?: "")
        put("accountLastName", data[KEY_ACCOUNT_LAST_NAME] ?: "")
        put("accountAge", data[KEY_ACCOUNT_AGE] ?: 0)
        put("accountPhotoUri", data[KEY_ACCOUNT_PHOTO_URI] ?: "")
        put("accountAvatar", data[KEY_ACCOUNT_AVATAR] ?: AVATARS[0])
        put("accountFamilyRole", data[KEY_ACCOUNT_FAMILY_ROLE] ?: "father")
        put("parentPinHash", data[KEY_PARENT_PIN_HASH] ?: "")
        put("pinSalt", data[KEY_PIN_SALT] ?: "")
        put("pinAttempts", data[KEY_PIN_ATTEMPTS] ?: 0)
        put("pinLockedUntil", data[KEY_PIN_LOCKED_UNTIL] ?: 0L)
        put("monitoringEnabled", data[KEY_MONITORING_ENABLED] ?: true)
        put("blockedApps", (data[KEY_BLOCKED_APPS] ?: emptySet()).toJsonArray())
        put("childId", data[KEY_CHILD_ID] ?: "")
        put("childName", data[KEY_CHILD_NAME] ?: "")
        put("childAvatar", data[KEY_CHILD_AVATAR] ?: AVATARS[0])
        put("childPhotoUri", data[KEY_CHILD_PHOTO_URI] ?: "")
        put("childTitle", data[KEY_CHILD_TITLE] ?: levelTitle(data[KEY_CHILD_LEVEL] ?: 1))
        put("childOnboarded", data[KEY_CHILD_ONBOARDED] ?: false)
        put("childrenJson", sanitizeProfileLocksJson(data[KEY_CHILDREN_JSON] ?: "[]"))
        put("childXp", data[KEY_CHILD_XP] ?: 0)
        put("childLevel", data[KEY_CHILD_LEVEL] ?: 1)
        put("xpWallet", data[KEY_XP_WALLET] ?: 0)
        put("lifetimeXp", data[KEY_LIFETIME_XP] ?: 0)
        put("weeklyXp", data[KEY_WEEKLY_XP] ?: 0)
        put("weeklyXpWeek", data[KEY_WEEKLY_XP_WEEK] ?: "")
        put("swagRedemptionsJson", data[KEY_SWAG_REDEMPTIONS_JSON] ?: "[]")
        put("streakDays", data[KEY_STREAK_DAYS] ?: 0)
        put("lastChallengeDate", data[KEY_LAST_CHALLENGE_DATE] ?: "")
        put("longestStreak", data[KEY_LONGEST_STREAK] ?: 0)
        put("totalChallengesSolved", data[KEY_TOTAL_CHALLENGES_SOLVED] ?: 0)
        put("totalCorrect", data[KEY_TOTAL_CORRECT] ?: 0)
        put("speedBonusCount", data[KEY_SPEED_BONUS_COUNT] ?: 0)
        put("perfectSessionCount", data[KEY_PERFECT_SESSION_COUNT] ?: 0)
        put("comboBest", data[KEY_COMBO_BEST] ?: 0)
        put("screenTimeSavedSec", data[KEY_SCREEN_TIME_SAVED_SEC] ?: 0L)
        put("totalBlockedAttempts", data[KEY_TOTAL_BLOCKED_ATTEMPTS] ?: 0)
        put("rewardPoints", data[KEY_REWARD_POINTS] ?: 0)
        put("totalPointsEarned", data[KEY_TOTAL_POINTS_EARNED] ?: 0)
        put("voluntaryBacks", data[KEY_VOLUNTARY_BACKS] ?: 0)
        put("noHintSolves", data[KEY_NO_HINT_SOLVES] ?: 0)
        put("practiceSolved", data[KEY_PRACTICE_SOLVED] ?: 0)
        put("practiceAttempted", data[KEY_PRACTICE_ATTEMPTED] ?: 0)
        put("weeklyStreakBonusDate", data[KEY_WEEKLY_STREAK_BONUS_DATE] ?: "")
        put("achievements", (data[KEY_ACHIEVEMENTS] ?: emptySet()).toJsonArray())
        put("dailyBudgetEnabled", data[KEY_DAILY_BUDGET_ENABLED] ?: false)
        put("dailyBudgetMin", data[KEY_DAILY_BUDGET_MIN] ?: DEFAULT_DAILY_BUDGET_MIN)
        put("dailyUsageDate", data[KEY_DAILY_USAGE_DATE] ?: "")
        put("dailyUsageSeconds", data[KEY_DAILY_USAGE_SECONDS] ?: 0L)
        put("nudgeEnabled", data[KEY_NUDGE_ENABLED] ?: true)
        put("nudgeThresholdMin", data[KEY_NUDGE_THRESHOLD_MIN] ?: DEFAULT_NUDGE_THRESHOLD_MIN)
        put("lastNudgeTime", data[KEY_LAST_NUDGE_TIME] ?: 0L)
        put("budgetWarningShown", data[KEY_BUDGET_WARNING_SHOWN] ?: false)
        put("settingsFrozenUntil", 0L)
        put("monkModeUntil", data[KEY_MONK_MODE_UNTIL] ?: 0L)
        put("monkModeSongs", data[KEY_MONK_MODE_SONGS].orEmpty())
    }

    private fun restoreAccountSnapshot(prefs: MutablePreferences, snapshot: JSONObject) {
        prefs[KEY_IS_SETUP_DONE] = snapshot.optBoolean("isSetupDone", false)
        prefs[KEY_AUTH_EMAIL] = snapshot.optString("authEmail", prefs[KEY_AUTH_EMAIL].orEmpty())
        prefs[KEY_AUTH_PHONE] = snapshot.optString("authPhone", prefs[KEY_AUTH_PHONE].orEmpty())
        prefs[KEY_REQUIRE_APP_OPEN_LOCK] = snapshot.optBoolean("requireAppOpenLock", true)
        prefs[KEY_ACCOUNT_TYPE] = snapshot.optString("accountType", AccountType.INDIVIDUAL.storageValue)
        prefs[KEY_ACCOUNT_FIRST_NAME] = snapshot.optString("accountFirstName", "")
        prefs[KEY_ACCOUNT_LAST_NAME] = snapshot.optString("accountLastName", "")
        val accountAge = snapshot.optInt("accountAge", 0)
        if (accountAge > 0) prefs[KEY_ACCOUNT_AGE] = accountAge else prefs.remove(KEY_ACCOUNT_AGE)
        prefs[KEY_ACCOUNT_PHOTO_URI] = snapshot.optString("accountPhotoUri", "")
        prefs[KEY_ACCOUNT_AVATAR] = snapshot.optString("accountAvatar", AVATARS[0])
        prefs[KEY_ACCOUNT_FAMILY_ROLE] = normalizeFamilyRole(snapshot.optString("accountFamilyRole", "father"))
        prefs[KEY_PARENT_PIN_HASH] = snapshot.optString("parentPinHash", "")
        prefs[KEY_PIN_SALT] = snapshot.optString("pinSalt", "")
        prefs[KEY_PIN_ATTEMPTS] = snapshot.optInt("pinAttempts", 0)
        prefs[KEY_PIN_LOCKED_UNTIL] = snapshot.optLong("pinLockedUntil", 0L)
        prefs[KEY_MONITORING_ENABLED] = snapshot.optBoolean("monitoringEnabled", true)
        prefs[KEY_BLOCKED_APPS] = snapshot.readStringSet("blockedApps")
        prefs[KEY_CHILD_ID] = snapshot.optString("childId", "")
        prefs[KEY_CHILD_NAME] = snapshot.optString("childName", "")
        prefs[KEY_CHILD_AVATAR] = snapshot.optString("childAvatar", AVATARS[0])
        prefs[KEY_CHILD_PHOTO_URI] = snapshot.optString("childPhotoUri", "")
        val level = snapshot.optInt("childLevel", 1)
        prefs[KEY_CHILD_TITLE] = snapshot.optString("childTitle", levelTitle(level))
        prefs[KEY_CHILD_ONBOARDED] = snapshot.optBoolean("childOnboarded", false)
        prefs[KEY_CHILDREN_JSON] = sanitizeProfileLocksJson(snapshot.optString("childrenJson", "[]"))
        prefs[KEY_CHILD_XP] = snapshot.optInt("childXp", 0)
        prefs[KEY_CHILD_LEVEL] = level
        prefs[KEY_XP_WALLET] = snapshot.optInt("xpWallet", 0)
        prefs[KEY_LIFETIME_XP] = snapshot.optInt("lifetimeXp", 0)
        prefs[KEY_WEEKLY_XP] = snapshot.optInt("weeklyXp", 0)
        prefs[KEY_WEEKLY_XP_WEEK] = snapshot.optString("weeklyXpWeek", "")
        prefs[KEY_SWAG_REDEMPTIONS_JSON] = snapshot.optString("swagRedemptionsJson", "[]")
        prefs[KEY_STREAK_DAYS] = snapshot.optInt("streakDays", 0)
        prefs[KEY_LAST_CHALLENGE_DATE] = snapshot.optString("lastChallengeDate", "")
        prefs[KEY_LONGEST_STREAK] = snapshot.optInt("longestStreak", 0)
        prefs[KEY_TOTAL_CHALLENGES_SOLVED] = snapshot.optInt("totalChallengesSolved", 0)
        prefs[KEY_TOTAL_CORRECT] = snapshot.optInt("totalCorrect", 0)
        prefs[KEY_SPEED_BONUS_COUNT] = snapshot.optInt("speedBonusCount", 0)
        prefs[KEY_PERFECT_SESSION_COUNT] = snapshot.optInt("perfectSessionCount", 0)
        prefs[KEY_COMBO_BEST] = snapshot.optInt("comboBest", 0)
        prefs[KEY_SCREEN_TIME_SAVED_SEC] = snapshot.optLong("screenTimeSavedSec", 0L)
        prefs[KEY_TOTAL_BLOCKED_ATTEMPTS] = snapshot.optInt("totalBlockedAttempts", 0)
        prefs[KEY_REWARD_POINTS] = snapshot.optInt("rewardPoints", 0)
        prefs[KEY_TOTAL_POINTS_EARNED] = snapshot.optInt("totalPointsEarned", 0)
        prefs[KEY_VOLUNTARY_BACKS] = snapshot.optInt("voluntaryBacks", 0)
        prefs[KEY_NO_HINT_SOLVES] = snapshot.optInt("noHintSolves", 0)
        prefs[KEY_PRACTICE_SOLVED] = snapshot.optInt("practiceSolved", 0)
        prefs[KEY_PRACTICE_ATTEMPTED] = snapshot.optInt("practiceAttempted", 0)
        prefs[KEY_WEEKLY_STREAK_BONUS_DATE] = snapshot.optString("weeklyStreakBonusDate", "")
        prefs[KEY_ACHIEVEMENTS] = snapshot.readStringSet("achievements")
        prefs[KEY_DAILY_BUDGET_ENABLED] = snapshot.optBoolean("dailyBudgetEnabled", false)
        prefs[KEY_DAILY_BUDGET_MIN] = snapshot.optInt("dailyBudgetMin", DEFAULT_DAILY_BUDGET_MIN)
        prefs[KEY_DAILY_USAGE_DATE] = snapshot.optString("dailyUsageDate", "")
        prefs[KEY_DAILY_USAGE_SECONDS] = snapshot.optLong("dailyUsageSeconds", 0L)
        prefs[KEY_NUDGE_ENABLED] = snapshot.optBoolean("nudgeEnabled", true)
        prefs[KEY_NUDGE_THRESHOLD_MIN] = snapshot.optInt("nudgeThresholdMin", DEFAULT_NUDGE_THRESHOLD_MIN)
        prefs[KEY_LAST_NUDGE_TIME] = snapshot.optLong("lastNudgeTime", 0L)
        prefs[KEY_BUDGET_WARNING_SHOWN] = snapshot.optBoolean("budgetWarningShown", false)
        prefs[KEY_SETTINGS_FROZEN_UNTIL] = 0L
        prefs[KEY_MONK_MODE_UNTIL] = snapshot.optLong("monkModeUntil", 0L)
        prefs[KEY_MONK_MODE_SONGS] = snapshot.optString("monkModeSongs", "")
    }

    private fun clearUserScopedPreferences(prefs: MutablePreferences) {
        prefs[KEY_IS_SETUP_DONE] = false
        prefs.remove(KEY_ACCOUNT_TYPE)
        prefs.remove(KEY_REQUIRE_APP_OPEN_LOCK)
        prefs.remove(KEY_ACCOUNT_FIRST_NAME)
        prefs.remove(KEY_ACCOUNT_LAST_NAME)
        prefs.remove(KEY_ACCOUNT_AGE)
        prefs.remove(KEY_ACCOUNT_PHOTO_URI)
        prefs.remove(KEY_ACCOUNT_AVATAR)
        prefs.remove(KEY_ACCOUNT_FAMILY_ROLE)
        prefs.remove(KEY_PARENT_PIN_HASH)
        prefs.remove(KEY_PIN_SALT)
        prefs.remove(KEY_PIN_ATTEMPTS)
        prefs.remove(KEY_PIN_LOCKED_UNTIL)
        prefs[KEY_MONITORING_ENABLED] = true
        prefs[KEY_BLOCKED_APPS] = emptySet()
        prefs.remove(KEY_MONK_MODE_UNTIL)
        prefs.remove(KEY_CHILD_ID)
        prefs.remove(KEY_CHILD_NAME)
        prefs.remove(KEY_CHILD_AVATAR)
        prefs.remove(KEY_CHILD_PHOTO_URI)
        prefs.remove(KEY_CHILD_TITLE)
        prefs[KEY_CHILD_ONBOARDED] = false
        prefs[KEY_CHILDREN_JSON] = "[]"
        prefs.remove(KEY_CHILD_XP)
        prefs.remove(KEY_CHILD_LEVEL)
        prefs.remove(KEY_XP_WALLET)
        prefs.remove(KEY_LIFETIME_XP)
        prefs.remove(KEY_WEEKLY_XP)
        prefs.remove(KEY_WEEKLY_XP_WEEK)
        prefs.remove(KEY_SWAG_REDEMPTIONS_JSON)
        prefs.remove(KEY_STREAK_DAYS)
        prefs.remove(KEY_LAST_CHALLENGE_DATE)
        prefs.remove(KEY_LONGEST_STREAK)
        prefs.remove(KEY_TOTAL_CHALLENGES_SOLVED)
        prefs.remove(KEY_TOTAL_CORRECT)
        prefs.remove(KEY_SPEED_BONUS_COUNT)
        prefs.remove(KEY_PERFECT_SESSION_COUNT)
        prefs.remove(KEY_COMBO_BEST)
        prefs.remove(KEY_SCREEN_TIME_SAVED_SEC)
        prefs.remove(KEY_TOTAL_BLOCKED_ATTEMPTS)
        prefs.remove(KEY_REWARD_POINTS)
        prefs.remove(KEY_TOTAL_POINTS_EARNED)
        prefs.remove(KEY_VOLUNTARY_BACKS)
        prefs.remove(KEY_NO_HINT_SOLVES)
        prefs.remove(KEY_PRACTICE_SOLVED)
        prefs.remove(KEY_PRACTICE_ATTEMPTED)
        prefs.remove(KEY_WEEKLY_STREAK_BONUS_DATE)
        prefs[KEY_ACHIEVEMENTS] = emptySet()
        prefs.remove(KEY_DAILY_BUDGET_ENABLED)
        prefs.remove(KEY_DAILY_BUDGET_MIN)
        prefs.remove(KEY_DAILY_USAGE_DATE)
        prefs.remove(KEY_DAILY_USAGE_SECONDS)
        prefs.remove(KEY_NUDGE_ENABLED)
        prefs.remove(KEY_NUDGE_THRESHOLD_MIN)
        prefs.remove(KEY_LAST_NUDGE_TIME)
        prefs.remove(KEY_BUDGET_WARNING_SHOWN)
        prefs.remove(KEY_SETTINGS_FROZEN_UNTIL)
    }

    private fun clearActiveRestrictionPreferences(prefs: MutablePreferences) {
        prefs[KEY_SESSION_PASS_APP] = ""
        prefs[KEY_SESSION_PASS_UNTIL] = 0L
        prefs[KEY_SESSION_PASS_BOOT_REF] = 0L
        prefs[KEY_LOCKOUT_UNTIL] = 0L
        prefs[KEY_LOCKOUT_BOOT_REF] = 0L
        prefs[KEY_PROGRESSIVE_STAGE] = 0
        prefs[KEY_PROGRESSIVE_SESSION_PASS_MIN] = accessMinutesForStage(0)
        prefs[KEY_SESSION_PASS_DURATION_MIN] = accessMinutesForStage(0)
        prefs[KEY_CHALLENGE_IN_PROGRESS] = false
        prefs[KEY_CHALLENGE_APP] = ""
        prefs[KEY_CHALLENGE_STARTED_AT] = 0L
        prefs.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_APP)
        prefs.remove(KEY_DASHBOARD_LOCKOUT_NOTICE_MINUTES)
        prefs[KEY_CURRENT_SESSION_START] = 0L
        prefs[KEY_CURRENT_SESSION_APP] = ""
    }

    private fun parseChildrenJson(json: String): JSONArray = try {
        JSONArray(json.ifBlank { "[]" })
    } catch (_: Exception) {
        JSONArray()
    }

    private fun parseObject(json: String): JSONObject = try {
        JSONObject(json.ifBlank { "{}" })
    } catch (_: Exception) {
        JSONObject()
    }

    private fun sanitizeProfileLocksJson(json: String): String {
        val children = parseChildrenJson(json)
        for (i in 0 until children.length()) {
            children.optJSONObject(i)?.remove("lockSecret")
        }
        return children.toString()
    }

    private fun sanitizeAccountSnapshotsJson(json: String): String {
        val snapshots = parseObject(json)
        val keys = snapshots.keys()
        while (keys.hasNext()) {
            val uid = keys.next()
            val snapshot = snapshots.optJSONObject(uid) ?: continue
            snapshot.put("childrenJson", sanitizeProfileLocksJson(snapshot.optString("childrenJson", "[]")))
        }
        return snapshots.toString()
    }

    private fun JSONArray.indexOfChild(childId: String): Int {
        for (i in 0 until length()) {
            if (optJSONObject(i)?.optString("id") == childId) return i
        }
        return -1
    }

    private fun JSONArray.findChild(childId: String): JSONObject? {
        val index = indexOfChild(childId)
        return if (index >= 0) optJSONObject(index) else null
    }

    private fun JSONObject.readStringSet(key: String): Set<String> {
        val values = optJSONArray(key) ?: return emptySet()
        return buildSet {
            for (i in 0 until values.length()) {
                val value = values.optString(i)
                if (value.isNotBlank()) add(value)
            }
        }
    }

    private fun JSONObject.readLongMap(key: String): Map<String, Long> {
        val values = optJSONObject(key) ?: return emptyMap()
        return buildMap {
            val keys = values.keys()
            while (keys.hasNext()) {
                val mapKey = keys.next()
                val value = values.optLong(mapKey, 0L)
                if (mapKey.isNotBlank() && value > 0L) put(mapKey, value)
            }
        }
    }

    private fun JSONObject.readIntMap(key: String): Map<String, Int> {
        val values = optJSONObject(key) ?: return emptyMap()
        return buildMap {
            val keys = values.keys()
            while (keys.hasNext()) {
                val mapKey = keys.next()
                val value = values.optInt(mapKey, 0)
                if (mapKey.isNotBlank() && value > 0) put(mapKey, value)
            }
        }
    }

    private fun JSONObject.writeRestrictionState(data: Preferences) {
        put("sessionPassApp", data[KEY_SESSION_PASS_APP] ?: "")
        put("sessionPassUntil", data[KEY_SESSION_PASS_UNTIL] ?: 0L)
        put("sessionPassDurationMin", data[KEY_SESSION_PASS_DURATION_MIN] ?: accessMinutesForStage(0))
        put("progressiveStage", data[KEY_PROGRESSIVE_STAGE] ?: 0)
        put("progressiveSessionPassMin", data[KEY_PROGRESSIVE_SESSION_PASS_MIN] ?: accessMinutesForStage(0))
        put("lockoutUntil", data[KEY_LOCKOUT_UNTIL] ?: 0L)
        put("challengeInProgress", data[KEY_CHALLENGE_IN_PROGRESS] ?: false)
        put("challengeApp", data[KEY_CHALLENGE_APP] ?: "")
        put("challengeStartedAt", data[KEY_CHALLENGE_STARTED_AT] ?: 0L)
    }

    private fun Set<String>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { arr.put(it) }
        return arr
    }
}
