package com.catalyst.ProCounsellor.service;


import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Admin;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class AdminService {

    private static final String ADMINS = "admins";

    // Signup functionality
    public String signup(Admin user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(ADMINS).document(user.getUserName());

        // Check if user already exists
        if (documentReference.get().get().exists()) {
            return "User already exists with ID: " + user.getUserName();
        }

        // Save new user
        ApiFuture<WriteResult> collectionsApiFuture = documentReference.set(user);
        return "Signup successful! User ID: " + user.getUserName();
    }

    // Signin functionality
    public String signin(Admin admin) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentSnapshot documentSnapshot = dbFirestore.collection(ADMINS)
                                                        .document(admin.getUserName())
                                                        .get()
                                                        .get();

        if (documentSnapshot.exists()) {
            Admin existingAdmin = documentSnapshot.toObject(Admin.class);
            if (existingAdmin.getPassword().equals(admin.getPassword())) {
                return "Signin successful for User ID: " + admin.getUserName();
            } else {
                throw new InvalidCredentialsException("Invalid credentials provided.");
            }
        } else {
            throw new UserNotFoundException("Admin not found for User ID: " + admin.getUserName());
        }
    }
}
