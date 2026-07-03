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
  const first = (profile.get("firstName") as string | undefined)?.trim() || "";
  const last = (profile.get("lastName") as string | undefined)?.trim() || "";
  const combined = `${first} ${last}`.trim();
  if (combined) return combined;
  return (profile.get("name") as string | undefined)?.trim() || "Member";
}

function normalizeCommunitySlug(raw: string): string {
  return raw.toLowerCase().replace(/[^a-z0-9_]/g, "").slice(0, 24);
}

async function generateJoinCode(): Promise<string> {
  for (let attempt = 0; attempt < 25; attempt++) {
    const code = String(Math.floor(100000 + Math.random() * 900000));
    const existing = await db.collection("community_codes").doc(code).get();
    if (!existing.exists) return code;
  }
  throw new functions.https.HttpsError("resource-exhausted", "Could not generate join code");
}

function communityToMap(id: string, data: FirebaseFirestore.DocumentData, isMember = false) {
  return {
    id,
    name: data.name || "",
    slug: data.slug || "",
    joinCode: data.joinCode || "",
    description: data.description || "",
    type: data.type || "OTHER",
    location: data.location || "",
    adminId: data.adminId || "",
    adminName: data.adminName || "",
    memberCount: data.memberCount || 0,
    emoji: data.emoji || "🏘️",
    isMember,
    latestUpdate: data.latestUpdate || "",
  };
}

async function joinCommunityInternal(uid: string, communityId: string) {
  const commRef = db.collection("communities").doc(communityId);
  const memberRef = db.collection("community_members").doc(`${communityId}_${uid}`);

  await db.runTransaction(async (tx) => {
    const comm = await tx.get(commRef);
    if (!comm.exists) throw new functions.https.HttpsError("not-found", "Community not found");
    const existing = await tx.get(memberRef);
    if (existing.exists) return;
    const count = Number(comm.get("memberCount")) || 0;
    tx.set(memberRef, {
      communityId,
      userId: uid,
      role: "member",
      joinedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    tx.update(commRef, {
      memberCount: count + 1,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  });

  const updated = await commRef.get();
  const commName = (updated.get("name") as string) || "Community";
  await pushActivity({
    userId: uid,
    type: "community_joined",
    title: "Joined a community",
    subtitle: commName,
    emoji: (updated.get("emoji") as string) || "🏘️",
    communityId,
  });
  return communityToMap(updated.id, updated.data()!, true);
}

export const createCommunity = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const name = String(data.name || "").trim();
  const slug = normalizeCommunitySlug(name);
  if (name.length < 6) {
    throw new functions.https.HttpsError("invalid-argument", "Community name must be at least 6 characters");
  }
  if (slug.length < 6) {
    throw new functions.https.HttpsError("invalid-argument", "Use at least 6 letters or numbers in the name");
  }

  const slugRef = db.collection("community_names").doc(slug);
  const existingSlug = await slugRef.get();
  if (existingSlug.exists) {
    throw new functions.https.HttpsError("already-exists", "Community name already taken");
  }

  const joinCode = await generateJoinCode();
  const adminName = await displayName(uid);
  const ref = db.collection("communities").doc();
  const payload = {
    name,
    slug,
    joinCode,
    description: String(data.description || "").trim(),
    type: String(data.type || "OTHER"),
    location: String(data.location || "").trim(),
    adminId: uid,
    adminName,
    memberCount: 1,
    emoji: String(data.emoji || "🏘️"),
    latestUpdate: "Community created",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  };

  await db.runTransaction(async (tx) => {
    tx.set(ref, payload);
    tx.set(slugRef, { communityId: ref.id, slug, createdAt: Date.now() });
    tx.set(db.collection("community_codes").doc(joinCode), { communityId: ref.id, joinCode });
    tx.set(db.collection("community_members").doc(`${ref.id}_${uid}`), {
      communityId: ref.id,
      userId: uid,
      role: "admin",
      joinedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  });

  return { community: communityToMap(ref.id, payload, true) };
});

export const listCommunities = functions.https.onCall(async (_data, context) => {
  const uid = requireAuth(context);
  const snap = await db.collection("communities").orderBy("memberCount", "desc").limit(40).get();
  const mine = await db.collection("community_members").where("userId", "==", uid).get();
  const memberOf = new Set(mine.docs.map((d) => d.get("communityId") as string));
  const items = snap.docs.map((doc) => communityToMap(doc.id, doc.data(), memberOf.has(doc.id)));
  return { items };
});

export const listMyCommunities = functions.https.onCall(async (_data, context) => {
  const uid = requireAuth(context);
  const mine = await db.collection("community_members").where("userId", "==", uid).limit(30).get();
  const items: Record<string, unknown>[] = [];
  for (const m of mine.docs) {
    const communityId = m.get("communityId") as string;
    const doc = await db.collection("communities").doc(communityId).get();
    if (doc.exists) items.push(communityToMap(doc.id, doc.data()!, true));
  }
  return { items };
});

export const getCommunity = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const communityId = String(data.communityId || "");
  const doc = await db.collection("communities").doc(communityId).get();
  if (!doc.exists) throw new functions.https.HttpsError("not-found", "Community not found");
  const member = await db.collection("community_members").doc(`${communityId}_${uid}`).get();
  return { community: communityToMap(doc.id, doc.data()!, member.exists) };
});

export const joinCommunity = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const communityId = String(data.communityId || "");
  const community = await joinCommunityInternal(uid, communityId);
  return { community };
});

export const joinCommunityByCode = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const code = String(data.joinCode || "").trim();
  if (!/^\d{6}$/.test(code)) {
    throw new functions.https.HttpsError("invalid-argument", "Enter a valid 6-digit code");
  }
  const codeDoc = await db.collection("community_codes").doc(code).get();
  if (!codeDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Invalid join code");
  }
  const communityId = codeDoc.get("communityId") as string;
  const community = await joinCommunityInternal(uid, communityId);
  return { community };
});

