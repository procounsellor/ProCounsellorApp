package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class CounsellorAuthService {

    private static final String COLLECTION_NAME = "counsellors";

    // Signup functionality
    public String signup(Counsellor user) throws ExecutionException, InterruptedException {
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
    public String signin(Counsellor counsellor) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentSnapshot documentSnapshot = dbFirestore.collection(COLLECTION_NAME)
                                                        .document(counsellor.getUserName())
                                                        .get()
                                                        .get();

        if (documentSnapshot.exists()) {
            Counsellor existingCounsellor = documentSnapshot.toObject(Counsellor.class);
            if (existingCounsellor.getPassword().equals(counsellor.getPassword())) {
                return "Signin successful for User ID: " + counsellor.getUserName();
            } else {
                throw new InvalidCredentialsException("Invalid credentials provided.");
            }
        } else {
            throw new UserNotFoundException("Counsellor not found for User ID: " + counsellor.getUserName());
        }
    }
}
