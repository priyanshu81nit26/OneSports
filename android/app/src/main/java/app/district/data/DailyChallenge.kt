package app.district.data

import org.json.JSONArray
import org.json.JSONObject

data class DailyState(
    val date: String = "",
    val solvedIds: Set<String> = emptySet(),
    val streak: Int = 0,
    val longest: Int = 0,
    val lastCompletedDate: String = ""
) {
    fun solvedForToday(today: String): Set<String> = if (date == today) solvedIds else emptySet()

    fun completedToday(today: String): Boolean = lastCompletedDate == today

    fun toJson(): JSONObject = JSONObject().apply {
        put("date", date)
        put("solvedIds", JSONArray(solvedIds.toList()))
        put("streak", streak)
        put("longest", longest)
        put("lastCompletedDate", lastCompletedDate)
    }

    companion object {
        fun fromJson(obj: JSONObject?): DailyState {
            if (obj == null) return DailyState()
            val arr = obj.optJSONArray("solvedIds")
            val ids = buildSet {
                if (arr != null) for (i in 0 until arr.length()) add(arr.optString(i))
            }
            return DailyState(
                date = obj.optString("date", ""),
                solvedIds = ids,
                streak = obj.optInt("streak", 0),
                longest = obj.optInt("longest", 0),
                lastCompletedDate = obj.optString("lastCompletedDate", "")
            )
        }
    }
}
