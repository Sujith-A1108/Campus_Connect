package com.example.campus_connect;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EmulatorSetup {

    @BeforeClass
    public static void setUpEmulator() {
        // When instrumentation tests run in CI with a host Firestore emulator, the device/emulator should connect to host via 10.0.2.2
        try {
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
            FirestoreProvider.setInstanceForTests(FirebaseFirestore.getInstance());
        } catch (Exception e) {
            // ignore if emulator not available; tests that require network should handle accordingly
            e.printStackTrace();
        }
    }
}
