package app.district.data

enum class EventCategory(val label: String, val emoji: String) {
    FOOTBALL("Football", "⚽"),
    CRICKET("Cricket", "🏏"),
    BASKETBALL("Basketball", "🏀"),
    BADMINTON("Badminton", "🏸"),
    TENNIS("Tennis", "🎾"),
    MARATHON("Running", "🏃"),
    FITNESS("Fitness", "🏋️"),
    CYCLING("Cycling", "🚴"),
    SWIMMING("Swimming", "🏊"),
    SPORTS("Multi-sport", "🏅"),
    DANCE("Dance", "💃"),
    CHESS("Chess", "♟️"),
    OTHER("Other", "✨");

    companion object {
        fun fromStorage(value: String?): EventCategory =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER

        val sportGrid: List<EventCategory> = listOf(
            FOOTBALL, CRICKET, BASKETBALL, BADMINTON, TENNIS,
            MARATHON, FITNESS, CYCLING, SWIMMING, SPORTS
        )
    }
}

enum class EventDateFilter { ALL, TODAY, TOMORROW }

/** Preset tags organisers can attach when creating an event. */
object EventTags {
    val presets = listOf(
        "Beginner friendly",
        "Competitive",
        "Family",
        "Outdoor",
        "Indoor",
        "Free entry",
        "Multi-day",
        "Team event",
        "Solo",
        "Charity"
    )
}

data class EventListFilter(
    val category: EventCategory? = null,
    val tag: String? = null,
    val search: String = "",
    val timelineOnly: Boolean = false,
    val mineOnly: Boolean = false,
    val dateFilter: EventDateFilter = EventDateFilter.ALL
)

enum class CommunityType(val label: String, val emoji: String) {
    GYM("Gym / Fitness", "🏋️"),
    SOCIETY("Housing Society", "🏘️"),
    CLUB("Club / Hobby", "🎯"),
    SPORTS("Sports Group", "⚽"),
    OTHER("Other", "🌐");

    companion object {
        fun fromStorage(value: String?): CommunityType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}

enum class ConnectionStatus { NONE, PENDING, CONNECTED }

data class EventTimelineSegment(
    val label: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

data class EventTimelineDay(
    val dayNumber: Int = 1,
    val title: String = "",
    val venue: String = "",
    val segments: List<EventTimelineSegment> = emptyList(),
    val extraFields: List<EventCustomField> = emptyList()
)

data class EventCustomField(
    val label: String = "",
    val value: String = ""
)

data class DistrictEvent(
    val id: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val title: String = "",
    val description: String = "",
    val category: EventCategory = EventCategory.OTHER,
    val venue: String = "",
    val address: String = "",
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val maxParticipants: Int = 0,
    val participantCount: Int = 0,
    val fee: String = "Free",
    val rules: String = "",
    val communityId: String = "",
    val communityName: String = "",
    val imageEmoji: String = "🎪",
    val isRegistered: Boolean = false,
    val isSaved: Boolean = false,
    val status: String = "upcoming",
    val hasTimeline: Boolean = false,
    val dayCount: Int = 1,
    val prize: String = "",
    val participantMessage: String = "",
    val timelineDays: List<EventTimelineDay> = emptyList(),
    val customFields: List<EventCustomField> = emptyList(),
    val tags: List<String> = emptyList(),
    val isFeatured: Boolean = false
) {
    val spotsLeft: Int get() = if (maxParticipants <= 0) Int.MAX_VALUE else (maxParticipants - participantCount).coerceAtLeast(0)
    val isFull: Boolean get() = maxParticipants > 0 && participantCount >= maxParticipants

    val displayVenue: String
        get() = timelineDays.firstOrNull { it.venue.isNotBlank() }?.venue?.takeIf { it.isNotBlank() }
            ?: venue.ifBlank { address }
}

data class DistrictCommunity(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val joinCode: String = "",
    val description: String = "",
    val type: CommunityType = CommunityType.OTHER,
    val location: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val memberCount: Int = 0,
    val emoji: String = "🏘️",
    val isMember: Boolean = false,
    val latestUpdate: String = ""
)

data class CommunityUpdate(
    val id: String = "",
    val communityId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)

data class CommunityMember(
    val userId: String = "",
    val name: String = "",
    val username: String = "",
    val avatar: String = "👤",
    val connectionStatus: ConnectionStatus = ConnectionStatus.NONE
)

data class CreateEventRequest(
    val title: String,
    val description: String,
    val category: EventCategory,
    val venue: String,
    val address: String,
    val startAt: Long,
    val endAt: Long,
    val maxParticipants: Int,
    val fee: String,
    val rules: String,
    val communityId: String = "",
    val imageEmoji: String = "🎪",
    val hasTimeline: Boolean = false,
    val dayCount: Int = 1,
    val prize: String = "",
    val participantMessage: String = "",
    val timelineDays: List<EventTimelineDay> = emptyList(),
    val customFields: List<EventCustomField> = emptyList(),
    val tags: List<String> = emptyList()
)

data class CreateCommunityRequest(
    val name: String,
    val description: String,
    val type: CommunityType,
    val location: String,
    val emoji: String = "🏘️"
)

enum class ActivityType(val label: String) {
    EVENT_CREATED("Event"),
    EVENT_REGISTERED("Going"),
    EVENT_SAVED("Saved"),
    COMMUNITY_UPDATE("Update"),
    COMMUNITY_JOINED("Joined"),
    CONNECTION_REQUEST("Request"),
    CONNECTION_ACCEPTED("Connected");

    companion object {
        fun fromStorage(value: String?): ActivityType = when (value?.lowercase()) {
            "event_created" -> EVENT_CREATED
            "event_registered" -> EVENT_REGISTERED
            "event_saved" -> EVENT_SAVED
            "community_update" -> COMMUNITY_UPDATE
            "community_joined" -> COMMUNITY_JOINED
            "connection_request" -> CONNECTION_REQUEST
            "connection_accepted" -> CONNECTION_ACCEPTED
            else -> COMMUNITY_UPDATE
        }
    }
}

data class DistrictActivity(
    val id: String = "",
    val type: ActivityType = ActivityType.COMMUNITY_UPDATE,
    val title: String = "",
    val subtitle: String = "",
    val emoji: String = "✨",
    val eventId: String = "",
    val communityId: String = "",
    val actorUserId: String = "",
    val timestamp: Long = 0L
)

data class DistrictSummary(
    val eventsHosting: Int = 0,
    val eventsAttending: Int = 0,
    val communitiesJoined: Int = 0,
    val connectionsCount: Int = 0,
    val savedEventsCount: Int = 0,
    val pendingRequestsCount: Int = 0
)

data class DistrictConnection(
    val userId: String = "",
    val name: String = "",
    val username: String = "",
    val avatar: String = "👤",
    val connectedAt: Long = 0L
)

data class ConnectionRequest(
    val requestId: String = "",
    val userId: String = "",
    val name: String = "",
    val username: String = "",
    val avatar: String = "👤",
    val createdAt: Long = 0L
)
