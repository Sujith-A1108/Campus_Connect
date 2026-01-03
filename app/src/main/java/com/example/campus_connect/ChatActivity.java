package com.example.campus_connect;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private EditText messageInput;
    private Button sendButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String chatChannelId;
    private ListenerRegistration chatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        String recipientId = getIntent().getStringExtra("recipientId");
        if (mAuth.getCurrentUser() == null || recipientId == null) {
            finish();
            return;
        }
        String currentUserId = mAuth.getCurrentUser().getUid();

        if (currentUserId.compareTo(recipientId) > 0) {
            chatChannelId = currentUserId + recipientId;
        } else {
            chatChannelId = recipientId + currentUserId;
        }

        recyclerView = findViewById(R.id.messages_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);

        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        sendButton.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private void listenForMessages() {
        CollectionReference messagesRef = db.collection("chats").document(chatChannelId).collection("messages");
        chatListener = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (value != null) {
                messageList.clear();
                for (QueryDocumentSnapshot doc : value) { // <-- CORRECTED THIS LINE
                    Message message = doc.toObject(Message.class);
                    messageList.add(message);
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        Message message = new Message(currentUserId, content, System.currentTimeMillis());

        db.collection("chats").document(chatChannelId).collection("messages").add(message);

        messageInput.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) {
            chatListener.remove();
        }
    }
}
