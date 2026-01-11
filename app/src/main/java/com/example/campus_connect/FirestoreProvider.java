package com.example.campus_connect;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreProvider {
    private static volatile FirebaseFirestore instance;

    public static FirebaseFirestore get() {
        if (instance == null) {
            synchronized (FirestoreProvider.class) {
                if (instance == null) instance = FirebaseFirestore.getInstance();
            }
        }
        return instance;
    }

    // For tests/CI to inject an emulator-backed instance
    public static void setInstanceForTests(FirebaseFirestore firestore) {
        instance = firestore;
    }
}