package com.example.campus_connect;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private CommentAdapter commentAdapter;
    private List<Comment> comments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        db = FirestoreProvider.get();
        mAuth = FirebaseAuth.getInstance();

        Announcement announcement = (Announcement) getIntent().getSerializableExtra("announcement");
        if (announcement == null) return;

        String communityId = announcement.getCommunityId();
        String postId = announcement.getId();

        ImageView image = findViewById(R.id.detail_image);
        TextView author = findViewById(R.id.detail_author);
        TextView content = findViewById(R.id.detail_content);
        TextView likes = findViewById(R.id.detail_like_count);
        ImageView likeButton = findViewById(R.id.detail_like_button);
        TextView timestampView = findViewById(R.id.detail_timestamp);

        RecyclerView commentsRecycler = findViewById(R.id.comments_recycler);
        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(comments);
        commentsRecycler.setAdapter(commentAdapter);

        TextInputEditText commentInput = findViewById(R.id.comment_input);
        MaterialButton commentSend = findViewById(R.id.comment_send);

        author.setText(announcement.getAuthor());
        content.setText(announcement.getContent());
        likes.setText(String.valueOf(announcement.getLikeCount()));

        // show created time
        long ts = announcement.getTimestamp();
        if (ts > 0) {
            CharSequence rel = android.text.format.DateUtils.getRelativeTimeSpanString(ts, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS);
            timestampView.setText(rel);
        } else {
            timestampView.setText("");
        }
        if (!TextUtils.isEmpty(announcement.getImageUrl())) {
            Glide.with(this).load(announcement.getImageUrl()).into(image);
        }

        // Listen for comments
        db.collection("communities").document(communityId).collection("announcements")
                .document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    comments.clear();
                    if (value != null) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                            Comment c = doc.toObject(Comment.class);
                            c.setId(doc.getId());
                            comments.add(c);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                });

        // Listen for live counts
        DocumentReference postRef = db.collection("communities").document(communityId).collection("announcements").document(postId);
        postRef.addSnapshotListener((snap, err) -> {
            if (err != null || snap == null || !snap.exists()) return;
            long likeCount = snap.getLong("likeCount") == null ? 0 : snap.getLong("likeCount");
            long commentCount = snap.getLong("commentCount") == null ? 0 : snap.getLong("commentCount");
            likes.setText(String.valueOf(likeCount));
        });

        // set initial liked state
        FirebaseUser u = mAuth.getCurrentUser();
        if (u != null) {
            String uid = u.getUid();
            postRef.collection("likes").document(uid).get().addOnSuccessListener(docSnap -> {
                boolean liked = docSnap.exists();
                setLikeButtonState(likeButton, liked);
            });
        }

        // like toggle (per-user doc)
        likeButton.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please login to like posts.", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = user.getUid();
            DocumentReference likeDoc = postRef.collection("likes").document(uid);
            likeDoc.get().addOnSuccessListener(docSnapshot -> {
                if (docSnapshot.exists()) {
                    // unlike
                    likeDoc.delete().addOnSuccessListener(a -> {
                        postRef.update("likeCount", FieldValue.increment(-1));
                        setLikeButtonState(likeButton, false);
                    });
                } else {
                    // like
                    likeDoc.set(new java.util.HashMap<String,Object>(){{ put("timestamp", System.currentTimeMillis()); }}).addOnSuccessListener(a -> {
                        postRef.update("likeCount", FieldValue.increment(1));
                        setLikeButtonState(likeButton, true);
                    });
                }
            });
        });

        // send comment
        commentSend.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please login to comment.", Toast.LENGTH_SHORT).show();
                return;
            }
            String text = commentInput.getText() == null ? "" : commentInput.getText().toString().trim();
            if (text.isEmpty()) return;

            // fetch user name
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(userSnap -> {
                String name = userSnap.exists() ? userSnap.getString("name") : "Unknown";
                Comment c = new Comment();
                c.setAuthorId(user.getUid());
                c.setAuthorName(name);
                c.setContent(text);
                c.setTimestamp(System.currentTimeMillis());

                postRef.collection("comments").add(c).addOnSuccessListener(docRef -> {
                    postRef.update("commentCount", FieldValue.increment(1));
                    commentInput.setText("");
                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to post comment.", Toast.LENGTH_SHORT).show());
            });
        });

    }

    private void setLikeButtonState(ImageView likeButton, boolean liked) {
        if (liked) {
            likeButton.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
            likeButton.setContentDescription("Unlike post");
        } else {
            likeButton.setColorFilter(getResources().getColor(android.R.color.darker_gray));
            likeButton.setContentDescription("Like post");
        }
    }
}