package com.example.campus_connect;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity {

    private static final String TAG = "AnnouncementsActivity";
    private static final int REQUEST_IMAGE_PICK = 1001;

    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private List<Announcement> announcementList;
    private Button newAnnouncementButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String communityId;
    private String userYear;

    private ListenerRegistration announcementsListener;

    // image selection state for new announcement
    private Uri selectedImageUri = null;
    private ImageView currentDialogImagePreview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        communityId = getIntent().getStringExtra("communityId");

        recyclerView = findViewById(R.id.announcements_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementList = new ArrayList<>();
        adapter = new AnnouncementAdapter(announcementList);
        recyclerView.setAdapter(adapter);

        newAnnouncementButton = findViewById(R.id.new_announcement_button);
        newAnnouncementButton.setOnClickListener(v -> showNewAnnouncementDialog());

        fetchUserYearAndSetupPermissions();
        listenForAnnouncements();
    }

    private void fetchUserYearAndSetupPermissions() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    userYear = documentSnapshot.getString("year");
                    setupPermissions();
                }
            });
        }
    }

    private void setupPermissions() {
        boolean isCollegeCommunity = communityId != null && !communityId.matches(".*_\\d{4}");
        boolean canPost = (!isCollegeCommunity) || (userYear != null && (userYear.equals("3rd") || userYear.equals("4th")));

        newAnnouncementButton.setVisibility(canPost ? View.VISIBLE : View.GONE);
    }

    private void listenForAnnouncements() {
        if (communityId == null) return;
        announcementsListener = db.collection("communities").document(communityId).collection("announcements")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    announcementList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) { // <-- CORRECTED THIS LINE
                            Announcement announcement = doc.toObject(Announcement.class);
                            announcement.setId(doc.getId());
                            announcement.setCommunityId(communityId);
                            announcementList.add(announcement);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showNewAnnouncementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Announcement");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_announcement, null);
        final EditText input = dialogView.findViewById(R.id.new_announcement_content);
        final ImageView preview = dialogView.findViewById(R.id.new_announcement_image_preview);
        final Button pickImage = dialogView.findViewById(R.id.new_announcement_pick_image);

        // keep a reference so onActivityResult can update preview
        currentDialogImagePreview = preview;

        pickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_IMAGE_PICK);
        });

        builder.setView(dialogView);

        builder.setPositiveButton("Post", (dialog, which) -> {
            String content = input.getText().toString();
            postAnnouncement(content);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            selectedImageUri = null;
            dialog.cancel();
        });

        AlertDialog dialog = builder.show();
        dialog.setOnDismissListener(d -> {
            selectedImageUri = null;
            currentDialogImagePreview = null;
        });
    }

    private void postAnnouncement(String content) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String authorName = documentSnapshot.getString("name");

                    if (selectedImageUri != null) {
                        // Create the announcement document first in 'pending' state so cloud functions
                        // can map storage files to the document ID reliably.
                        Announcement announcement = new Announcement(authorName, content, null, "pending");
                        db.collection("communities").document(communityId).collection("announcements")
                                .add(announcement)
                                .addOnSuccessListener(documentReference -> {
                                    String postId = documentReference.getId();
                                    documentReference.update("timestamp", System.currentTimeMillis());

                                    // upload image using the postId so Cloud Function can find the related doc
                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                                            .child("posts/" + communityId + "/" + postId + ".jpg");
                                    storageRef.putFile(selectedImageUri)
                                            .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                String imageUrl = uri.toString();
                                                // update the announcement with imageUrl (status remains pending until moderation)
                                                documentReference.update("imageUrl", imageUrl);
                                                selectedImageUri = null;
                                            }))
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(AnnouncementsActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                                                // mark post as rejected due to upload failure
                                                documentReference.update("status", "rejected", "moderationReason", "upload_failed");
                                            });
                                });
                    } else {
                        Announcement announcement = new Announcement(authorName, content);
                        db.collection("communities").document(communityId).collection("announcements")
                                .add(announcement)
                                .addOnSuccessListener(documentReference -> documentReference.update("timestamp", System.currentTimeMillis()));
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (announcementsListener != null) {
            announcementsListener.remove();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            selectedImageUri = uri;

            // Run client-side moderation checks using ML Kit
            final ImageView preview = currentDialogImagePreview;
            try {
                InputImage image = InputImage.fromFilePath(this, selectedImageUri);

                // Face detection
                FaceDetectorOptions faceOptions = new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .build();
                FaceDetector detector = FaceDetection.getClient(faceOptions);
                detector.process(image)
                        .addOnSuccessListener(faces -> {
                            if (faces != null && !faces.isEmpty()) {
                                Toast.makeText(AnnouncementsActivity.this, "Image blocked: contains people/personal photo.", Toast.LENGTH_LONG).show();
                                selectedImageUri = null;
                                preview.setVisibility(View.GONE);
                                return;
                            }

                            // NOTE: Label detection (ML Kit Image Labeling) is temporarily disabled due to
                            // a dependency compilation issue. We still perform face detection locally
                            // and rely on server-side Cloud Vision for travel/photo checks.
                            // accepted - show preview
                            preview.setVisibility(View.VISIBLE);
                            Glide.with(AnnouncementsActivity.this).load(selectedImageUri).into(preview);

                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AnnouncementsActivity.this, "Image analysis failed.", Toast.LENGTH_SHORT).show();
                            selectedImageUri = null;
                            preview.setVisibility(View.GONE);
                        });

            } catch (IOException e) {
                Toast.makeText(this, "Failed to read selected image.", Toast.LENGTH_SHORT).show();
                selectedImageUri = null;
                preview.setVisibility(View.GONE);
            }
        }
    }
}
