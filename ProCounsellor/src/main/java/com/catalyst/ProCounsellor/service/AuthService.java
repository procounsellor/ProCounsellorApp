package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class AuthService {

    private static final String COLLECTION_NAME = "users";

    // Signup functionality
    public String signup(User user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(user.getUserId());

        // Check if user already exists
        if (documentReference.get().get().exists()) {
            return "User already exists with ID: " + user.getUserId();
        }

        // Save new user
        ApiFuture<WriteResult> collectionsApiFuture = documentReference.set(user);
        return "Signup successful! User ID: " + user.getUserId();
    }

    // Signin functionality
    public String signin(User user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(user.getUserId());

        DocumentSnapshot documentSnapshot = documentReference.get().get();
        if (documentSnapshot.exists()) {
            User existingUser = documentSnapshot.toObject(User.class);
            if (existingUser.getUserId().equals(user.getUserId()) && existingUser.getPassword().equals(user.getPassword())) {
                return "Signin successful for User ID: " + user.getUserId();
            } else {
                return "Invalid credentials!";
            }
        } else {
            return "User not found!";
        }
    }
}
