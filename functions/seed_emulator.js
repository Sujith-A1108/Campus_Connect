const admin = require('firebase-admin');

// When running against emulator, set FIRESTORE_EMULATOR_HOST environment var to host:port
// e.g. FIRESTORE_EMULATOR_HOST=127.0.0.1:8080

if (!process.env.FIRESTORE_EMULATOR_HOST) {
  console.error('FIRESTORE_EMULATOR_HOST not set, exiting.');
  process.exit(1);
}

admin.initializeApp({ projectId: 'demo-project' });
const db = admin.firestore();

async function seed() {
  const communityId = 'demo_college';
  const postRef = db.collection('communities').doc(communityId).collection('announcements').doc('post_demo_1');
  await postRef.set({
    author: 'Seed User',
    authorPhotoUrl: '',
    content: 'Welcome to Campus Connect! This is a seeded post.',
    imageUrl: '',
    status: 'approved',
    timestamp: Date.now(),
    likeCount: 0,
    commentCount: 0
  });
  console.log('Seeded post:', postRef.path);
}

seed().then(() => process.exit(0)).catch(err => { console.error(err); process.exit(1); });