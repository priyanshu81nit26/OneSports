import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

const db = admin.firestore();

function requireAuth(context: functions.https.CallableContext): string {
  if (!context.auth?.uid) {
    throw new functions.https.HttpsError("unauthenticated", "Sign in required");
  }
  return context.auth.uid;
}

export const listActivityFeed = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const limit = Math.min(Math.max(Number(data.limit) || 40, 1), 60);
  const snap = await db.collection("activity_feed")
    .where("userId", "==", uid)
    .orderBy("timestamp", "desc")
    .limit(limit)
    .get();
  const items = snap.docs.map((doc) => ({
    id: doc.id,
    type: doc.get("type") || "",
    title: doc.get("title") || "",
    subtitle: doc.get("subtitle") || "",
    emoji: doc.get("emoji") || "✨",
    eventId: doc.get("eventId") || "",
    communityId: doc.get("communityId") || "",
    actorUserId: doc.get("actorUserId") || "",
    timestamp: doc.get("timestamp") || 0,
  }));
  return { items };
});

export const getDistrictSummary = functions.https.onCall(async (_data, context) => {
  const uid = requireAuth(context);
  const [
    hosting,
    registrations,
    communities,
    connectionsFrom,
    connectionsTo,
    saves,
    pendingIn,
  ] = await Promise.all([
    db.collection("events").where("organizerId", "==", uid).count().get(),
    db.collection("event_registrations").where("userId", "==", uid).count().get(),
    db.collection("community_members").where("userId", "==", uid).count().get(),
    db.collection("connections").where("fromUserId", "==", uid).count().get(),
    db.collection("connections").where("toUserId", "==", uid).count().get(),
    db.collection("event_saves").where("userId", "==", uid).count().get(),
    db.collection("connection_requests").where("toUserId", "==", uid).where("status", "==", "pending").count().get(),
  ]);
  return {
    summary: {
      eventsHosting: hosting.data().count,
      eventsAttending: registrations.data().count,
      communitiesJoined: communities.data().count,
      connectionsCount: connectionsFrom.data().count + connectionsTo.data().count,
      savedEventsCount: saves.data().count,
      pendingRequestsCount: pendingIn.data().count,
    },
  };
});
