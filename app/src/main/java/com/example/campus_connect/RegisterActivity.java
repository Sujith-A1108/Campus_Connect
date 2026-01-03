package com.example.campus_connect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText nameEditText, emailEditText, mobileEditText, passwordEditText, confirmPasswordEditText;
    private CheckBox termsCheckBox;
    private Button registerButton;
    private TextView loginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        mobileEditText = findViewById(R.id.mobile);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        termsCheckBox = findViewById(R.id.terms_checkbox);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);

        registerButton.setOnClickListener(v -> registerUser());

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        final String name = nameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String mobile = mobileEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enforce institutional email domain
        String emailLower = email.toLowerCase();
        if (!emailLower.endsWith("@vvce.ac.in")) {
            emailEditText.setError("Please use your institutional email address (@vvce.ac.in).");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match.");
            return;
        }

        if (!termsCheckBox.isChecked()) {
            Toast.makeText(this, "Please accept the Terms and Conditions.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, authTask -> {
                if (authTask.isSuccessful()) {
                    Log.d(TAG, "createUserWithEmail:success");
                    saveUserProfile(authTask.getResult().getUser().getUid(), name, email, mobile);
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", authTask.getException());
                    Toast.makeText(RegisterActivity.this, "Authentication failed: " + authTask.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
    }

    private void saveUserProfile(String userId, String name, String email, String mobile) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("mobile", mobile);

        db.collection("users").document(userId).set(user)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User profile saved successfully.");
                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving user profile", e);
                Toast.makeText(RegisterActivity.this, "Account created, but failed to save profile. Please log in.", Toast.LENGTH_LONG).show();
                navigateToLogin();
            });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
