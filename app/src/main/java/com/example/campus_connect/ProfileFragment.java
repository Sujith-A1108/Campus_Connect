package com.example.campus_connect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private TextView profileName, profileHeadline, profileLocation, profileAbout;
    private ImageView settingsButton, editProfileIcon;
    private LinearLayout skillsContainer;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    // **THE FIX: Step 1 - Add a variable to hold the listener**
    private ListenerRegistration profileListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        profileName = view.findViewById(R.id.profile_name);
        profileHeadline = view.findViewById(R.id.profile_headline);
        profileLocation = view.findViewById(R.id.profile_location);
        profileAbout = view.findViewById(R.id.profile_about);
        settingsButton = view.findViewById(R.id.settings_button);
        editProfileIcon = view.findViewById(R.id.edit_profile_icon);
        skillsContainer = view.findViewById(R.id.skills_container);

        // Setup UI visibility and click listeners
        setupButtons();

        if (userId != null) {
            loadUserProfile(userId);
        }
    }

    private void setupButtons() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && userId != null && userId.equals(currentUser.getUid())) {
            settingsButton.setVisibility(View.VISIBLE);
            editProfileIcon.setVisibility(View.VISIBLE);
        } else {
            settingsButton.setVisibility(View.GONE);
            editProfileIcon.setVisibility(View.GONE);
        }

        settingsButton.setOnClickListener(v -> {
            if (isAdded()) {
                startActivity(new Intent(requireActivity(), SettingsActivity.class));
            }
        });

        editProfileIcon.setOnClickListener(v -> {
            if (isAdded()) {
                startActivity(new Intent(requireActivity(), EditProfileActivity.class));
            }
        });
    }

    private void loadUserProfile(String uid) {
        DocumentReference docRef = db.collection("users").document(uid);

        // **THE FIX: Step 2 - Store the listener when it's attached**
        profileListener = docRef.addSnapshotListener((document, e) -> {
            if (e != null || !isAdded() || getContext() == null) {
                Log.w(TAG, "Listen failed or fragment detached.", e);
                return;
            }

            if (document != null && document.exists()) {
                profileName.setText(document.getString("name"));
                profileHeadline.setText("Student"); // Placeholder
                profileLocation.setText(document.getString("location"));
                profileAbout.setText(document.getString("about"));

                if (document.contains("skills")) {
                    Object rawSkills = document.get("skills");
                    List<String> skills = new ArrayList<>();
                    if (rawSkills instanceof List) {
                        for (Object o : (List<?>) rawSkills) {
                            if (o != null) skills.add(o.toString());
                        }
                    } else if (rawSkills instanceof String) {
                        String s = (String) rawSkills;
                        for (String part : s.split("\\s*,\\s*")) {
                            if (!part.trim().isEmpty()) skills.add(part.trim());
                        }
                    }

                    skillsContainer.removeAllViews();
                    for (String skill : skills) {
                        TextView skillView = new TextView(getContext());
                        skillView.setText("● " + skill);
                        skillView.setTextSize(16f);
                        skillView.setPadding(0, 4, 0, 4);
                        skillsContainer.addView(skillView);
                    }
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // **THE FIX: Step 3 - Detach the listener when the view is destroyed**
        if (profileListener != null) {
            profileListener.remove();
        }
    }
}
