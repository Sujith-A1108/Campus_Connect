package com.example.campus_connect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editAbout, editLocation, editSkills;
    private Button saveProfileButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editAbout = findViewById(R.id.edit_about);
        editLocation = findViewById(R.id.edit_location);
        editSkills = findViewById(R.id.edit_skills);
        saveProfileButton = findViewById(R.id.save_profile_button);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userRef = db.collection("users").document(currentUser.getUid());
            loadUserData();
        }

        saveProfileButton.setOnClickListener(v -> saveProfileData());
    }

    private void loadUserData() {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                editAbout.setText(documentSnapshot.getString("about"));
                editLocation.setText(documentSnapshot.getString("location"));
                if (documentSnapshot.contains("skills")) {
                    editSkills.setText(String.join(", ", documentSnapshot.get("skills", String.class)));
                }
            }
        });
    }

    private void saveProfileData() {
        String about = editAbout.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String skillsStr = editSkills.getText().toString().trim();
        
        // Split skills string by comma and trim whitespace
        java.util.List<String> skills = Arrays.asList(skillsStr.split("\\s*,\\s*"));

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("about", about);
        profileData.put("location", location);
        profileData.put("skills", skills);

        userRef.update(profileData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity and return to the profile
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Error updating profile.", Toast.LENGTH_SHORT).show());
    }
}