package com.catalyst.ProCounsellor.service;


import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Service
public class UserService {

    private static final String USERS = "users";

    // Signup functionality
    public String signup(User user) throws ExecutionException, InterruptedException {
    	System.out.println(user.getFirstName());
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(USERS).document(user.getUserName());

        // Check if user already exists
        if (documentReference.get().get().exists()) {
            return "User already exists with ID: " + user.getUserName();
        }

        // Save new user
        ApiFuture<WriteResult> collectionsApiFuture = documentReference.set(user);
        return "Signup successful! User ID: " + user.getUserName();
    }

    // Signin functionality
    public String signin(User user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentSnapshot documentSnapshot = dbFirestore.collection(USERS)
                                                        .document(user.getUserName())
                                                        .get()
                                                        .get();

        if (documentSnapshot.exists()) {
            User existingUser = documentSnapshot.toObject(User.class);
            if (existingUser.getPassword().equals(user.getPassword())) {
                return "Signin successful for User ID: " + user.getUserName();
            } else {
                throw new InvalidCredentialsException("Invalid credentials provided.");
            }
        } else {
            throw new UserNotFoundException("User not found for User ID: " + user.getUserName());
        }
    }
}
