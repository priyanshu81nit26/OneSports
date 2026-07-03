package app.district.data

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Profile role for District (individual accounts only).
 */
enum class ProfileRole(val storageValue: String) {
    USER("user"),
    /** @deprecated Legacy StayFocus family role — not used in District UI. */
    PARENT("parent"),
    /** @deprecated Legacy StayFocus family role — not used in District UI. */
    CHILD("child");

    companion object {
        fun fromStorage(value: String?): ProfileRole =
            entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) } ?: USER
    }
}

/**
 * Lock methods a profile can use to verify identity before entering.
 */
enum class LockType(val storageValue: String) {
    NONE("none"),
    PIN("pin"),
    PATTERN("pattern");

    companion object {
        fun fromStorage(value: String?): LockType =
            entries.firstOrNull { it.storageValue == value } ?: NONE
    }
}

enum class AccountType(val storageValue: String) {
    INDIVIDUAL("individual"),
    /** @deprecated Legacy StayFocus family accounts — District is individual-only. */
    FAMILY("family");

    companion object {
        fun fromStorage(value: String?): AccountType =
            entries.firstOrNull { it.storageValue == value } ?: INDIVIDUAL
    }
}

/**
 * Parent-customizable progressive access rules used when a child (or self-monitored
 * parent) tries to open a blocked app.
 *
 * Tier 1: solve [tier1Puzzles] puzzles -> [tier1Min] minutes of access.
 * Tier 2: solve [tier2Puzzles] puzzles -> [tier2Min] minutes of access.
 * Tier 3: solve [tier3Puzzles] puzzles -> [tier3Min] minutes of access.
 * After tier 3 expires -> [cooldownMin] minute cooldown before tier 1 again.
 */
data class PuzzleRules(
    val tier1Puzzles: Int = DEFAULT_TIER1_PUZZLES,
    val tier1Min: Int = DEFAULT_TIER1_MIN,
    val tier1LockoutMin: Int = DEFAULT_TIER1_LOCKOUT_MIN,
    val tier2Puzzles: Int = DEFAULT_TIER2_PUZZLES,
    val tier2Min: Int = DEFAULT_TIER2_MIN,
    val tier2LockoutMin: Int = DEFAULT_TIER2_LOCKOUT_MIN,
    val tier3Puzzles: Int = DEFAULT_TIER3_PUZZLES,
    val tier3Min: Int = DEFAULT_TIER3_MIN,
    val tier3LockoutMin: Int = DEFAULT_TIER3_LOCKOUT_MIN,
    val cooldownMin: Int = DEFAULT_COOLDOWN_MIN
) {
    fun puzzlesForTier(tier: Int): Int = when (tier.coerceIn(0, 2)) {
        0 -> tier1Puzzles
        1 -> tier2Puzzles
        else -> tier3Puzzles
    }

    fun minutesForTier(tier: Int): Int = when (tier.coerceIn(0, 2)) {
        0 -> tier1Min
        1 -> tier2Min
        else -> tier3Min
    }

    fun lockoutMinutesForTier(tier: Int): Int = when (tier.coerceIn(0, 2)) {
        0 -> tier1LockoutMin
        1 -> tier2LockoutMin
        else -> tier3LockoutMin
    }

    companion object {
        const val DEFAULT_TIER1_PUZZLES = 1
        const val DEFAULT_TIER1_MIN = 5
        const val DEFAULT_TIER1_LOCKOUT_MIN = 15
        const val DEFAULT_TIER2_PUZZLES = 3
        const val DEFAULT_TIER2_MIN = 10
        const val DEFAULT_TIER2_LOCKOUT_MIN = 30
        const val DEFAULT_TIER3_PUZZLES = 5
        const val DEFAULT_TIER3_MIN = 15
        const val DEFAULT_TIER3_LOCKOUT_MIN = 45
        const val DEFAULT_COOLDOWN_MIN = 180
        const val MAX_TIER = 2

        fun fromJson(obj: JSONObject?): PuzzleRules {
            if (obj == null) return PuzzleRules()
            return PuzzleRules(
                tier1Puzzles = obj.optInt("tier1Puzzles", DEFAULT_TIER1_PUZZLES),
                tier1Min = obj.optInt("tier1Min", DEFAULT_TIER1_MIN),
                tier1LockoutMin = obj.optInt("tier1LockoutMin", DEFAULT_TIER1_LOCKOUT_MIN),
                tier2Puzzles = obj.optInt("tier2Puzzles", DEFAULT_TIER2_PUZZLES),
                tier2Min = obj.optInt("tier2Min", DEFAULT_TIER2_MIN),
                tier2LockoutMin = obj.optInt("tier2LockoutMin", DEFAULT_TIER2_LOCKOUT_MIN),
                tier3Puzzles = obj.optInt("tier3Puzzles", DEFAULT_TIER3_PUZZLES),
                tier3Min = obj.optInt("tier3Min", DEFAULT_TIER3_MIN),
                tier3LockoutMin = obj.optInt("tier3LockoutMin", DEFAULT_TIER3_LOCKOUT_MIN),
                cooldownMin = obj.optInt("cooldownMin", DEFAULT_COOLDOWN_MIN)
            )
        }
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("tier1Puzzles", tier1Puzzles)
        put("tier1Min", tier1Min)
        put("tier1LockoutMin", tier1LockoutMin)
        put("tier2Puzzles", tier2Puzzles)
        put("tier2Min", tier2Min)
        put("tier2LockoutMin", tier2LockoutMin)
        put("tier3Puzzles", tier3Puzzles)
        put("tier3Min", tier3Min)
        put("tier3LockoutMin", tier3LockoutMin)
        put("cooldownMin", cooldownMin)
    }
}

