import * as admin from "firebase-admin";

import * as functions from "firebase-functions/v1";



admin.initializeApp();



export { onUserCreated } from "./auth";

export {

  createEvent,

  listEvents,

  listMyOrganizedEvents,

  listMyRegisteredEvents,

  getEvent,

  registerForEvent,

  cancelEventRegistration,

} from "./events";

export {

  createCommunity,

  listCommunities,

  listMyCommunities,

  getCommunity,

  joinCommunity,
  joinCommunityByCode,
  joinCommunityByName,
  leaveCommunity,

  listCommunityUpdates,

  postCommunityUpdate,

  listCommunityMembers,

  sendConnectionRequest,

} from "./communities";

export { listActivityFeed, getDistrictSummary } from "./feed";

export { toggleEventSave, listSavedEvents } from "./saves";

export {

  listMyConnections,

  listPendingConnectionRequests,

  respondConnectionRequest,

} from "./connections";



const db = admin.firestore();



export const cleanupStaleConnectionRequests = functions.pubsub

  .schedule("every 168 hours")

  .onRun(async () => {

    const cutoff = Date.now() - 30 * 24 * 60 * 60 * 1000;

    const snap = await db.collection("connection_requests")

      .where("createdAt", "<", new Date(cutoff))

      .limit(200)

      .get();

    if (snap.empty) return;

    const batch = db.batch();

    snap.docs.forEach((doc) => batch.delete(doc.ref));

    await batch.commit();

  });

