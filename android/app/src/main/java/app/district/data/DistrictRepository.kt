package app.district.data

import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.flow.Flow

data class AuthAccount(
    val uid: String,
    val email: String,
    val phoneNumber: String = "",
    val displayName: String = ""
)

data class UserProfile(
    val uid: String,
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirthMillis: Long = 0L,
    val bio: String = "",
    val photoUri: String = "",
    val usernameChangedAt: Long = 0L
) {
    val displayName: String get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}

data class AccountCloudState(
    val snapshotJson: String = "",
    val deletionRequestedAt: Long = 0L,
    val deletionDeleteAfter: Long = 0L,
    val deletionReason: String = "",
    val deletionStatus: String = ""
) {
    val deletionPending: Boolean get() = deletionStatus == "scheduled" && deletionDeleteAfter > 0L
}

interface DistrictRepository {
    val authState: Flow<Boolean>
    fun isLoggedIn(): Boolean
    fun currentAccount(): AuthAccount?

    suspend fun loginWithEmail(email: String, password: String)
    suspend fun signUpWithEmail(email: String, password: String, name: String = "")
    suspend fun loginWithGoogle(idToken: String)
    suspend fun loginWithPhoneCredential(credential: PhoneAuthCredential)
    suspend fun logout()

    suspend fun fetchAccountCloudState(): AccountCloudState?
    suspend fun saveAccountSnapshot(snapshotJson: String)
    suspend fun requestAccountDeletion(reason: String, deleteAfterMillis: Long)
    suspend fun cancelAccountDeletion()
    suspend fun claimUsername(uid: String, username: String)
    suspend fun isUsernameAvailable(username: String): Boolean
    suspend fun loginWithUsername(username: String, password: String)
    suspend fun linkEmailPassword(email: String, password: String)
    suspend fun hasCompletedProfile(uid: String): Boolean
    suspend fun fetchUserProfile(uid: String): UserProfile?
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun updateUsername(uid: String, newUsername: String)

    // ---- Events ----
    suspend fun listEvents(filter: EventListFilter = EventListFilter()): List<DistrictEvent>
    suspend fun listMyOrganizedEvents(): List<DistrictEvent>
    suspend fun listMyRegisteredEvents(): List<DistrictEvent>
    suspend fun getEvent(eventId: String): DistrictEvent?
    suspend fun createEvent(request: CreateEventRequest): DistrictEvent
    suspend fun registerForEvent(eventId: String): DistrictEvent
    suspend fun cancelEventRegistration(eventId: String)

    // ---- Communities ----
    suspend fun listCommunities(): List<DistrictCommunity>
    suspend fun listMyCommunities(): List<DistrictCommunity>
    suspend fun getCommunity(communityId: String): DistrictCommunity?
    suspend fun createCommunity(request: CreateCommunityRequest): DistrictCommunity
    suspend fun joinCommunity(communityId: String): DistrictCommunity
    suspend fun joinCommunityByCode(joinCode: String): DistrictCommunity
    suspend fun joinCommunityByName(name: String): DistrictCommunity
    suspend fun leaveCommunity(communityId: String)
    suspend fun listCommunityUpdates(communityId: String): List<CommunityUpdate>
    suspend fun postCommunityUpdate(communityId: String, message: String): CommunityUpdate
    suspend fun listCommunityMembers(communityId: String): List<CommunityMember>
    suspend fun sendConnectionRequest(targetUserId: String)

    // ---- Pulse / cross-feature ----
    suspend fun listActivityFeed(limit: Int = 40): List<DistrictActivity>
    suspend fun getDistrictSummary(): DistrictSummary
    suspend fun toggleEventSave(eventId: String): DistrictEvent
    suspend fun listSavedEvents(): List<DistrictEvent>
    suspend fun listMyConnections(): List<DistrictConnection>
    suspend fun listPendingConnectionRequests(): List<ConnectionRequest>
    suspend fun respondConnectionRequest(requestId: String, accept: Boolean)
}
