const functions = require('firebase-functions');
const admin = require('firebase-admin');
const {ImageAnnotatorClient} = require('@google-cloud/vision');

admin.initializeApp();
const db = admin.firestore();
const visionClient = new ImageAnnotatorClient();

// Expected storage path: posts/{communityId}/{postId}.jpg
exports.moderatePostImage = functions.storage.object().onFinalize(async (object) => {
  try {
    const filePath = object.name; // e.g. posts/community123/abc123.jpg
    if (!filePath || !filePath.startsWith('posts/')) return null;

    const parts = filePath.split('/');
    if (parts.length < 3) return null;
    const communityId = parts[1];
    const postIdWithExt = parts[2];
    const postId = postIdWithExt.replace(/\.[^.]+$/, '');

    const gcsUri = `gs://${object.bucket}/${filePath}`;

    // Call Vision API: label detection, face detection, landmark detection
    const [labelResponse] = await visionClient.labelDetection(gcsUri);
    const labels = labelResponse.labelAnnotations || [];

    const [faceResponse] = await visionClient.faceDetection(gcsUri);
    const faces = faceResponse.faceAnnotations || [];

    const [landmarkResponse] = await visionClient.landmarkDetection(gcsUri);
    const landmarks = landmarkResponse.landmarkAnnotations || [];

    // Heuristics
    let rejected = false;
    let reason = null;

    // If face(s) detected => likely personal photo
    if (faces.length > 0) {
      rejected = true;
      reason = 'face_detected';
    }

    // Check labels for travel-related tags
    if (!rejected) {
      const travelLabels = ['beach','vacation','travel','tourism','mountain','sea','resort','holiday','hotel'];
      for (const lbl of labels) {
        const desc = (lbl.description || '').toLowerCase();
        const score = lbl.score || 0;
        if (travelLabels.some(t => desc.includes(t)) && score > 0.65) {
          rejected = true;
          reason = 'travel_photo';
          break;
        }
      }
    }

    // Landmark detection suggests touristic photo
    if (!rejected && landmarks.length > 0) {
      rejected = true;
      reason = 'landmark_detected';
    }

    // Update Firestore post doc
    const docRef = db.collection('communities').doc(communityId).collection('announcements').doc(postId);
    const doc = await docRef.get();
    if (!doc.exists) {
      console.warn('Post doc not found for moderation', communityId, postId);
      return null;
    }

    if (rejected) {
      await docRef.update({status: 'rejected', moderationReason: reason});
      // Optionally move or delete the file. Here we move to a quarantine folder.
      const {Storage} = require('@google-cloud/storage');
      const storage = new Storage();
      const bucket = storage.bucket(object.bucket);
      const src = bucket.file(filePath);
      const destPath = `quarantine/${communityId}/${postId}_${Date.now()}.jpg`;
      await src.move(destPath);
      // update imageUrl in doc to the quarantine path or remove
      await docRef.update({imageUrl: `gs://${object.bucket}/${destPath}`});
      return null;
    } else {
      await docRef.update({status: 'approved'});
      return null;
    }
  } catch (err) {
    console.error('Moderation function failed', err);
    return null;
  }
});