export const joinCommunityByName = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const slug = normalizeCommunitySlug(String(data.name || ""));
  if (slug.length < 6) {
    throw new functions.https.HttpsError("invalid-argument", "Community name must be at least 6 characters");
  }
  const nameDoc = await db.collection("community_names").doc(slug).get();
  if (!nameDoc.exists) {
    throw new functions.https.HttpsError("not-found", "No community with that name");
  }
  const communityId = nameDoc.get("communityId") as string;
  const community = await joinCommunityInternal(uid, communityId);
  return { community };
});

export const leaveCommunity = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const communityId = String(data.communityId || "");
  const commRef = db.collection("communities").doc(communityId);
  const memberRef = db.collection("community_members").doc(`${communityId}_${uid}`);

  await db.runTransaction(async (tx) => {
    const member = await tx.get(memberRef);
    if (!member.exists) return;
    const comm = await tx.get(commRef);
    const count = Math.max((Number(comm.get("memberCount")) || 1) - 1, 0);
    tx.delete(memberRef);
    tx.update(commRef, { memberCount: count });
  });
  return { ok: true };
});

export const listCommunityUpdates = functions.https.onCall(async (data, context) => {
  requireAuth(context);
  const communityId = String(data.communityId || "");
  const snap = await db.collection("community_updates")
    .where("communityId", "==", communityId)
    .orderBy("timestamp", "desc")
    .limit(30)
    .get();
  const items = snap.docs.map((doc) => ({
    id: doc.id,
    communityId: doc.get("communityId"),
    authorId: doc.get("authorId"),
    authorName: doc.get("authorName"),
    message: doc.get("message"),
    timestamp: doc.get("timestamp") || Date.now(),
  }));
  return { items };
});

export const postCommunityUpdate = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const communityId = String(data.communityId || "");
  const message = String(data.message || "").trim();
  if (!message) throw new functions.https.HttpsError("invalid-argument", "Message required");

  const member = await db.collection("community_members").doc(`${communityId}_${uid}`).get();
  if (!member.exists) {
    throw new functions.https.HttpsError("permission-denied", "Join the community first");
  }

  const authorName = await displayName(uid);
  const ref = db.collection("community_updates").doc();
  const timestamp = Date.now();
  const update = {
    communityId,
    authorId: uid,
    authorName,
    message,
    timestamp,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  };
  await ref.set(update);
  const commDoc = await db.collection("communities").doc(communityId).get();
  const commName = (commDoc.get("name") as string) || "Community";
  await db.collection("communities").doc(communityId).update({
    latestUpdate: message.slice(0, 120),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  await fanOutToCommunity(communityId, uid, {
    type: "community_update",
    title: commName,
    subtitle: message.slice(0, 100),
    emoji: "📣",
    communityId,
    actorUserId: uid,
  });
  return { update: { id: ref.id, ...update } };
});

export const listCommunityMembers = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const communityId = String(data.communityId || "");
  const member = await db.collection("community_members").doc(`${communityId}_${uid}`).get();
  if (!member.exists) {
    throw new functions.https.HttpsError("permission-denied", "Join to view members");
  }

  const membersSnap = await db.collection("community_members").where("communityId", "==", communityId).limit(50).get();
  const [fromConn, toConn, pendingOutSnap] = await Promise.all([
    db.collection("connections").where("fromUserId", "==", uid).get(),
    db.collection("connections").where("toUserId", "==", uid).get(),
    db.collection("connection_requests").where("fromUserId", "==", uid).where("status", "==", "pending").get(),
  ]);
  const connected = new Set<string>();
  for (const d of fromConn.docs) connected.add(d.get("toUserId") as string);
  for (const d of toConn.docs) connected.add(d.get("fromUserId") as string);
  const pendingOut = new Set(pendingOutSnap.docs.map((d) => d.get("toUserId") as string));

  const items: Record<string, unknown>[] = [];
  for (const m of membersSnap.docs) {
    const userId = m.get("userId") as string;
    const profile = await db.collection("profiles").doc(userId).get();
    const user = await db.collection("users").doc(userId).get();
    const first = (profile.get("firstName") as string) || "";
    const last = (profile.get("lastName") as string) || "";
    const name = `${first} ${last}`.trim() || (user.get("displayName") as string) || "Member";
    const username = (profile.get("username") as string) || "";
    let connectionStatus = "none";
    if (connected.has(userId)) connectionStatus = "connected";
    else if (pendingOut.has(userId)) connectionStatus = "pending";
    items.push({
      userId,
      name,
      username,
      avatar: (profile.get("avatar") as string) || "",
      connectionStatus,
    });
  }
  return { items };
});

export const sendConnectionRequest = functions.https.onCall(async (data, context) => {
  const uid = requireAuth(context);
  const targetUserId = String(data.targetUserId || "");
  if (!targetUserId || targetUserId === uid) {
    throw new functions.https.HttpsError("invalid-argument", "Invalid target");
  }
  const existing = await db.collection("connections").doc(`${uid}_${targetUserId}`).get();
  if (existing.exists) {
    throw new functions.https.HttpsError("already-exists", "Already connected");
  }
  const ref = db.collection("connection_requests").doc(`${uid}_${targetUserId}`);
  const senderName = await displayName(uid);
  await ref.set({
    fromUserId: uid,
    toUserId: targetUserId,
    status: "pending",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  await pushActivity({
    userId: targetUserId,
    type: "connection_request",
    title: "New connection request",
    subtitle: `${senderName} wants to connect`,
    emoji: "👋",
    actorUserId: uid,
  });
  return { ok: true };
});
