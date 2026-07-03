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

function eventToMap(id: string, data: FirebaseFirestore.DocumentData, isRegistered = false, isSaved = false) {
  return {
    id,
    organizerId: data.organizerId || "",
    organizerName: data.organizerName || "",
    title: data.title || "",
    description: data.description || "",
    category: data.category || "OTHER",
    venue: data.venue || "",
    address: data.address || "",
    startAt: data.startAt || 0,
    endAt: data.endAt || 0,
    maxParticipants: data.maxParticipants || 0,
    participantCount: data.participantCount || 0,
    fee: data.fee || "Free",
    rules: data.rules || "",
    communityId: data.communityId || "",
    communityName: data.communityName || "",
    imageEmoji: data.imageEmoji || "🎪",
    isRegistered,
    isSaved,
    status: data.status || "upcoming",
  };
}

export const toggleEventSave = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const eventId = String(data.eventId || "");
  const eventDoc = await db.collection("events").doc(eventId).get();
  if (!eventDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Event not found");
  }
  const saveRef = db.collection("event_saves").doc(`${uid}_${eventId}`);
  const existing = await saveRef.get();
  let isSaved: boolean;
  if (existing.exists) {
    await saveRef.delete();
    isSaved = false;
  } else {
    await saveRef.set({
      userId: uid,
      eventId,
      savedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    isSaved = true;
    const title = (eventDoc.get("title") as string) || "Event";
    await pushActivity({
      userId: uid,
      type: "event_saved",
      title: "Saved for later",
      subtitle: title,
      emoji: (eventDoc.get("imageEmoji") as string) || "⭐",
      eventId,
      communityId: (eventDoc.get("communityId") as string) || "",
    });
  }
  const reg = await db.collection("event_registrations").doc(`${eventId}_${uid}`).get();
  return {
    event: eventToMap(eventDoc.id, eventDoc.data()!, reg.exists, isSaved),
  };
});

export const listSavedEvents = functions.https.onCall(async (_data, context) => {
  const uid = requireAuth(context);
  const saves = await db.collection("event_saves").where("userId", "==", uid).limit(30).get();
  const regs = await db.collection("event_registrations").where("userId", "==", uid).get();
  const registered = new Set(regs.docs.map((d) => d.get("eventId") as string));
  const items: Record<string, unknown>[] = [];
  for (const save of saves.docs) {
    const eventId = save.get("eventId") as string;
    const eventDoc = await db.collection("events").doc(eventId).get();
    if (eventDoc.exists) {
      items.push(eventToMap(eventDoc.id, eventDoc.data()!, registered.has(eventId), true));
    }
  }
  return { items };
});
