package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.dto.MessageRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {

    @Autowired
    private Firestore firestore;

    public String startChat(String userId, String counsellorId) throws ExecutionException, InterruptedException {
        // Step 1: Validate userId exists in the users table
        DocumentReference userDocRef = firestore.collection("users").document(userId);
        DocumentSnapshot userSnapshot = userDocRef.get().get();
        if (!userSnapshot.exists()) {
            throw new IllegalArgumentException("Invalid userId: User does not exist.");
        }

        // Step 2: Validate counsellorId exists in the counsellors table
        DocumentReference counsellorDocRef = firestore.collection("counsellors").document(counsellorId);
        DocumentSnapshot counsellorSnapshot = counsellorDocRef.get().get();
        if (!counsellorSnapshot.exists()) {
            throw new IllegalArgumentException("Invalid counsellorId: Counsellor does not exist.");
        }

        // Step 3: Check if a chat already exists between the user and counselor based on userId and counselorId
        ApiFuture<QuerySnapshot> existingChatQuery = firestore.collection("chats")
                .whereEqualTo("userId", userId)
                .whereEqualTo("counsellorId", counsellorId)
                .get();

        QuerySnapshot querySnapshot = existingChatQuery.get();
        if (!querySnapshot.isEmpty()) {
            // A chat already exists, return the existing chatId
            return querySnapshot.getDocuments().get(0).getId();
        }

        // Step 4: Create a new chat if it does not exist
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("userId", userId);
        chatData.put("counsellorId", counsellorId);
        chatData.put("createdAt", FieldValue.serverTimestamp());

        DocumentReference newChatRef = firestore.collection("chats").document();
        newChatRef.set(chatData).get();

        return newChatRef.getId();
    }

    public void sendMessage(String chatId, MessageRequest messageRequest) throws ExecutionException, InterruptedException, IllegalAccessException {
        // Fetch the chat document to validate the user and counselor IDs
        DocumentSnapshot chatSnapshot = firestore.collection("chats").document(chatId).get().get();

        if (!chatSnapshot.exists()) {
            throw new IllegalArgumentException("Chat not found.");
        }

        // Get the userId and counselorId from the chat document
        String storedUserId = chatSnapshot.getString("userId");
        String storedCounselorId = chatSnapshot.getString("counsellorId");

        // Validate that the senderId is either the userId or counselorId
        if (!messageRequest.getSenderId().equals(storedUserId) && !messageRequest.getSenderId().equals(storedCounselorId)) {
            throw new IllegalAccessException("You are not authorized to send a message in this chat.");
        }

        // If validation passes, save the message
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", messageRequest.getSenderId());
        message.put("text", messageRequest.getText());
        message.put("timestamp", FieldValue.serverTimestamp());

        // Save the message to the chat's message collection
        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .get();
    }

    public Iterable<Map<String, Object>> getChatMessages(String chatId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .get();

        List<Map<String, Object>> messages = new ArrayList<>();
        for (DocumentSnapshot document : query.get().getDocuments()) {
            Map<String, Object> message = document.getData();
            message.put("id", document.getId());
            messages.add(message);
        }

        return messages;
    }
}
