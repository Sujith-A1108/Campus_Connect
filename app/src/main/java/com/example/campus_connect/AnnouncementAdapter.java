package com.example.campus_connect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import android.content.Intent;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private List<Announcement> announcementList;

    public AnnouncementAdapter(List<Announcement> announcementList) {
        this.announcementList = announcementList;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.announcement_item, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement announcement = announcementList.get(position);
        holder.author.setText(announcement.getAuthor());

        // author image
        String authorPhoto = announcement.getAuthorPhotoUrl();
        if (authorPhoto != null && !authorPhoto.isEmpty()) {
            Glide.with(holder.author.getContext()).load(authorPhoto).circleCrop().into(holder.authorImage);
        } else {
            holder.authorImage.setImageResource(R.drawable.ic_profile_nav);
        }

        // main image & overlay caption
        if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(holder.image.getContext()).load(announcement.getImageUrl()).into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        holder.captionOverlay.setText(announcement.getContent());

        // counts
        holder.likeCount.setText(String.valueOf(announcement.getLikeCount()));
        holder.commentCount.setText(String.valueOf(announcement.getCommentCount()));

        // show liked state for current user
        com.google.firebase.auth.FirebaseUser current = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (current != null) {
            String uid = current.getUid();
            com.google.firebase.firestore.DocumentReference postRef = FirestoreProvider.get()
                    .collection("communities").document(announcement.getCommunityId()).collection("announcements").document(announcement.getId());
            postRef.collection("likes").document(uid).get().addOnSuccessListener(ds -> {
                boolean liked = ds.exists();
                if (liked) {
                    holder.likeButton.setColorFilter(holder.itemView.getResources().getColor(android.R.color.holo_red_dark));
                    holder.likeButton.setContentDescription("Unlike post");
                } else {
                    holder.likeButton.setColorFilter(holder.itemView.getResources().getColor(android.R.color.darker_gray));
                    holder.likeButton.setContentDescription("Like post");
                }
                // store liked state on the view to allow optimistic toggle
                holder.likeButton.setTag(Boolean.valueOf(liked));
            });
        } else {
            holder.likeButton.setColorFilter(holder.itemView.getResources().getColor(android.R.color.darker_gray));
            holder.likeButton.setContentDescription("Like post");
        }

        holder.itemView.setOnClickListener(v -> {
            // Open PostDetailActivity with announcement data
            android.content.Intent intent = new android.content.Intent(v.getContext(), PostDetailActivity.class);
            intent.putExtra("announcement", announcement);
            v.getContext().startActivity(intent);
        });

        holder.likeButton.setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseUser u = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (u == null) {
                android.widget.Toast.makeText(v.getContext(), "Please login to like posts.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = u.getUid();
            com.google.firebase.firestore.DocumentReference postRef = FirestoreProvider.get()
                    .collection("communities").document(announcement.getCommunityId()).collection("announcements").document(announcement.getId());
            com.google.firebase.firestore.DocumentReference likeDoc = postRef.collection("likes").document(uid);

            // Read current optimistic state from tag
            Boolean currentlyLiked = (Boolean) holder.likeButton.getTag();
            if (currentlyLiked == null) currentlyLiked = Boolean.FALSE;

            // Optimistically update UI
            if (currentlyLiked) {
                // switch to unliked
                holder.likeButton.setTag(Boolean.FALSE);
                holder.likeButton.setColorFilter(holder.itemView.getResources().getColor(android.R.color.darker_gray));
                try {
                    int c = Integer.parseInt(holder.likeCount.getText().toString());
                    holder.likeCount.setText(String.valueOf(Math.max(0, c - 1)));
                } catch (Exception ignored) {}

                // send request to server
                likeDoc.delete().addOnSuccessListener(a -> postRef.update("likeCount", com.google.firebase.firestore.FieldValue.increment(-1)))
                        .addOnFailureListener(e -> {
                            // rollback UI
                            holder.likeButton.setTag(Boolean.TRUE);
                            holder.likeButton.setColorFilter(holder.itemView.getResources().getColor(android.R.color.holo_red_dark));
                            try { int c = Integer.parseInt(holder.likeCount.getText().toString()); holder.likeCount.setText(String.valueOf(c + 1)); } catch (Exception ignored) {}
                        });
            } else {
                // switch to liked
                holder.likeButton.setTag(Boolean.TRUE);
                holder.likeButton.setColorFilter(holder.itemView.getResources().getColor(android.R.color.holo_red_dark));
                try {
                    int c = Integer.parseInt(holder.likeCount.getText().toString());
                    holder.likeCount.setText(String.valueOf(c + 1));
                } catch (Exception ignored) {}

                likeDoc.set(new java.util.HashMap<String,Object>(){{ put("timestamp", System.currentTimeMillis()); }})
                        .addOnSuccessListener(a -> postRef.update("likeCount", com.google.firebase.firestore.FieldValue.increment(1)))
                        .addOnFailureListener(e -> {
                            // rollback UI
                            holder.likeButton.setTag(Boolean.FALSE);
                            holder.likeButton.setColorFilter(holder.itemView.getResources().getColor(android.R.color.darker_gray));
                            try { int c = Integer.parseInt(holder.likeCount.getText().toString()); holder.likeCount.setText(String.valueOf(Math.max(0, c - 1))); } catch (Exception ignored) {}
                        });
            }
        });

        holder.commentButton.setOnClickListener(v -> {
            // navigate to comments / post detail
            android.content.Intent intent = new android.content.Intent(v.getContext(), PostDetailActivity.class);
            intent.putExtra("announcement", announcement);
            v.getContext().startActivity(intent);
        });

        holder.shareButton.setOnClickListener(v -> {
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String text = announcement.getAuthor() + ": " + announcement.getContent();
            if (announcement.getImageUrl() != null) text += " " + announcement.getImageUrl();
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
            v.getContext().startActivity(android.content.Intent.createChooser(shareIntent, "Share post"));
        });

        if (announcement.getStatus() != null && !announcement.getStatus().isEmpty()) {
            String status = announcement.getStatus();
            if ("pending".equals(status)) {
                holder.status.setText("Status: pending moderation");
            } else if ("approved".equals(status)) {
                holder.status.setText("Status: approved");
            } else if ("rejected".equals(status)) {
                holder.status.setText("Status: rejected");
            } else {
                holder.status.setText(status);
            }
            holder.status.setVisibility(View.VISIBLE);
        } else {
            holder.status.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        ImageView authorImage;
        TextView author;
        ImageView image;
        TextView captionOverlay;
        ImageView likeButton;
        ImageView commentButton;
        ImageView shareButton;
        TextView likeCount;
        TextView commentCount;
        TextView status;

        AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            authorImage = itemView.findViewById(R.id.post_author_image);
            author = itemView.findViewById(R.id.post_author_name);
            image = itemView.findViewById(R.id.post_image);
            captionOverlay = itemView.findViewById(R.id.post_caption_overlay);
            likeButton = itemView.findViewById(R.id.like_button);
            commentButton = itemView.findViewById(R.id.comment_button);
            shareButton = itemView.findViewById(R.id.share_button);
            likeCount = itemView.findViewById(R.id.like_count);
            commentCount = itemView.findViewById(R.id.comment_count);
            status = itemView.findViewById(R.id.announcement_status);
        }
    }
}