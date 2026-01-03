package com.example.campus_connect;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    private List<String> communityList;

    public CommunityAdapter(List<String> communityList) {
        this.communityList = communityList;
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.community_item, parent, false);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        String communityName = communityList.get(position);
        holder.communityName.setText(communityName);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AnnouncementsActivity.class);
            intent.putExtra("communityId", communityName.replaceAll("\\s+", "_"));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return communityList.size();
    }

    static class CommunityViewHolder extends RecyclerView.ViewHolder {
        TextView communityName;

        CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            communityName = itemView.findViewById(R.id.community_name);
        }
    }
}