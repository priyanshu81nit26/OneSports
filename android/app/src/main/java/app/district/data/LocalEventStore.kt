package app.district.data

/** Session-local booking/save state for demo events (works without Cloud Functions). */
object LocalEventStore {
    private val registeredIds = mutableSetOf<String>()
    private val savedIds = mutableSetOf<String>()

    fun apply(event: DistrictEvent): DistrictEvent {
        val registered = registeredIds.contains(event.id)
        val saved = savedIds.contains(event.id)
        val count = event.participantCount + if (registered && !event.isRegistered) 1 else 0
        return event.copy(
            isRegistered = event.isRegistered || registered,
            isSaved = event.isSaved || saved,
            participantCount = count.coerceAtMost(if (event.maxParticipants > 0) event.maxParticipants else count)
        )
    }

    fun applyAll(events: List<DistrictEvent>): List<DistrictEvent> = events.map { apply(it) }

    fun register(eventId: String) {
        registeredIds += eventId
    }

    fun unregister(eventId: String) {
        registeredIds -= eventId
    }

    fun toggleSave(eventId: String): Boolean {
        return if (savedIds.contains(eventId)) {
            savedIds -= eventId
            false
        } else {
            savedIds += eventId
            true
        }
    }

    fun isRegistered(eventId: String) = registeredIds.contains(eventId)
    fun isSaved(eventId: String) = savedIds.contains(eventId)
}