/**
 * A single family member profile. This is the read model surfaced to the UI; the
 * authoritative storage lives in [PrefsManager] as a JSON array.
 */
data class FamilyProfile(
    val id: String,
    val role: ProfileRole,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val bio: String,
    val usernameUpdatedAt: Long,
    val age: Int,
    val avatar: String,
    val photoUri: String,
    val lockType: LockType,
    val biometricEnabled: Boolean,
    val blockedApps: Set<String>,
    val blockedAppFrozenUntil: Map<String, Long>,
    val appUsageSeconds: Map<String, Long>,
    val blockedAttemptsByApp: Map<String, Int>,
    val level: Int,
    val xp: Int,
    val streakDays: Int,
    val totalChallengesSolved: Int,
    val puzzleRules: PuzzleRules
) {
    val hasLock: Boolean get() = lockType != LockType.NONE
    val isPrimaryUser: Boolean get() = role == ProfileRole.USER || role == ProfileRole.PARENT

    companion object {
        fun fromJson(obj: JSONObject): FamilyProfile {
            return FamilyProfile(
                id = obj.optString("id"),
                role = ProfileRole.fromStorage(obj.optString("role").ifBlank { null }),
                name = obj.optString("name", "Member"),
                username = obj.optString("username", ""),
                email = obj.optString("email", ""),
                phone = obj.optString("phone", ""),
                bio = obj.optString("bio", ""),
                usernameUpdatedAt = obj.optLong("usernameUpdatedAt", 0L),
                age = obj.optInt("age", 10),
                avatar = obj.optString("avatar", ProfileLock.DEFAULT_AVATAR),
                photoUri = obj.optString("photoUri", ""),
                lockType = LockType.fromStorage(obj.optString("lockType").ifBlank { null }),
                biometricEnabled = obj.optBoolean("biometricEnabled", false),
                blockedApps = obj.jsonStringSet("blockedApps"),
                blockedAppFrozenUntil = obj.jsonLongMap("blockedAppFrozenUntil"),
                appUsageSeconds = obj.jsonLongMap("appUsageSeconds"),
                blockedAttemptsByApp = obj.jsonIntMap("blockedAttemptsByApp"),
                level = obj.optInt("level", 1),
                xp = obj.optInt("xp", 0),
                streakDays = obj.optInt("streakDays", 0),
                totalChallengesSolved = obj.optInt("totalChallengesSolved", 0),
                puzzleRules = PuzzleRules.fromJson(obj.optJSONObject("puzzleRules"))
            )
        }

        fun parse(json: String): List<FamilyProfile> = try {
            val arr = JSONArray(json.ifBlank { "[]" })
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val profile = fromJson(obj)
                    if (profile.id.isNotBlank()) add(profile)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

/**
 * Lock + hashing utilities shared by profile creation/verification.
 */
object ProfileLock {
    const val DEFAULT_AVATAR = "\uD83E\uDDD1"
    const val PATTERN_MIN_DOTS = 4
    private const val HASH_VERSION = "pbkdf2-sha256"
    private const val HASH_ITERATIONS = 120_000
    private const val HASH_BYTES = 32

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(secret: String, salt: String): String =
        pbkdf2(secret, salt, HASH_ITERATIONS)

    fun matches(secret: String, salt: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        return if (isModernHash(storedHash)) {
            val iterations = storedHash.split(":").getOrNull(1)?.toIntOrNull() ?: HASH_ITERATIONS
            secureEquals(pbkdf2(secret, salt, iterations), storedHash)
        } else {
            secureEquals(legacyHash(secret, salt), storedHash)
        }
    }

    fun isModernHash(storedHash: String): Boolean =
        storedHash.startsWith("$HASH_VERSION:")

    private fun pbkdf2(secret: String, salt: String, iterations: Int): String {
        val spec = PBEKeySpec(
            secret.toCharArray(),
            normalizedSalt(salt),
            iterations.coerceAtLeast(60_000),
            HASH_BYTES * 8
        )
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
        return "$HASH_VERSION:$iterations:${Base64.getEncoder().encodeToString(bytes)}"
    }

    private fun legacyHash(secret: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$secret".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun secureEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))

    private fun normalizedSalt(salt: String): ByteArray =
        salt.ifBlank { "District-empty-profile-salt-migration" }.toByteArray(Charsets.UTF_8)

    /** Pattern dots are 0..8 (3x3 grid). Encode as a comma-joined string before hashing. */
    fun encodePattern(dots: List<Int>): String = dots.joinToString(",")
}

internal fun JSONObject.jsonStringSet(key: String): Set<String> {
    val values = optJSONArray(key) ?: return emptySet()
    return buildSet {
        for (i in 0 until values.length()) {
            val value = values.optString(i)
            if (value.isNotBlank()) add(value)
        }
    }
}

internal fun JSONObject.jsonLongMap(key: String): Map<String, Long> {
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

internal fun JSONObject.jsonIntMap(key: String): Map<String, Int> {
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

internal fun Set<String>.toJsonStringArray(): JSONArray {
    val arr = JSONArray()
    forEach { arr.put(it) }
    return arr
}
