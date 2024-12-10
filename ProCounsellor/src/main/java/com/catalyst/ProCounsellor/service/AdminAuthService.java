package com.catalyst.ProCounsellor.service;


import com.catalyst.ProCounsellor.model.Admin;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class AdminAuthService {

    private static final String COLLECTION_NAME = "admins";

    // Signup functionality
    public String signup(Admin user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(user.getUserName());

        // Check if user already exists
        if (documentReference.get().get().exists()) {
            return "User already exists with ID: " + user.getUserName();
        }

        // Save new user
        ApiFuture<WriteResult> collectionsApiFuture = documentReference.set(user);
        return "Signup successful! User ID: " + user.getUserName();
    }

    // Signin functionality
    public String signin(Admin user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(user.getUserName());

        DocumentSnapshot documentSnapshot = documentReference.get().get();
        if (documentSnapshot.exists()) {
            Admin existingUser = documentSnapshot.toObject(Admin.class);
            if (existingUser.getUserName().equals(user.getUserName()) && existingUser.getPassword().equals(user.getPassword())) {
                return "Signin successful for User ID: " + user.getUserName();
            } else {
                return "Invalid credentials!";
            }
        } else {
            return "User not found!";
        }
    }
}
