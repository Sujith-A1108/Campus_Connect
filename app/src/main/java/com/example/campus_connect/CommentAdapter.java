package com.example.campus_connect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) { this.comments = comments; }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment c = comments.get(position);
        holder.author.setText(c.getAuthorName());
        holder.content.setText(c.getContent());

        // show relative time
        long ts = c.getTimestamp();
        if (ts > 0) {
            CharSequence rel = android.text.format.DateUtils.getRelativeTimeSpanString(ts, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS);
            holder.timestamp.setText(rel);
        } else {
            holder.timestamp.setText("");
        }
    }

    @Override
    public int getItemCount() { return comments.size(); }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView author, content, timestamp;
        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.comment_author);
            content = itemView.findViewById(R.id.comment_content);
            timestamp = itemView.findViewById(R.id.comment_timestamp);
        }
    }
}