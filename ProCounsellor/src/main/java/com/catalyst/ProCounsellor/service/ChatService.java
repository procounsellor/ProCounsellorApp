package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.dto.MessageRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private FirebaseDatabase firebaseDatabase; // This is for Realtime Database

    public String startChat(String userId, String counsellorId) throws ExecutionException, InterruptedException {
        // Step 1: Validate userId exists in the users table (Firestore)
        DocumentReference userDocRef = firestore.collection("users").document(userId);
        DocumentSnapshot userSnapshot = userDocRef.get().get();
        if (!userSnapshot.exists()) {
            throw new IllegalArgumentException("Invalid userId: User does not exist.");
        }

        // Step 2: Validate counsellorId exists in the counsellors table (Firestore)
        DocumentReference counsellorDocRef = firestore.collection("counsellors").document(counsellorId);
        DocumentSnapshot counsellorSnapshot = counsellorDocRef.get().get();
        if (!counsellorSnapshot.exists()) {
            throw new IllegalArgumentException("Invalid counsellorId: Counsellor does not exist.");
        }

        // Step 3: Check if a chat already exists between the user and counselor (Firestore)
        ApiFuture<QuerySnapshot> existingChatQuery = firestore.collection("chats")
                .whereEqualTo("userId", userId)
                .whereEqualTo("counsellorId", counsellorId)
                .get();

        QuerySnapshot querySnapshot = existingChatQuery.get();
        if (!querySnapshot.isEmpty()) {
            // A chat already exists, return the existing chatId
            return querySnapshot.getDocuments().get(0).getId();
        }

        // Step 4: Create a new chat if it does not exist (Firestore)
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("userId", userId);
        chatData.put("counsellorId", counsellorId);
        chatData.put("createdAt", FieldValue.serverTimestamp());

        DocumentReference newChatRef = firestore.collection("chats").document();
        newChatRef.set(chatData).get();

        return newChatRef.getId();
    }

    public void sendMessage(String chatId, MessageRequest messageRequest) throws ExecutionException, InterruptedException, IllegalAccessException {
        // Fetch the chat document to validate the user and counselor IDs (Firestore)
        DocumentSnapshot chatSnapshot = firestore.collection("chats").document(chatId).get().get();

        if (!chatSnapshot.exists()) {
            throw new IllegalArgumentException("Chat not found.");
        }

        // Get the userId and counselorId from the chat document (Firestore)
        String storedUserId = chatSnapshot.getString("userId");
        String storedCounselorId = chatSnapshot.getString("counsellorId");

        // Validate that the senderId is either the userId or counselorId
        if (!messageRequest.getSenderId().equals(storedUserId) && !messageRequest.getSenderId().equals(storedCounselorId)) {
            throw new IllegalAccessException("You are not authorized to send a message in this chat.");
        }

        // Step 2: Save the message in Firebase Realtime Database (Real-time messages)
        DatabaseReference chatMessagesRef = firebaseDatabase.getReference("chats")
                .child(chatId)
                .child("messages");

        String messageId = chatMessagesRef.push().getKey();  // Generate a new ID for the message
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", messageRequest.getSenderId());
        messageData.put("text", messageRequest.getText());
        messageData.put("timestamp", System.currentTimeMillis());

        chatMessagesRef.child(messageId).setValueAsync(messageData);
    }

    public CompletableFuture<List<Map<String, Object>>> getChatMessages(String chatId) {
        // Initialize a CompletableFuture to handle the asynchronous response
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        // Reference to the messages in the chat in Realtime Database
        DatabaseReference chatMessagesRef = firebaseDatabase.getReference("chats")
                .child(chatId)
                .child("messages");

        // Initialize an empty list to hold the messages
        List<Map<String, Object>> messages = new ArrayList<>();

        // Use addListenerForSingleValueEvent to asynchronously retrieve the chat messages
        chatMessagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop through the snapshot to get individual messages
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> message = (Map<String, Object>) messageSnapshot.getValue();
                    message.put("id", messageSnapshot.getKey());
                    messages.add(message);
                }
                // Once the data is fetched, complete the CompletableFuture with the list of messages
                future.complete(messages);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // In case of error, complete the CompletableFuture exceptionally
                future.completeExceptionally(new Exception("Error fetching chat messages: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    
    public boolean doesChatExist(String userId, String counsellorId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = firestore.collection("chats")
                .whereEqualTo("userId", userId)
                .whereEqualTo("counsellorId", counsellorId)
                .get();

        // Check if any document matches the query
        return !query.get().getDocuments().isEmpty();
    }
}
