const crypto = require("crypto");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onRequest } = require("firebase-functions/v2/https");
const { logger } = require("firebase-functions");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");

admin.initializeApp();

const cloudinaryApiKey = defineSecret("CLOUDINARY_API_KEY");
const cloudinaryApiSecret = defineSecret("CLOUDINARY_API_SECRET");

const SIGNABLE_UPLOAD_PARAMS = new Set([
  "public_id",
  "folder",
  "tags",
  "context",
  "type",
  "format",
  "upload_preset",
  "eager",
  "transformation",
  "headers",
  "face_coordinates",
  "ocr",
  "raw_convert",
  "categorization",
  "detection",
  "similarity_search",
  "auto_tagging",
  "access_control",
]);

const RECORDING_FOLDER_PREFIX = "silent_sos/recordings/";

exports.cloudinaryUploadSignature = onRequest(
  {
    region: "us-central1",
    secrets: [cloudinaryApiKey, cloudinaryApiSecret],
  },
  async (request, response) => {
    if (request.method !== "POST") {
      response.status(405).json({ error: "Method not allowed" });
      return;
    }

    try {
      const authHeader = request.get("authorization") || "";
      if (!authHeader.startsWith("Bearer ")) {
        response.status(401).json({ error: "Missing Firebase auth token" });
        return;
      }

      const decodedToken = await admin.auth().verifyIdToken(authHeader.slice("Bearer ".length));
      const eventId = request.body?.eventId;
      const params = request.body?.params || {};

      if (typeof eventId !== "string" || eventId.length === 0 || eventId.includes("/")) {
        response.status(400).json({ error: "Invalid SOS event id" });
        return;
      }

      const eventSnapshot = await admin.firestore().collection("sos_events").doc(eventId).get();
      if (!eventSnapshot.exists || eventSnapshot.get("userId") !== decodedToken.uid) {
        response.status(403).json({ error: "SOS event does not belong to the authenticated user" });
        return;
      }

      const signedParams = sanitizeCloudinaryParams(params);
      const expectedFolder = `${RECORDING_FOLDER_PREFIX}${eventId}`;
      if (signedParams.folder !== expectedFolder) {
        response.status(400).json({ error: "Invalid Cloudinary recording folder" });
        return;
      }

      const timestamp = Math.floor(Date.now() / 1000);
      signedParams.timestamp = timestamp;

      response.json({
        apiKey: cloudinaryApiKey.value(),
        timestamp,
        signature: signCloudinaryParams(signedParams, cloudinaryApiSecret.value()),
      });
    } catch (error) {
      logger.error("Failed to create Cloudinary upload signature", error);
      response.status(500).json({
        error: error instanceof Error ? error.message : String(error),
      });
    }
  }
);

exports.dispatchSilentSOSNotification = onDocumentCreated(
  "notification_queue/{requestId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("Notification queue trigger fired without snapshot data");
      return;
    }

    const requestId = event.params.requestId;
    const payload = snapshot.data();
    const queueRef = admin.firestore().collection("notification_queue").doc(requestId);

    try {
      if (!payload?.contactPhoneNumber || !payload?.type) {
        await queueRef.set(
          {
            status: "FAILED",
            errorMessage: "Missing required notification payload fields",
            deliveredAt: Date.now(),
          },
          { merge: true }
        );
        return;
      }

      const matchingUsers = await admin
        .firestore()
        .collection("users")
        .where("phoneNumber", "==", payload.contactPhoneNumber)
        .limit(1)
        .get();

      if (matchingUsers.empty) {
        await queueRef.set(
          {
            status: "SKIPPED",
            errorMessage: "No SilentSOS user registered for this phone number",
            deliveredAt: Date.now(),
          },
          { merge: true }
        );
        return;
      }

      const targetUser = matchingUsers.docs[0].data();
      if (!targetUser.fcmToken) {
        await queueRef.set(
          {
            status: "SKIPPED",
            errorMessage: "Target user does not have an active FCM token",
            deliveredAt: Date.now(),
          },
          { merge: true }
        );
        return;
      }

      await admin.messaging().send({
        token: targetUser.fcmToken,
        notification: {
          title: payload.title || "SilentSOS alert",
          body: payload.body || "A safety event requires your attention.",
        },
        data: {
          type: payload.type === "SOS_STATUS_UPDATE" ? "sos_status_update" : "sos_alert",
          sosEventId: payload.sosEventId || "",
          latitude: payload.latitude != null ? String(payload.latitude) : "",
          longitude: payload.longitude != null ? String(payload.longitude) : "",
          title: payload.title || "SilentSOS alert",
          body: payload.body || "A safety event requires your attention.",
          userId: payload.userId || "",
        },
        android: {
          priority: "high",
          notification: {
            channelId: "sos_alerts",
            sound: "default",
          },
        },
      });

      await queueRef.set(
        {
          status: "SENT",
          errorMessage: "",
          deliveredAt: Date.now(),
        },
        { merge: true }
      );
    } catch (error) {
      logger.error("Failed to dispatch SilentSOS notification", error);
      await queueRef.set(
        {
          status: "FAILED",
          errorMessage: error instanceof Error ? error.message : String(error),
          deliveredAt: Date.now(),
        },
        { merge: true }
      );
    }
  }
);

function sanitizeCloudinaryParams(params) {
  if (!params || typeof params !== "object" || Array.isArray(params)) {
    throw new Error("Cloudinary params must be an object");
  }

  return Object.entries(params).reduce((clean, [key, value]) => {
    if (!SIGNABLE_UPLOAD_PARAMS.has(key)) {
      throw new Error(`Unsupported Cloudinary upload parameter: ${key}`);
    }

    if (value === undefined || value === null || String(value).length === 0) {
      return clean;
    }

    clean[key] = Array.isArray(value) ? value.join(",") : String(value);
    return clean;
  }, {});
}

function signCloudinaryParams(params, apiSecret) {
  const canonical = Object.keys(params)
    .filter((key) => params[key] !== undefined && params[key] !== null && String(params[key]).length > 0)
    .sort()
    .map((key) => `${key}=${String(params[key]).replace(/&/g, "%26")}`)
    .join("&");

  return crypto
    .createHash("sha1")
    .update(`${canonical}${apiSecret}`)
    .digest("hex");
}
