import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";
import { fanOutToCommunity, pushActivity } from "./shared/activity";

const db = admin.firestore();

function requireAuth(context: functions.https.CallableContext): string {
  if (!context.auth?.uid) {
    throw new functions.https.HttpsError("unauthenticated", "Sign in required");
  }
  return context.auth.uid;
}

async function displayName(uid: string): Promise<string> {
  const userDoc = await db.collection("users").doc(uid).get();
  const name = userDoc.get("displayName") as string | undefined;
  if (name?.trim()) return name.trim();
  const profile = await db.collection("profiles").doc(uid).get();
  return (profile.get("name") as string | undefined)?.trim() || "Member";
}

async function savedEventIds(uid: string): Promise<Set<string>> {
  const snap = await db.collection("event_saves").where("userId", "==", uid).limit(50).get();
  return new Set(snap.docs.map((d) => d.get("eventId") as string));
}

function parseTimelineDays(raw: unknown): Record<string, unknown>[] {
  if (!Array.isArray(raw)) return [];
  return raw.map((day, index) => {
    const d = day as Record<string, unknown>;
    const segments = Array.isArray(d.segments)
      ? (d.segments as Record<string, unknown>[]).map((s) => ({
          label: String(s.label || "").trim(),
          startTime: String(s.startTime || "").trim(),
          endTime: String(s.endTime || "").trim(),
        }))
      : [];
    const extraFields = Array.isArray(d.extraFields)
      ? (d.extraFields as Record<string, unknown>[]).map((f) => ({
          label: String(f.label || "").trim(),
          value: String(f.value || "").trim(),
        }))
      : [];
    return {
      dayNumber: Number(d.dayNumber) || index + 1,
      title: String(d.title || "").trim(),
      venue: String(d.venue || "").trim(),
      segments,
      extraFields,
    };
  });
}

function parseCustomFields(raw: unknown): Record<string, unknown>[] {
  if (!Array.isArray(raw)) return [];
  return (raw as Record<string, unknown>[]).map((f) => ({
    label: String(f.label || "").trim(),
    value: String(f.value || "").trim(),
  }));
}

function parseTags(raw: unknown): string[] {
  if (!Array.isArray(raw)) return [];
  return (raw as unknown[])
    .map((t) => String(t || "").trim())
    .filter((t) => t.length > 0)
    .slice(0, 8);
}

function eventToMap(
  id: string,
  data: FirebaseFirestore.DocumentData,
  isRegistered = false,
  isSaved = false
) {
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
    hasTimeline: Boolean(data.hasTimeline),
    dayCount: Number(data.dayCount) || 1,
    prize: data.prize || "",
    participantMessage: data.participantMessage || "",
    timelineDays: data.timelineDays || [],
    customFields: data.customFields || [],
    tags: data.tags || [],
  };
}

export const createEvent = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const title = String(data.title || "").trim();
  if (title.length < 3) {
    throw new functions.https.HttpsError("invalid-argument", "Title too short");
  }
  const organizerName = await displayName(uid);
  const ref = db.collection("events").doc();
  const hasTimeline = Boolean(data.hasTimeline);
  const dayCount = Math.min(Math.max(Number(data.dayCount) || 1, 1), 14);
  const timelineDays = hasTimeline ? parseTimelineDays(data.timelineDays) : [];
  const customFields = parseCustomFields(data.customFields);
  const prize = String(data.prize || "").trim();
  const participantMessage = String(data.participantMessage || "").trim();
  const tags = parseTags(data.tags);
  if (tags.length === 0) {
    throw new functions.https.HttpsError("invalid-argument", "Pick at least one tag for your event");
  }
  const category = String(data.category || "OTHER").toUpperCase();
  if (!["SPORTS", "MARATHON", "CHESS", "DANCE", "FITNESS", "CYCLING", "CRICKET", "OTHER"].includes(category)) {
    throw new functions.https.HttpsError("invalid-argument", "Invalid event category");
  }
  const payload = {
    organizerId: uid,
    organizerName,
    title,
    description: String(data.description || "").trim(),
    category,
    venue: String(data.venue || "").trim(),
    address: String(data.address || "").trim(),
    startAt: Number(data.startAt) || Date.now(),
    endAt: Number(data.endAt) || Date.now(),
    maxParticipants: Math.min(Math.max(Number(data.maxParticipants) || 50, 1), 5000),
    participantCount: 0,
    fee: String(data.fee || "Free").trim(),
    rules: String(data.rules || "").trim(),
    communityId: String(data.communityId || ""),
    communityName: "",
    imageEmoji: String(data.imageEmoji || "🎪"),
    status: "upcoming",
    hasTimeline,
    dayCount,
    prize,
    participantMessage,
    timelineDays,
    customFields,
    tags,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  };
  if (payload.communityId) {
    const comm = await db.collection("communities").doc(payload.communityId).get();
    payload.communityName = (comm.get("name") as string) || "";
  }
  await ref.set(payload);

  await pushActivity({
    userId: uid,
    type: "event_created",
    title: "You organised an event",
    subtitle: title,
    emoji: payload.imageEmoji,
    eventId: ref.id,
    communityId: payload.communityId,
    actorUserId: uid,
  });

  if (payload.communityId) {
    await fanOutToCommunity(payload.communityId, uid, {
      type: "event_created",
      title: "New event in your community",
      subtitle: `${title} · ${payload.communityName}`,
      emoji: payload.imageEmoji,
      eventId: ref.id,
      communityId: payload.communityId,
      actorUserId: uid,
    });
  }

  return { event: eventToMap(ref.id, payload) };
});

