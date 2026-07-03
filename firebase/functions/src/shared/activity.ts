import * as admin from "firebase-admin";

const db = admin.firestore();

export type ActivityType =
  | "event_created"
  | "event_registered"
  | "event_saved"
  | "community_update"
  | "community_joined"
  | "connection_request"
  | "connection_accepted";

export interface ActivityPayload {
  userId: string;
  type: ActivityType;
  title: string;
  subtitle: string;
  emoji: string;
  eventId?: string;
  communityId?: string;
  actorUserId?: string;
}

/** Writes one activity item to a user's personal feed (fan-out target). */
export async function pushActivity(payload: ActivityPayload): Promise<void> {
  await db.collection("activity_feed").add({
    userId: payload.userId,
    type: payload.type,
    title: payload.title,
    subtitle: payload.subtitle,
    emoji: payload.emoji,
    eventId: payload.eventId || "",
    communityId: payload.communityId || "",
    actorUserId: payload.actorUserId || "",
    timestamp: Date.now(),
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}

/** Fan-out the same activity to all members of a community (capped for scale). */
export async function fanOutToCommunity(
  communityId: string,
  excludeUserId: string,
  payload: Omit<ActivityPayload, "userId">
): Promise<void> {
  const members = await db.collection("community_members")
    .where("communityId", "==", communityId)
    .limit(100)
    .get();
  const batch = db.batch();
  let count = 0;
  for (const doc of members.docs) {
    const userId = doc.get("userId") as string;
    if (!userId || userId === excludeUserId) continue;
    const ref = db.collection("activity_feed").doc();
    batch.set(ref, {
      userId,
      type: payload.type,
      title: payload.title,
      subtitle: payload.subtitle,
      emoji: payload.emoji,
      eventId: payload.eventId || "",
      communityId: payload.communityId || communityId,
      actorUserId: payload.actorUserId || "",
      timestamp: Date.now(),
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    count++;
    if (count >= 400) break;
  }
  if (count > 0) await batch.commit();
}
