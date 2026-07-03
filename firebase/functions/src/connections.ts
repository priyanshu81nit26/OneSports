import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";
import { pushActivity } from "./shared/activity";

const db = admin.firestore();

function requireAuth(context: functions.https.CallableContext): string {
  if (!context.auth?.uid) {
    throw new functions.https.HttpsError("unauthenticated", "Sign in required");
  }
  return context.auth.uid;
}

async function profileSummary(userId: string) {
  const profile = await db.collection("profiles").doc(userId).get();
  const user = await db.collection("users").doc(userId).get();
  return {
    userId,
    name: (profile.get("name") as string) || (user.get("displayName") as string) || "Member",
    username: (profile.get("username") as string) || "",
    avatar: (profile.get("avatar") as string) || "👤",
  };
}

export const listMyConnections = functions.https.onCall(async (_data, context) => {
  const uid = requireAuth(context);
  const [fromSnap, toSnap] = await Promise.all([
    db.collection("connections").where("fromUserId", "==", uid).limit(50).get(),
    db.collection("connections").where("toUserId", "==", uid).limit(50).get(),
  ]);
  const seen = new Set<string>();
  const items: Record<string, unknown>[] = [];
  for (const doc of [...fromSnap.docs, ...toSnap.docs]) {
    const otherId = doc.get("fromUserId") === uid
      ? doc.get("toUserId") as string
      : doc.get("fromUserId") as string;
    if (!otherId || seen.has(otherId)) continue;
    seen.add(otherId);
    const profile = await profileSummary(otherId);
    items.push({
      ...profile,
      connectedAt: doc.get("createdAt") ? Date.now() : 0,
    });
  }
  return { items };
});

export const listPendingConnectionRequests = functions.https.onCall(async (_data, context) => {
  const uid = requireAuth(context);
  const snap = await db.collection("connection_requests")
    .where("toUserId", "==", uid)
    .where("status", "==", "pending")
    .limit(30)
    .get();
  const items: Record<string, unknown>[] = [];
  for (const doc of snap.docs) {
    const fromUserId = doc.get("fromUserId") as string;
    const profile = await profileSummary(fromUserId);
    items.push({
      requestId: doc.id,
      ...profile,
      createdAt: doc.get("createdAt") ? Date.now() : 0,
    });
  }
  return { items };
});

export const respondConnectionRequest = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const requestId = String(data.requestId || "");
  const accept = Boolean(data.accept);
  const reqRef = db.collection("connection_requests").doc(requestId);
  const req = await reqRef.get();
  if (!req.exists) {
    throw new functions.https.HttpsError("not-found", "Request not found");
  }
  if (req.get("toUserId") !== uid) {
    throw new functions.https.HttpsError("permission-denied", "Not your request");
  }
  const fromUserId = req.get("fromUserId") as string;
  if (accept) {
    await db.collection("connections").doc(`${fromUserId}_${uid}`).set({
      fromUserId,
      toUserId: uid,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    const fromName = (await profileSummary(fromUserId)).name;
    const toName = (await profileSummary(uid)).name;
    await pushActivity({
      userId: fromUserId,
      type: "connection_accepted",
      title: "Connection accepted",
      subtitle: `${toName} accepted your request`,
      emoji: "🤝",
      actorUserId: uid,
    });
    await pushActivity({
      userId: uid,
      type: "connection_accepted",
      title: "You're connected",
      subtitle: `You and ${fromName} are now connected`,
      emoji: "🤝",
      actorUserId: fromUserId,
    });
  }
  await reqRef.update({
    status: accept ? "accepted" : "declined",
    respondedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  return { ok: true, accepted: accept };
});
