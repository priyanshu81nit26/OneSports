package app.district.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

data class DeviceProfile(
    val uid: String,
    val username: String,
    val displayName: String,
    val photoUri: String = ""
)

@Singleton
class DeviceProfileStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("rise_device_profiles", Context.MODE_PRIVATE)

    fun listProfiles(): List<DeviceProfile> {
        val raw = prefs.getString(KEY, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    add(
                        DeviceProfile(
                            uid = o.optString("uid"),
                            username = o.optString("username"),
                            displayName = o.optString("displayName"),
                            photoUri = o.optString("photoUri")
                        )
                    )
                }
            }.filter { it.uid.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    fun upsert(profile: DeviceProfile) {
        val current = listProfiles().filter { it.uid != profile.uid }.toMutableList()
        current.add(0, profile)
        val array = JSONArray()
        current.take(5).forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private fun DeviceProfile.toJson(): JSONObject = JSONObject().apply {
        put("uid", uid)
        put("username", username)
        put("displayName", displayName)
        put("photoUri", photoUri)
    }

    companion object {
        private const val KEY = "profiles"
    }
}
