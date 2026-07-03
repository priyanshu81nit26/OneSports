package app.district.data

import java.util.UUID

enum class SimitRuleMode(val storage: String) {
    BLOCK_ALWAYS("block_always"),
    BLOCK_BETWEEN("block_between"),
    ALLOW_BETWEEN("allow_between");

    companion object {
        fun fromStorage(value: String): SimitRuleMode =
            entries.firstOrNull { it.storage == value } ?: BLOCK_ALWAYS
    }
}

data class SimitSiteRule(
    val id: String = UUID.randomUUID().toString(),
    val host: String,
    val mode: SimitRuleMode = SimitRuleMode.BLOCK_ALWAYS,
    val startMinuteOfDay: Int = 0,
    val endMinuteOfDay: Int = 0,
    val enabled: Boolean = true
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "host" to host.lowercase().trim(),
        "mode" to mode.storage,
        "startMin" to startMinuteOfDay,
        "endMin" to endMinuteOfDay,
        "enabled" to enabled
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): SimitSiteRule? {
            val host = (map["host"] as? String)?.trim().orEmpty().ifBlank { return null }
            return SimitSiteRule(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                host = host,
                mode = SimitRuleMode.fromStorage(map["mode"] as? String ?: ""),
                startMinuteOfDay = (map["startMin"] as? Number)?.toInt() ?: 0,
                endMinuteOfDay = (map["endMin"] as? Number)?.toInt() ?: 0,
                enabled = map["enabled"] as? Boolean ?: true
            )
        }
    }
}

data class SimitFocusWindow(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "Focus window",
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val allowedHosts: List<String>,
    val enabled: Boolean = true
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "label" to label,
        "startMin" to startMinuteOfDay,
        "endMin" to endMinuteOfDay,
        "allowedHosts" to allowedHosts.map { it.lowercase().trim() },
        "enabled" to enabled
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): SimitFocusWindow? {
            val start = (map["startMin"] as? Number)?.toInt() ?: return null
            val end = (map["endMin"] as? Number)?.toInt() ?: return null
            @Suppress("UNCHECKED_CAST")
            val hosts = (map["allowedHosts"] as? List<String>)?.map { it.lowercase().trim() }.orEmpty()
            return SimitFocusWindow(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                label = map["label"] as? String ?: "Focus window",
                startMinuteOfDay = start,
                endMinuteOfDay = end,
                allowedHosts = hosts,
                enabled = map["enabled"] as? Boolean ?: true
            )
        }
    }
}