export const listEvents = functions.https.onCall(async (data, context) => {
  requireAuth(context);
  const uid = context.auth!.uid;
  const category = String(data.category || "ALL").toUpperCase();
  const tag = String(data.tag || "").trim();
  const search = String(data.search || "").trim().toLowerCase();
  const timelineOnly = Boolean(data.timelineOnly);
  const snap = await db.collection("events").orderBy("startAt", "asc").limit(100).get();
  const regs = await db.collection("event_registrations").where("userId", "==", uid).get();
  const registered = new Set(regs.docs.map((d) => d.get("eventId") as string));
  const saved = await savedEventIds(uid);
  let items = snap.docs.map((doc) =>
    eventToMap(doc.id, doc.data(), registered.has(doc.id), saved.has(doc.id))
  );
  if (category !== "ALL") {
    items = items.filter((e) => String(e.category).toUpperCase() === category);
  }
  if (tag) {
    items = items.filter((e) => (e.tags as string[]).includes(tag));
  }
  if (timelineOnly) {
    items = items.filter((e) => Boolean(e.hasTimeline));
  }
  if (search) {
    items = items.filter((e) => {
      const hay = [
        e.title,
        e.description,
        e.venue,
        e.organizerName,
        e.communityName,
        ...(e.tags as string[]),
      ]
        .join(" ")
        .toLowerCase();
      return hay.includes(search);
    });
  }
  return { items };
});

export const listMyOrganizedEvents = functions.https.onCall(async (_data, context) => {
  const uid = requireAuth(context);
  const saved = await savedEventIds(uid);
  const snap = await db.collection("events").where("organizerId", "==", uid).orderBy("startAt", "desc").limit(30).get();
  const items = snap.docs.map((doc) => eventToMap(doc.id, doc.data(), false, saved.has(doc.id)));
  return { items };
});

export const listMyRegisteredEvents = functions.https.onCall(async (_data, context) => {
  const uid = requireAuth(context);
  const saved = await savedEventIds(uid);
  const regs = await db.collection("event_registrations").where("userId", "==", uid).limit(30).get();
  const items: Record<string, unknown>[] = [];
  for (const reg of regs.docs) {
    const eventId = reg.get("eventId") as string;
    const eventDoc = await db.collection("events").doc(eventId).get();
    if (eventDoc.exists) items.push(eventToMap(eventDoc.id, eventDoc.data()!, true, saved.has(eventId)));
  }
  return { items };
});

export const getEvent = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const eventId = String(data.eventId || "");
  const doc = await db.collection("events").doc(eventId).get();
  if (!doc.exists) throw new functions.https.HttpsError("not-found", "Event not found");
  const reg = await db.collection("event_registrations").doc(`${eventId}_${uid}`).get();
  const save = await db.collection("event_saves").doc(`${uid}_${eventId}`).get();
  return { event: eventToMap(doc.id, doc.data()!, reg.exists, save.exists) };
});

export const registerForEvent = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const eventId = String(data.eventId || "");
  const eventRef = db.collection("events").doc(eventId);
  const regRef = db.collection("event_registrations").doc(`${eventId}_${uid}`);

  await db.runTransaction(async (tx) => {
    const eventDoc = await tx.get(eventRef);
    if (!eventDoc.exists) throw new functions.https.HttpsError("not-found", "Event not found");
    const max = Number(eventDoc.get("maxParticipants")) || 0;
    const count = Number(eventDoc.get("participantCount")) || 0;
    if (max > 0 && count >= max) {
      throw new functions.https.HttpsError("resource-exhausted", "Event is full");
    }
    tx.set(regRef, {
      eventId,
      userId: uid,
      registeredAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    tx.update(eventRef, {
      participantCount: count + 1,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  });

  const updated = await eventRef.get();
  const title = (updated.get("title") as string) || "Event";
  const save = await db.collection("event_saves").doc(`${uid}_${eventId}`).get();
  await pushActivity({
    userId: uid,
    type: "event_registered",
    title: "You're going",
    subtitle: title,
    emoji: (updated.get("imageEmoji") as string) || "🎟️",
    eventId,
    communityId: (updated.get("communityId") as string) || "",
  });

  return { event: eventToMap(updated.id, updated.data()!, true, save.exists) };
});

export const cancelEventRegistration = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const eventId = String(data.eventId || "");
  const eventRef = db.collection("events").doc(eventId);
  const regRef = db.collection("event_registrations").doc(`${eventId}_${uid}`);

  await db.runTransaction(async (tx) => {
    const reg = await tx.get(regRef);
    if (!reg.exists) return;
    const eventDoc = await tx.get(eventRef);
    const count = Math.max((Number(eventDoc.get("participantCount")) || 1) - 1, 0);
    tx.delete(regRef);
    tx.update(eventRef, {
      participantCount: count,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  });
  return { ok: true };
});
