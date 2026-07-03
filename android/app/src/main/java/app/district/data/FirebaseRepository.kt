package app.district.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor() : DistrictRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    override val authState: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser != null) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override fun currentAccount(): AuthAccount? {
        val user = auth.currentUser ?: return null
        return AuthAccount(
            uid = user.uid,
            email = user.email.orEmpty(),
            phoneNumber = user.phoneNumber.orEmpty(),
            displayName = user.displayName.orEmpty()
        )
    }

    override suspend fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim().lowercase(), password).await()
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email.trim().lowercase(), password).await()
    }

    override suspend fun loginWithGoogle(idToken: String) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    override suspend fun loginWithPhoneCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).await()
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun fetchAccountCloudState(): AccountCloudState? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = db.collection("users").document(uid).get().await()
        if (!doc.exists()) return null
        return AccountCloudState(
            snapshotJson = doc.getString("accountSnapshotJson").orEmpty(),
            deletionRequestedAt = doc.getLong("deletionRequestedAt") ?: 0L,
            deletionDeleteAfter = doc.getLong("deletionDeleteAfter") ?: 0L,
            deletionReason = doc.getString("deletionReason").orEmpty(),
            deletionStatus = doc.getString("deletionStatus").orEmpty()
        )
    }

    override suspend fun saveAccountSnapshot(snapshotJson: String) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).set(
            mapOf(
                "uid" to user.uid,
                "email" to user.email.orEmpty(),
                "phoneNumber" to user.phoneNumber.orEmpty(),
                "displayName" to user.displayName.orEmpty(),
                "accountSnapshotJson" to snapshotJson,
                "updatedAt" to com.google.firebase.Timestamp.now()
            ),
            SetOptions.merge()
        ).await()
    }

    override suspend fun requestAccountDeletion(reason: String, deleteAfterMillis: Long) {
        val user = auth.currentUser ?: throw IllegalStateException("Not logged in")
        val now = System.currentTimeMillis()
        db.collection("users").document(user.uid).set(
            mapOf(
                "deletionStatus" to "scheduled",
                "deletionRequestedAt" to now,
                "deletionDeleteAfter" to deleteAfterMillis,
                "deletionReason" to reason.trim(),
                "deletionUpdatedAt" to com.google.firebase.Timestamp.now()
            ),
            SetOptions.merge()
        ).await()
    }

    override suspend fun cancelAccountDeletion() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).set(
            mapOf(
                "deletionStatus" to "active",
                "deletionRequestedAt" to 0L,
                "deletionDeleteAfter" to 0L,
                "deletionReason" to "",
                "deletionUpdatedAt" to com.google.firebase.Timestamp.now()
            ),
            SetOptions.merge()
        ).await()
    }

    override suspend fun claimUsername(uid: String, username: String) {
        val lower = username.lowercase()
        val ref = db.collection("usernames").document(lower)
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            if (snap.exists()) {
                val owner = snap.getString("uid").orEmpty()
                if (owner != uid) throw IllegalStateException("Username already taken")
            } else {
                tx.set(ref, mapOf("uid" to uid, "createdAt" to System.currentTimeMillis()))
            }
        }.await()
        db.collection("profiles").document(uid).set(
            mapOf("username" to lower, "uid" to uid, "authEmail" to authEmailForUsername(lower)),
            SetOptions.merge()
        ).await()
    }

    override suspend fun isUsernameAvailable(username: String): Boolean {
        val lower = PrefsManager.normalizeUsername(username).lowercase()
        if (lower.length < 6) return false
        val snap = db.collection("usernames").document(lower).get().await()
        if (!snap.exists()) return true
        return snap.getString("uid").orEmpty() == auth.currentUser?.uid
    }

    override suspend fun loginWithUsername(username: String, password: String) {
        val lower = PrefsManager.normalizeUsername(username).lowercase()
        if (lower.length < 6) throw IllegalStateException("Invalid username")
        val snap = db.collection("usernames").document(lower).get().await()
        if (!snap.exists()) throw IllegalStateException("Username not found")
        val email = authEmailForUsername(lower)
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun linkEmailPassword(email: String, password: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Not logged in")
        val credential = EmailAuthProvider.getCredential(email.trim().lowercase(), password)
        user.linkWithCredential(credential).await()
    }

    override suspend fun hasCompletedProfile(uid: String): Boolean {
        val doc = db.collection("profiles").document(uid).get().await()
        if (!doc.exists()) return false
        val username = doc.getString("username").orEmpty()
        return username.length >= 6 && doc.getBoolean("signupComplete") == true
    }

    override suspend fun fetchUserProfile(uid: String): UserProfile? {
        val doc = db.collection("profiles").document(uid).get().await()
        if (!doc.exists()) return null
        return UserProfile(
            uid = uid,
            username = doc.getString("username").orEmpty(),
            firstName = doc.getString("firstName").orEmpty(),
            lastName = doc.getString("lastName").orEmpty(),
            dateOfBirthMillis = doc.getLong("dateOfBirthMillis") ?: 0L,
            bio = doc.getString("bio").orEmpty(),
            photoUri = doc.getString("photoUri").orEmpty(),
            usernameChangedAt = doc.getLong("usernameChangedAt") ?: 0L
        )
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        val lower = profile.username.lowercase()
        db.collection("profiles").document(profile.uid).set(
            mapOf(
                "uid" to profile.uid,
                "username" to lower,
                "firstName" to profile.firstName.trim(),
                "lastName" to profile.lastName.trim(),
                "dateOfBirthMillis" to profile.dateOfBirthMillis,
                "bio" to profile.bio.trim(),
                "photoUri" to profile.photoUri,
                "usernameChangedAt" to profile.usernameChangedAt,
                "authEmail" to authEmailForUsername(lower),
                "signupComplete" to true,
                "updatedAt" to com.google.firebase.Timestamp.now()
            ),
            SetOptions.merge()
        ).await()
    }

    override suspend fun updateUsername(uid: String, newUsername: String) {
        val lower = PrefsManager.normalizeUsername(newUsername).lowercase()
        if (lower.length < 6) throw IllegalStateException("Username must be at least 6 characters")
        val profileRef = db.collection("profiles").document(uid)
        val current = profileRef.get().await()
        val lastChange = current.getLong("usernameChangedAt") ?: 0L
        val cooldownMs = 15L * 24 * 60 * 60 * 1000
        if (lastChange > 0 && System.currentTimeMillis() - lastChange < cooldownMs) {
            throw IllegalStateException("You can change your username again in 15 days")
        }
        val oldUsername = current.getString("username").orEmpty()
        claimUsername(uid, lower)
        if (oldUsername.isNotBlank() && oldUsername != lower) {
            db.collection("usernames").document(oldUsername).delete().await()
        }
        profileRef.set(
            mapOf("usernameChangedAt" to System.currentTimeMillis()),
            SetOptions.merge()
        ).await()
    }

    private fun authEmailForUsername(username: String): String =
        "${username.lowercase()}@users.rise.app"

    // ---- Events ----

    override suspend fun listEvents(filter: EventListFilter): List<DistrictEvent> =
        callList(
            "listEvents",
            mapOf(
                "category" to (filter.category?.name ?: "ALL"),
                "tag" to (filter.tag ?: ""),
                "search" to filter.search.trim(),
                "timelineOnly" to filter.timelineOnly
            )
        ) { parseEvent(it) }

    override suspend fun listMyOrganizedEvents(): List<DistrictEvent> =
        callList("listMyOrganizedEvents", emptyMap()) { parseEvent(it) }

    override suspend fun listMyRegisteredEvents(): List<DistrictEvent> =
        callList("listMyRegisteredEvents", emptyMap()) { parseEvent(it) }

    override suspend fun getEvent(eventId: String): DistrictEvent? {
        val result = functions.getHttpsCallable("getEvent").call(mapOf("eventId" to eventId)).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as? Map<String, Any?> ?: return null
        @Suppress("UNCHECKED_CAST")
        val eventMap = data["event"] as? Map<String, Any?> ?: return null
        return parseEvent(eventMap)
    }

    override suspend fun createEvent(request: CreateEventRequest): DistrictEvent {
        val result = functions.getHttpsCallable("createEvent").call(
            mapOf(
                "title" to request.title,
                "description" to request.description,
                "category" to request.category.name,
                "venue" to request.venue,
                "address" to request.address,
                "startAt" to request.startAt,
                "endAt" to request.endAt,
                "maxParticipants" to request.maxParticipants,
                "fee" to request.fee,
                "rules" to request.rules,
                "communityId" to request.communityId,
                "imageEmoji" to request.imageEmoji,
                "hasTimeline" to request.hasTimeline,
                "dayCount" to request.dayCount,
                "prize" to request.prize,
                "participantMessage" to request.participantMessage,
                "timelineDays" to request.timelineDays.map { day ->
                    mapOf(
                        "dayNumber" to day.dayNumber,
                        "title" to day.title,
                        "venue" to day.venue,
                        "segments" to day.segments.map { seg ->
                            mapOf(
                                "label" to seg.label,
                                "startTime" to seg.startTime,
                                "endTime" to seg.endTime
                            )
                        },
                        "extraFields" to day.extraFields.map { field ->
                            mapOf("label" to field.label, "value" to field.value)
                        }
                    )
                },
                "customFields" to request.customFields.map { mapOf("label" to it.label, "value" to it.value) },
                "tags" to request.tags
            )
        ).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        return parseEvent(data["event"] as Map<String, Any?>)
    }

    override suspend fun registerForEvent(eventId: String): DistrictEvent {
        val result = functions.getHttpsCallable("registerForEvent").call(mapOf("eventId" to eventId)).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        return parseEvent(data["event"] as Map<String, Any?>)
    }

    override suspend fun cancelEventRegistration(eventId: String) {
        functions.getHttpsCallable("cancelEventRegistration").call(mapOf("eventId" to eventId)).await()
    }

    // ---- Communities ----

    override suspend fun listCommunities(): List<DistrictCommunity> =
        callList("listCommunities", emptyMap()) { parseCommunity(it) }

    override suspend fun listMyCommunities(): List<DistrictCommunity> =
        callList("listMyCommunities", emptyMap()) { parseCommunity(it) }

    override suspend fun getCommunity(communityId: String): DistrictCommunity? {
        val result = functions.getHttpsCallable("getCommunity").call(mapOf("communityId" to communityId)).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as? Map<String, Any?> ?: return null
        @Suppress("UNCHECKED_CAST")
        val map = data["community"] as? Map<String, Any?> ?: return null
        return parseCommunity(map)
    }

    override suspend fun createCommunity(request: CreateCommunityRequest): DistrictCommunity {
        val result = functions.getHttpsCallable("createCommunity").call(
            mapOf(
                "name" to request.name,
                "description" to request.description,
                "type" to request.type.name,
                "location" to request.location,
                "emoji" to request.emoji
            )
        ).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        return parseCommunity(data["community"] as Map<String, Any?>)
    }

    override suspend fun joinCommunity(communityId: String): DistrictCommunity {
        val result = functions.getHttpsCallable("joinCommunity").call(mapOf("communityId" to communityId)).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        return parseCommunity(data["community"] as Map<String, Any?>)
    }

    override suspend fun joinCommunityByCode(joinCode: String): DistrictCommunity {
        val result = functions.getHttpsCallable("joinCommunityByCode").call(mapOf("joinCode" to joinCode.trim())).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        return parseCommunity(data["community"] as Map<String, Any?>)
    }

    override suspend fun joinCommunityByName(name: String): DistrictCommunity {
        val result = functions.getHttpsCallable("joinCommunityByName").call(mapOf("name" to name.trim())).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        return parseCommunity(data["community"] as Map<String, Any?>)
    }

    override suspend fun leaveCommunity(communityId: String) {
        functions.getHttpsCallable("leaveCommunity").call(mapOf("communityId" to communityId)).await()
    }

    override suspend fun listCommunityUpdates(communityId: String): List<CommunityUpdate> =
        callList("listCommunityUpdates", mapOf("communityId" to communityId)) { parseUpdate(it) }

    override suspend fun postCommunityUpdate(communityId: String, message: String): CommunityUpdate {
        val result = functions.getHttpsCallable("postCommunityUpdate").call(
            mapOf("communityId" to communityId, "message" to message)
        ).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        return parseUpdate(data["update"] as Map<String, Any?>)
    }

    override suspend fun listCommunityMembers(communityId: String): List<CommunityMember> =
        callList("listCommunityMembers", mapOf("communityId" to communityId)) { parseMember(it) }

    override suspend fun sendConnectionRequest(targetUserId: String) {
        functions.getHttpsCallable("sendConnectionRequest").call(mapOf("targetUserId" to targetUserId)).await()
    }

    override suspend fun listActivityFeed(limit: Int): List<DistrictActivity> =
        callList("listActivityFeed", mapOf("limit" to limit)) { parseActivity(it) }

    override suspend fun getDistrictSummary(): DistrictSummary {
        val result = functions.getHttpsCallable("getDistrictSummary").call(emptyMap<String, Any>()).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as? Map<String, Any?> ?: return DistrictSummary()
        @Suppress("UNCHECKED_CAST")
        val map = data["summary"] as? Map<String, Any?> ?: return DistrictSummary()
        return parseSummary(map)
    }

    override suspend fun toggleEventSave(eventId: String): DistrictEvent {
        val result = functions.getHttpsCallable("toggleEventSave").call(mapOf("eventId" to eventId)).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        return parseEvent(data["event"] as Map<String, Any?>)
    }

    override suspend fun listSavedEvents(): List<DistrictEvent> =
        callList("listSavedEvents", emptyMap()) { parseEvent(it) }

    override suspend fun listMyConnections(): List<DistrictConnection> =
        callList("listMyConnections", emptyMap()) { parseConnection(it) }

    override suspend fun listPendingConnectionRequests(): List<ConnectionRequest> =
        callList("listPendingConnectionRequests", emptyMap()) { parseConnectionRequest(it) }

    override suspend fun respondConnectionRequest(requestId: String, accept: Boolean) {
        functions.getHttpsCallable("respondConnectionRequest").call(
            mapOf("requestId" to requestId, "accept" to accept)
        ).await()
    }

    private suspend fun <T> callList(
        name: String,
        params: Map<String, Any>,
        parser: (Map<String, Any?>) -> T
    ): List<T> {
        val result = functions.getHttpsCallable(name).call(HashMap(params)).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as? Map<String, Any?> ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val items = data["items"] as? List<Map<String, Any?>> ?: return emptyList()
        return items.map(parser)
    }

    private fun parseEvent(map: Map<String, Any?>): DistrictEvent = DistrictEvent(
        id = map["id"] as? String ?: "",
        organizerId = map["organizerId"] as? String ?: "",
        organizerName = map["organizerName"] as? String ?: "",
        title = map["title"] as? String ?: "",
        description = map["description"] as? String ?: "",
        category = EventCategory.fromStorage(map["category"] as? String),
        venue = map["venue"] as? String ?: "",
        address = map["address"] as? String ?: "",
        startAt = (map["startAt"] as? Number)?.toLong() ?: 0L,
        endAt = (map["endAt"] as? Number)?.toLong() ?: 0L,
        maxParticipants = (map["maxParticipants"] as? Number)?.toInt() ?: 0,
        participantCount = (map["participantCount"] as? Number)?.toInt() ?: 0,
        fee = map["fee"] as? String ?: "Free",
        rules = map["rules"] as? String ?: "",
        communityId = map["communityId"] as? String ?: "",
        communityName = map["communityName"] as? String ?: "",
        imageEmoji = map["imageEmoji"] as? String ?: "🎪",
        isRegistered = map["isRegistered"] as? Boolean ?: false,
        isSaved = map["isSaved"] as? Boolean ?: false,
        status = map["status"] as? String ?: "upcoming",
        hasTimeline = map["hasTimeline"] as? Boolean ?: false,
        dayCount = (map["dayCount"] as? Number)?.toInt() ?: 1,
        prize = map["prize"] as? String ?: "",
        participantMessage = map["participantMessage"] as? String ?: "",
        timelineDays = parseTimelineDays(map["timelineDays"]),
        customFields = parseCustomFields(map["customFields"]),
        tags = parseTags(map["tags"])
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseTags(raw: Any?): List<String> {
        val list = raw as? List<*> ?: return emptyList()
        return list.mapNotNull { it as? String }.filter { it.isNotBlank() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseTimelineDays(raw: Any?): List<EventTimelineDay> {
        val list = raw as? List<Map<String, Any?>> ?: return emptyList()
        return list.map { day ->
            val segmentsRaw = day["segments"] as? List<Map<String, Any?>> ?: emptyList()
            val extraRaw = day["extraFields"] as? List<Map<String, Any?>> ?: emptyList()
            EventTimelineDay(
                dayNumber = (day["dayNumber"] as? Number)?.toInt() ?: 1,
                title = day["title"] as? String ?: "",
                venue = day["venue"] as? String ?: "",
                segments = segmentsRaw.map { seg ->
                    EventTimelineSegment(
                        label = seg["label"] as? String ?: "",
                        startTime = seg["startTime"] as? String ?: "",
                        endTime = seg["endTime"] as? String ?: ""
                    )
                },
                extraFields = extraRaw.map { field ->
                    EventCustomField(
                        label = field["label"] as? String ?: "",
                        value = field["value"] as? String ?: ""
                    )
                }
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseCustomFields(raw: Any?): List<EventCustomField> {
        val list = raw as? List<Map<String, Any?>> ?: return emptyList()
        return list.map { field ->
            EventCustomField(
                label = field["label"] as? String ?: "",
                value = field["value"] as? String ?: ""
            )
        }
    }

    private fun parseCommunity(map: Map<String, Any?>): DistrictCommunity = DistrictCommunity(
        id = map["id"] as? String ?: "",
        name = map["name"] as? String ?: "",
        slug = map["slug"] as? String ?: "",
        joinCode = map["joinCode"] as? String ?: "",
        description = map["description"] as? String ?: "",
        type = CommunityType.fromStorage(map["type"] as? String),
        location = map["location"] as? String ?: "",
        adminId = map["adminId"] as? String ?: "",
        adminName = map["adminName"] as? String ?: "",
        memberCount = (map["memberCount"] as? Number)?.toInt() ?: 0,
        emoji = map["emoji"] as? String ?: "🏘️",
        isMember = map["isMember"] as? Boolean ?: false,
        latestUpdate = map["latestUpdate"] as? String ?: ""
    )

    private fun parseUpdate(map: Map<String, Any?>): CommunityUpdate = CommunityUpdate(
        id = map["id"] as? String ?: "",
        communityId = map["communityId"] as? String ?: "",
        authorId = map["authorId"] as? String ?: "",
        authorName = map["authorName"] as? String ?: "",
        message = map["message"] as? String ?: "",
        timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
    )

    private fun parseMember(map: Map<String, Any?>): CommunityMember = CommunityMember(
        userId = map["userId"] as? String ?: "",
        name = map["name"] as? String ?: "",
        username = map["username"] as? String ?: "",
        avatar = map["avatar"] as? String ?: "👤",
        connectionStatus = when (map["connectionStatus"] as? String) {
            "pending" -> ConnectionStatus.PENDING
            "connected" -> ConnectionStatus.CONNECTED
            else -> ConnectionStatus.NONE
        }
    )

    private fun parseActivity(map: Map<String, Any?>): DistrictActivity = DistrictActivity(
        id = map["id"] as? String ?: "",
        type = ActivityType.fromStorage(map["type"] as? String),
        title = map["title"] as? String ?: "",
        subtitle = map["subtitle"] as? String ?: "",
        emoji = map["emoji"] as? String ?: "✨",
        eventId = map["eventId"] as? String ?: "",
        communityId = map["communityId"] as? String ?: "",
        actorUserId = map["actorUserId"] as? String ?: "",
        timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
    )

    private fun parseSummary(map: Map<String, Any?>): DistrictSummary = DistrictSummary(
        eventsHosting = (map["eventsHosting"] as? Number)?.toInt() ?: 0,
        eventsAttending = (map["eventsAttending"] as? Number)?.toInt() ?: 0,
        communitiesJoined = (map["communitiesJoined"] as? Number)?.toInt() ?: 0,
        connectionsCount = (map["connectionsCount"] as? Number)?.toInt() ?: 0,
        savedEventsCount = (map["savedEventsCount"] as? Number)?.toInt() ?: 0,
        pendingRequestsCount = (map["pendingRequestsCount"] as? Number)?.toInt() ?: 0
    )

    private fun parseConnection(map: Map<String, Any?>): DistrictConnection = DistrictConnection(
        userId = map["userId"] as? String ?: "",
        name = map["name"] as? String ?: "",
        username = map["username"] as? String ?: "",
        avatar = map["avatar"] as? String ?: "👤",
        connectedAt = (map["connectedAt"] as? Number)?.toLong() ?: 0L
    )

    private fun parseConnectionRequest(map: Map<String, Any?>): ConnectionRequest = ConnectionRequest(
        requestId = map["requestId"] as? String ?: "",
        userId = map["userId"] as? String ?: "",
        name = map["name"] as? String ?: "",
        username = map["username"] as? String ?: "",
        avatar = map["avatar"] as? String ?: "👤",
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
    )
}
