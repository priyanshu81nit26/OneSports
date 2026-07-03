import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

const db = admin.firestore();

export const onUserCreated = functions.auth.user().onCreate(async (user) => {
  await db.collection("users").doc(user.uid).set({
    uid: user.uid,
    email: user.email || "",
    displayName: user.displayName || "",
    role: "member",
    plan: "free",
    timezone: "Asia/Kolkata",
    photoURL: user.photoURL || "",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
});
