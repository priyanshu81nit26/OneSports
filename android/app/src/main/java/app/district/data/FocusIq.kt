package app.district.data

import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

data class FocusIqState(
    val ratings: Map<String, Double> = emptyMap(),
    val impulse: Double = 0.0,
    val impulseUpdatedAt: Long = 0L,
    val attempts: Int = 0
) {
    val hasActivity: Boolean
        get() = attempts > 0 || ratings.isNotEmpty() || impulse > 0.0

    fun ratingFor(category: PracticeCategory): Double =
        ratings[category.id] ?: FocusIq.DEFAULT_RATING

    fun toJson(): JSONObject = JSONObject().apply {
        val r = JSONObject()
        ratings.forEach { (k, v) -> r.put(k, v) }
        put("ratings", r)
        put("impulse", impulse)
        put("impulseUpdatedAt", impulseUpdatedAt)
        put("attempts", attempts)
    }

    companion object {
        fun fromJson(obj: JSONObject?): FocusIqState {
            if (obj == null) return FocusIqState()
            val ratingsObj = obj.optJSONObject("ratings")
            val ratings = buildMap {
                if (ratingsObj != null) {
                    val keys = ratingsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        put(key, ratingsObj.optDouble(key, FocusIq.DEFAULT_RATING))
                    }
                }
            }
            return FocusIqState(
                ratings = ratings,
                impulse = obj.optDouble("impulse", 0.0).coerceIn(0.0, 1.0),
                impulseUpdatedAt = obj.optLong("impulseUpdatedAt", 0L),
                attempts = obj.optInt("attempts", 0)
            )
        }
    }
}

object FocusIq {
    const val DEFAULT_RATING = 1000.0
    const val MIN_RATING = 400.0
    const val MAX_RATING = 2200.0
    const val K = 32.0
    const val TARGET_SUCCESS = 0.78
    const val DIFFICULTY_BASE = 800.0
    const val DIFFICULTY_STEP = 200.0
    const val MAX_TIER = 3
    const val IMPULSE_HALF_LIFE_MS = 90.0 * 60.0 * 1000.0
    const val IMPULSE_INCREMENT = 0.34

    private const val DISPLAY_MIN = 500.0
    private const val DISPLAY_MAX = 1500.0

    fun difficultyForTier(tier: Int): Double =
        DIFFICULTY_BASE + tier.coerceIn(0, MAX_TIER) * DIFFICULTY_STEP

    fun expectedScore(rating: Double, difficulty: Double): Double =
        1.0 / (1.0 + 10.0.pow((difficulty - rating) / 400.0))

    fun updatedRating(rating: Double, difficulty: Double, correct: Boolean): Double {
        val outcome = if (correct) 1.0 else 0.0
        val next = rating + K * (outcome - expectedScore(rating, difficulty))
        return next.coerceIn(MIN_RATING, MAX_RATING)
    }

    fun recommendTier(rating: Double): Int {
        var best = 0
        var bestGap = Double.MAX_VALUE
        for (tier in 0..MAX_TIER) {
            val gap = abs(expectedScore(rating, difficultyForTier(tier)) - TARGET_SUCCESS)
            if (gap < bestGap) {
                bestGap = gap
                best = tier
            }
        }
        return best
    }

    fun decayImpulse(impulse: Double, elapsedMs: Long): Double {
        if (impulse <= 0.0) return 0.0
        val dt = elapsedMs.coerceAtLeast(0L).toDouble()
        return (impulse * 0.5.pow(dt / IMPULSE_HALF_LIFE_MS)).coerceIn(0.0, 1.0)
    }

    fun registerImpulse(previous: Double, previousAt: Long, now: Long): Double {
        val decayed = decayImpulse(previous, now - previousAt)
        return (decayed + IMPULSE_INCREMENT).coerceIn(0.0, 1.0)
    }

    fun averageMastery(state: FocusIqState): Int {
        if (state.ratings.isEmpty()) return 0
        return state.ratings.values.map { normalizedMastery(it) }.average().roundToInt()
    }

    fun normalizedMastery(rating: Double): Int =
        (((rating - DISPLAY_MIN) / (DISPLAY_MAX - DISPLAY_MIN)) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)

    fun focusScore(state: FocusIqState, streakDays: Int, now: Long): Int {
        if (!state.hasActivity && streakDays <= 0) return 0
        val mastery = averageMastery(state).toDouble()
        val liveImpulse = decayImpulse(state.impulse, now - state.impulseUpdatedAt)
        val discipline = (1.0 - liveImpulse) * 100.0
        val consistency = (streakDays.coerceAtLeast(0).coerceAtMost(14) / 14.0) * 100.0
        return (0.5 * mastery + 0.3 * discipline + 0.2 * consistency)
            .roundToInt()
            .coerceIn(0, 100)
    }
}
