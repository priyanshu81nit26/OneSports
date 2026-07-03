package app.district.data

import java.util.Calendar

object EventFilterUtils {
    fun filter(events: List<DistrictEvent>, filter: EventListFilter): List<DistrictEvent> {
        val search = filter.search.trim().lowercase()
        return events.filter { event ->
            (filter.category == null || event.category == filter.category) &&
                (filter.tag.isNullOrBlank() || event.tags.any { it.equals(filter.tag, ignoreCase = true) }) &&
                (!filter.timelineOnly || event.hasTimeline) &&
                (search.isBlank() ||
                    event.title.lowercase().contains(search) ||
                    event.description.lowercase().contains(search) ||
                    event.displayVenue.lowercase().contains(search) ||
                    event.category.label.lowercase().contains(search)) &&
                matchesDate(event, filter.dateFilter)
        }
    }

    fun featured(events: List<DistrictEvent>): List<DistrictEvent> =
        events.filter { it.isFeatured }.ifEmpty { events.take(5) }

    private fun matchesDate(event: DistrictEvent, dateFilter: EventDateFilter): Boolean {
        if (dateFilter == EventDateFilter.ALL || event.startAt <= 0L) return true
        val start = Calendar.getInstance().apply { timeInMillis = event.startAt }
        val today = Calendar.getInstance()
        val eventDay = dayKey(start)
        val todayKey = dayKey(today)
        return when (dateFilter) {
            EventDateFilter.TODAY -> eventDay == todayKey
            EventDateFilter.TOMORROW -> {
                val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                eventDay == dayKey(tomorrow)
            }
            EventDateFilter.ALL -> true
        }
    }

    private fun dayKey(cal: Calendar): Int =
        cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
}
