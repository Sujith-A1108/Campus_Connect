package com.example.campus_connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private List<Announcement> announcementList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration announcementsListener;
    private FeedViewModel viewModel;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the feed layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.feed_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        announcementList = new ArrayList<>();
        adapter = new AnnouncementAdapter(announcementList);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Use ViewModel to observe announcements and decouple from lifecycle
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(FeedViewModel.class);
        viewModel.getAnnouncements().observe(getViewLifecycleOwner(), announcements -> {
            announcementList.clear();
            announcementList.addAll(announcements);
            adapter.notifyDataSetChanged();
        });

        // fetch user college and start listening
        fetchUserCollegeAndListen();
    }
    private void fetchUserCollegeAndListen() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String college = documentSnapshot.getString("college");
                        if (college != null && !college.isEmpty()) {
                            viewModel.startListeningForCollege(college);
                        }
                    }
                })
                .addOnFailureListener(e -> android.util.Log.w(TAG, "Failed to fetch user profile", e));
    }

    private void listenForAnnouncements(String communityId) {
        if (communityId == null) return;
        announcementsListener = db.collection("communities").document(communityId).collection("announcements")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    announcementList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Announcement announcement = doc.toObject(Announcement.class);
                            announcementList.add(announcement);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (announcementsListener != null) {
            announcementsListener.remove();
        }
    }
}