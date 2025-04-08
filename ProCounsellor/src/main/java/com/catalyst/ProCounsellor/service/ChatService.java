package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.dto.MessageRequest;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import com.google.cloud.firestore.DocumentSnapshot;


@Service
public class ChatService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private FirebaseDatabase firebaseDatabase; // This is for Realtime Database
    
    @Autowired
	private UserService userService;
    
    @Autowired
	private CounsellorService counsellorService;

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
        
        addChatIdToUser(userDocRef, newChatRef.getId(), userId, counsellorId);
        addChatIdToCounsellor(counsellorDocRef, newChatRef.getId(), counsellorId, userId);
        
        return newChatRef.getId();
    }
    
//    public String startChatUserToUser(String userId, String userId2) throws ExecutionException, InterruptedException {
//        // Step 1: Validate userId exists in the users table (Firestore)
//        DocumentReference userDocRef = firestore.collection("users").document(userId);
//        DocumentSnapshot userSnapshot = userDocRef.get().get();
//        if (!userSnapshot.exists()) {
//            throw new IllegalArgumentException("Invalid userId: User does not exist.");
//        }
//
//        // Step 2: Validate use2 exists in the counsellors table (Firestore)
//        DocumentReference user2DocRef = firestore.collection("users").document(userId2);
//        DocumentSnapshot user2Snapshot = user2DocRef.get().get();
//        if (!user2Snapshot.exists()) {
//            throw new IllegalArgumentException("Invalid user: user does not exist.");
//        }
//
//        // Step 3: Check if a chat already exists between the user and counselor (Firestore)
//        ApiFuture<QuerySnapshot> existingChatQuery = firestore.collection("chats")
//                .whereEqualTo("userId", userId)
//                .whereEqualTo("userId2", userId2)
//                .get();
//
//        QuerySnapshot querySnapshot = existingChatQuery.get();
//        if (!querySnapshot.isEmpty()) {
//            // A chat already exists, return the existing chatId
//            return querySnapshot.getDocuments().get(0).getId();
//        }
//
//        // Step 4: Create a new chat if it does not exist (Firestore)
//        Map<String, Object> chatData = new HashMap<>();
//        chatData.put("userId", userId);
//        chatData.put("userId2", userId2);
//        chatData.put("createdAt", FieldValue.serverTimestamp());
//
//        DocumentReference newChatRef = firestore.collection("chats").document();
//        newChatRef.set(chatData).get();
//        
//        addChatIdToUser(userDocRef, newChatRef.getId(), userId, userId2);
//        addChatIdToUser(user2DocRef, newChatRef.getId(), userId2, userId);
//        
//        return newChatRef.getId();
//    }
    
    public String startChatUserToUser(String userId, String userId2) throws ExecutionException, InterruptedException {
        DocumentReference userDocRef = firestore.collection("users").document(userId);
        DocumentSnapshot userSnapshot = userDocRef.get().get();
        if (!userSnapshot.exists()) {
            throw new IllegalArgumentException("Invalid userId: User does not exist.");
        }

        DocumentReference user2DocRef = firestore.collection("users").document(userId2);
        DocumentSnapshot user2Snapshot = user2DocRef.get().get();
        if (!user2Snapshot.exists()) {
            throw new IllegalArgumentException("Invalid user: userId2 does not exist.");
        }

        // ✅ Step 3: Use sorted participants list to check existing chats
        List<String> participants = Arrays.asList(userId, userId2);
        Collections.sort(participants);

        ApiFuture<QuerySnapshot> existingChatQuery = firestore.collection("chats")
            .whereEqualTo("participants", participants)
            .get();

        QuerySnapshot querySnapshot = existingChatQuery.get();
        if (!querySnapshot.isEmpty()) {
            return querySnapshot.getDocuments().get(0).getId();
        }

        // ✅ Step 4: Create new chat
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("userId", userId);
        chatData.put("userId2", userId2);
        chatData.put("participants", participants); // ✅ Added sorted participant list
        chatData.put("createdAt", FieldValue.serverTimestamp());

        DocumentReference newChatRef = firestore.collection("chats").document();
        newChatRef.set(chatData).get();

        addChatIdToUser(userDocRef, newChatRef.getId(), userId, userId2);
        addChatIdToUser(user2DocRef, newChatRef.getId(), userId2, userId);

        return newChatRef.getId();
    }


    private void addChatIdToCounsellor(DocumentReference counsellorDocRef, String newChatId, String user1, String user2) throws InterruptedException, ExecutionException {
    	ApiFuture<DocumentSnapshot> future = counsellorDocRef.get();
        DocumentSnapshot document = future.get();
       
        if (document.exists()) {
            // Retrieve the existing chatIdsCreatedForCounsellor list
        	List<Map<String, String>> chatIds;

            if (document.exists()) {
                // Retrieve existing list or initialize if null
                chatIds = (List<Map<String, String>>) document.get("chatIdsCreatedForCounsellor");
                if (chatIds == null) {
                    chatIds = new ArrayList<>();
                }
            } else {
                // If document doesn't exist, initialize list
                chatIds = new ArrayList<>();
            }
            
            // Add the new chat ID if it does not already exist
            if (!chatIds.contains(newChatId)) {
            	Map<String, String> m = new HashMap<>();
            	m.put("chatId",newChatId );
            	m.put("user1", user1);
            	m.put("user2", user2);
                chatIds.add(m);

                // Update Firestore document with the new list
                ApiFuture<WriteResult> writeResult = counsellorDocRef.update("chatIdsCreatedForCounsellor", chatIds);
                writeResult.get();  // Wait for the update to complete

                System.out.println("Chat ID added successfully: " + newChatId);
            } else {
                System.out.println("Chat ID already exists: " + newChatId);
            }
        }
	}

	private void addChatIdToUser(DocumentReference userDocRef, String newChatId, String user1, String user2) throws InterruptedException, ExecutionException {
		ApiFuture<DocumentSnapshot> future = userDocRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            // Retrieve the existing chatIdsCreatedForCounsellor list
        	List<Map<String, String>> chatIds;

            if (document.exists()) {
                // Retrieve existing list or initialize if null
                chatIds = (List<Map<String, String>>) document.get("chatIdsCreatedForUser");
                if (chatIds == null) {
                    chatIds = new ArrayList<>();
                }
            } else {
                // If document doesn't exist, initialize list
                chatIds = new ArrayList<>();
            }

            // Add the new chat ID if it does not already exist
            if (!chatIds.contains(newChatId)) {
            	Map<String, String> m = new HashMap<>();
            	m.put("chatId",newChatId );
            	m.put("user1", user1);
            	m.put("user2", user2);
                chatIds.add(m);

                // Update Firestore document with the new list
                ApiFuture<WriteResult> writeResult = userDocRef.update("chatIdsCreatedForUser", chatIds);
                writeResult.get();  // Wait for the update to complete

                System.out.println("Chat ID added successfully: " + newChatId);
            } else {
                System.out.println("Chat ID already exists: " + newChatId);
            }
        }
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
        
        
        String storedUserId2 = chatSnapshot.getString("userId2");

        // Validate that the senderId is either the userId or counselorId
        if (!messageRequest.getSenderId().equals(storedUserId) && !messageRequest.getSenderId().equals(storedCounselorId) &&!messageRequest.getSenderId().equals(storedUserId2)) {
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
        messageData.put("isSeen", false);
        messageData.put("timestamp", System.currentTimeMillis());

        chatMessagesRef.child(messageId).setValueAsync(messageData);
    }

    public String sendFileMessage(String chatId, String senderId, MultipartFile file) throws Exception {
        // Fetch chat details to verify sender
        DocumentSnapshot chatSnapshot = firestore.collection("chats").document(chatId).get().get();

        if (!chatSnapshot.exists()) {
            throw new IllegalArgumentException("Chat not found.");
        }

        String storedUserId = chatSnapshot.getString("userId");
        String storedCounselorId = chatSnapshot.getString("counsellorId");
        String storedUserId2 = chatSnapshot.getString("userId2");
        // Validate sender
        if (!senderId.equals(storedUserId) && !senderId.equals(storedCounselorId) &&!senderId.equals(storedUserId2)) {
            throw new IllegalAccessException("You are not authorized to send a file in this chat.");
        }

        // Upload file to Firebase Storage
        String fileName = "chats/" + chatId + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Bucket bucket = StorageClient.getInstance().bucket("procounsellor-71824.firebasestorage.app");
        Blob blob = bucket.create(fileName, file.getInputStream(), file.getContentType());
        
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));//Make URL publicly accessible.

        String fileUrl = "https://storage.googleapis.com/" + bucket.getName() + "/" + fileName;

        // Save message in Firebase Realtime Database
        DatabaseReference chatMessagesRef = firebaseDatabase.getReference("chats").child(chatId).child("messages");
        String messageId = chatMessagesRef.push().getKey();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("fileUrl", fileUrl);
        messageData.put("fileName", file.getOriginalFilename());
        messageData.put("fileType", file.getContentType());
        messageData.put("isSeen", false);
        messageData.put("timestamp", System.currentTimeMillis());

        chatMessagesRef.child(messageId).setValueAsync(messageData);

        return fileUrl;
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
    
    public boolean doesChatExistUserToUser(String userId, String userId2) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = firestore.collection("chats")
                .whereEqualTo("userId", userId)
                .whereEqualTo("userId2", userId2)
                .get();

        // Check if any document matches the query
        return !query.get().getDocuments() .isEmpty();
    }
    
    public List<Counsellor> getCounsellorsForUser(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = firestore.collection("chats")
                .whereEqualTo("userId", userId)
                .get();

        QuerySnapshot querySnapshot = query.get();
        List<Counsellor> counsellors = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            // Extract counsellorId from each chat document
        	Counsellor counsellor = counsellorService.getCounsellorById(document.getString("counsellorId"));
            if (counsellor != null) {
            	counsellors.add(counsellor);
            }
        }
        return counsellors;
    }

    public List<User> getUsersForCounsellor(String counsellorId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = firestore.collection("chats")
                .whereEqualTo("counsellorId", counsellorId)
                .get();

        QuerySnapshot querySnapshot = query.get();
        List<User> users = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            // Extract userId from each chat document
            User user = userService.getUserById(document.getString("userId"));
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }
    
    public CompletableFuture<Boolean> isMessageSeen(String chatId, String messageId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Reference to the specific message in Firebase Realtime Database
        DatabaseReference messageRef = firebaseDatabase.getReference("chats")
                .child(chatId)
                .child("messages")
                .child(messageId);

        // Fetch the message data asynchronously
        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isSeen = dataSnapshot.child("isSeen").getValue(Boolean.class);
                    future.complete(isSeen != null && isSeen); // Return true if isSeen is true
                } else {
                    future.completeExceptionally(new IllegalArgumentException("Message not found."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new Exception("Error fetching message: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    
    public CompletableFuture<Void> markMessageAsSeen(String chatId, String messageId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Reference to the specific message in Firebase Realtime Database
        DatabaseReference messageRef = firebaseDatabase.getReference("chats")
                .child(chatId)
                .child("messages")
                .child(messageId);

        // Update the isSeen field to true
        messageRef.child("isSeen").setValue(true, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(new Exception("Failed to update isSeen: " + error.getMessage()));
            } else {
                future.complete(null);
            }
        });

        return future;
    }

}
