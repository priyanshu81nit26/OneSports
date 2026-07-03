package app.district.data

import java.util.Calendar

/** Local demo catalogue when Cloud Functions are unavailable — District-style sports discovery. */
object DemoSportsData {
    private fun daysFromNow(days: Int, hour: Int = 18, minute: Int = 0): Long =
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, days)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    val sampleEvents: List<DistrictEvent> = listOf(
        DistrictEvent(
            id = "demo-football-1",
            organizerName = "Delhi Sports Club",
            title = "Sunday 5-a-side Football League",
            description = "Weekly friendly league. Teams of 5, subs allowed. Boots required.",
            category = EventCategory.FOOTBALL,
            venue = "Play Arena Turf, Saket",
            address = "Saket, New Delhi",
            startAt = daysFromNow(0, 17),
            endAt = daysFromNow(0, 20),
            maxParticipants = 40,
            participantCount = 28,
            fee = "₹350",
            imageEmoji = "⚽",
            isFeatured = true,
            tags = listOf("Outdoor", "Team event", "Competitive")
        ),
        DistrictEvent(
            id = "demo-cricket-1",
            organizerName = "Gully Cricket Collective",
            title = "Box Cricket Night — Sector 7",
            description = "8-over matches, mixed skill levels welcome.",
            category = EventCategory.CRICKET,
            venue = "Sector 7 Social Box Cricket",
            address = "Chandigarh",
            startAt = daysFromNow(1, 19),
            endAt = daysFromNow(1, 23),
            maxParticipants = 24,
            participantCount = 16,
            fee = "₹200",
            imageEmoji = "🏏",
            isFeatured = true,
            tags = listOf("Beginner friendly", "Team event")
        ),
        DistrictEvent(
            id = "demo-basketball-1",
            organizerName = "Hoops India",
            title = "3v3 Street Basketball Open",
            description = "Fast-paced half-court games. Register as a team or solo.",
            category = EventCategory.BASKETBALL,
            venue = "Indoor Sports Complex",
            address = "Gurugram",
            startAt = daysFromNow(2, 16),
            maxParticipants = 36,
            participantCount = 12,
            fee = "₹150",
            imageEmoji = "🏀",
            isFeatured = true,
            tags = listOf("Indoor", "Competitive")
        ),
        DistrictEvent(
            id = "demo-badminton-1",
            organizerName = "Smash Club",
            title = "Badminton Doubles Social",
            description = "Rotating partners every 2 games. Shuttles provided.",
            category = EventCategory.BADMINTON,
            venue = "Shuttle House Courts",
            address = "Noida",
            startAt = daysFromNow(0, 10),
            maxParticipants = 20,
            participantCount = 14,
            fee = "Free",
            imageEmoji = "🏸",
            tags = listOf("Indoor", "Beginner friendly")
        ),
        DistrictEvent(
            id = "demo-tennis-1",
            organizerName = "Ace Tennis Academy",
            title = "Weekend Tennis Round Robin",
            description = "Singles draw for intermediate players (NTRP 3.0+).",
            category = EventCategory.TENNIS,
            venue = "DLTA Courts",
            address = "New Delhi",
            startAt = daysFromNow(3, 7),
            maxParticipants = 16,
            participantCount = 9,
            fee = "₹500",
            imageEmoji = "🎾",
            tags = listOf("Outdoor", "Competitive", "Solo")
        ),
        DistrictEvent(
            id = "demo-marathon-1",
            organizerName = "Run Delhi",
            title = "Monsoon 10K Community Run",
            description = "Chip-timed 10K with hydration stations. Medals for all finishers.",
            category = EventCategory.MARATHON,
            venue = "Lodhi Gardens Loop",
            address = "New Delhi",
            startAt = daysFromNow(7, 6),
            endAt = daysFromNow(7, 10),
            maxParticipants = 500,
            participantCount = 312,
            fee = "₹799",
            imageEmoji = "🏃",
            isFeatured = true,
            hasTimeline = true,
            dayCount = 2,
            tags = listOf("Outdoor", "Multi-day", "Charity"),
            timelineDays = listOf(
                EventTimelineDay(1, "Race day briefing", "Lodhi Gardens", listOf(EventTimelineSegment("Briefing", "05:30", "06:00"), EventTimelineSegment("10K start", "06:30", ""))),
                EventTimelineDay(2, "Recovery yoga", "Run Delhi Studio", listOf(EventTimelineSegment("Stretch session", "08:00", "09:00")))
            )
        ),
        DistrictEvent(
            id = "demo-fitness-1",
            organizerName = "CrossFit Hub",
            title = "Functional Fitness Throwdown",
            description = "Teams of 2. Workouts scaled for all levels.",
            category = EventCategory.FITNESS,
            venue = "CrossFit Hub Sector 29",
            address = "Gurugram",
            startAt = daysFromNow(4, 9),
            maxParticipants = 30,
            participantCount = 22,
            fee = "₹400",
            imageEmoji = "🏋️",
            tags = listOf("Indoor", "Team event")
        ),
        DistrictEvent(
            id = "demo-cycling-1",
            organizerName = "Pedal Warriors",
            title = "Sunrise Cycling Trail — Aravalli",
            description = "45 km guided ride. Helmet mandatory.",
            category = EventCategory.CYCLING,
            venue = "Aravalli Biodiversity Park",
            address = "Gurugram",
            startAt = daysFromNow(1, 5, 30),
            maxParticipants = 50,
            participantCount = 33,
            fee = "Free",
            imageEmoji = "🚴",
            tags = listOf("Outdoor", "Beginner friendly")
        ),
        DistrictEvent(
            id = "demo-swim-1",
            organizerName = "Aqua Sprint",
            title = "50m Freestyle Time Trial",
            description = "Pool lanes assigned by seed time. Timing chips provided.",
            category = EventCategory.SWIMMING,
            venue = "Talkatora Stadium Pool",
            address = "New Delhi",
            startAt = daysFromNow(5, 8),
            maxParticipants = 40,
            participantCount = 18,
            fee = "₹250",
            imageEmoji = "🏊",
            tags = listOf("Indoor", "Competitive", "Solo")
        ),
        DistrictEvent(
            id = "demo-multisport-1",
            organizerName = "Rise Sports Fest",
            title = "Sports Carnival — Family Day",
            description = "Football drills, cricket nets, fitness zones, and kids activities.",
            category = EventCategory.SPORTS,
            venue = "JLN Stadium Grounds",
            address = "New Delhi",
            startAt = daysFromNow(6, 11),
            maxParticipants = 200,
            participantCount = 87,
            fee = "₹99",
            imageEmoji = "🏅",
            isFeatured = true,
            tags = listOf("Family", "Outdoor", "Free entry")
        )
    )

    fun findEvent(id: String): DistrictEvent? = sampleEvents.firstOrNull { it.id == id }
}
