package com.example.campus_connect;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeedViewModel extends ViewModel {
    private MutableLiveData<List<Announcement>> announcements = new MutableLiveData<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration listener;

    public LiveData<List<Announcement>> getAnnouncements() {
        if (announcements.getValue() == null) announcements.setValue(new ArrayList<>());
        return announcements;
    }

    public void startListeningForCollege(String college) {
        if (college == null) return;
        // lazy-init firestore so unit tests that construct the ViewModel without Android/Firebase don't fail
        if (db == null) db = FirestoreProvider.get();
        String communityId = college.replaceAll("\\s+", "_");
        listener = db.collection("communities").document(communityId).collection("announcements")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    List<Announcement> list = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Announcement announcement = doc.toObject(Announcement.class);
                            // set metadata for later navigation/actions
                            announcement.setId(doc.getId());
                            announcement.setCommunityId(communityId);
                            list.add(announcement);
                        }
                    }
                    announcements.postValue(list);
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listener != null) listener.remove();
    }
}