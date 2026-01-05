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
        holder.content.setText(announcement.getContent());

        if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(holder.image.getContext()).load(announcement.getImageUrl()).into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }

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
        TextView author;
        TextView content;
        ImageView image;
        TextView status;

        AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.announcement_author);
            content = itemView.findViewById(R.id.announcement_content);
            image = itemView.findViewById(R.id.announcement_image);
            status = itemView.findViewById(R.id.announcement_status);
        }
    }
}