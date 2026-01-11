package com.example.campus_connect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Install the AndroidX splash screen to avoid double-splash on Android 12+
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);

        // Show the splash layout (uses res/drawable/logo.jpg)
        setContentView(R.layout.activity_splash);

        // Delay navigation so the user sees the splash image
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Get the current user from Firebase
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                Intent intent;
                if (currentUser != null) {
                    // If user is logged in, go directly to the MainActivity
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    // If user is not logged in, go to the LoginActivity
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                // Finish the SplashActivity so the user can't navigate back to it
                finish();
            }
        }, SPLASH_DELAY_MS);
    }
}