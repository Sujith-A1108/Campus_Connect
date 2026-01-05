# Cloud Functions for Campus_Connect

This folder contains Firebase Cloud Functions that perform server-side image moderation using Google Cloud Vision API.

Setup
1. Install dependencies:
   npm install

2. Enable APIs in GCP Console:
   - Cloud Vision API
   - Cloud Functions API
   - Cloud Storage (if not enabled)

3. Initialize Firebase functions (if not already):
   firebase login
   firebase init functions

4. Deploy:
   firebase deploy --only functions:moderatePostImage

Behavior
- Trigger: Storage `onFinalize` for files uploaded under `posts/{communityId}/{postId}.jpg`.
- The function inspects labels, faces, and landmarks and sets the related Firestore document `communities/{communityId}/announcements/{postId}` `status` field to `approved` or `rejected` and writes `moderationReason` for rejected posts.
- Rejected images are moved into `quarantine/{communityId}/{postId}_{timestamp}.jpg`.

Note
- Ensure your project has billing enabled to use Vision API at scale.