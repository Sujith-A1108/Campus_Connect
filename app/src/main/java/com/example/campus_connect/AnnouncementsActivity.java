package com.example.campus_connect;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity {

    private static final String TAG = "AnnouncementsActivity";

    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private List<Announcement> announcementList;
    private Button newAnnouncementButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String communityId;
    private String userYear;

    private ListenerRegistration announcementsListener;

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
                            announcementList.add(announcement);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showNewAnnouncementDialog() {
        // ... Dialog logic remains the same
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Announcement");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);

        builder.setPositiveButton("Post", (dialog, which) -> {
            String content = input.getText().toString();
            postAnnouncement(content);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void postAnnouncement(String content) {
        // ... Post logic remains the same
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String authorName = documentSnapshot.getString("name");
                    Announcement announcement = new Announcement(authorName, content);

                    db.collection("communities").document(communityId).collection("announcements")
                            .add(announcement)
                            .addOnSuccessListener(documentReference -> {
                                documentReference.update("timestamp", System.currentTimeMillis());
                            });
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
}
