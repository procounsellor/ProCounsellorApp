package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.StateType;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class CounsellorService {

    private static final String COUNSELLORS = "counsellors";

    // Signup functionality
    public String signup(Counsellor counsellor) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(COUNSELLORS).document(counsellor.getUserName());

        // Check if user already exists
        if (documentReference.get().get().exists()) {
            return "User already exists with ID: " + counsellor.getUserName();
        }

        // Save new user
        ApiFuture<WriteResult> collectionsApiFuture = documentReference.set(counsellor);
        return "Signup successful! User ID: " + counsellor.getUserName();
    }

    // Signin functionality
    public String signin(Counsellor counsellor) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentSnapshot documentSnapshot = dbFirestore.collection(COUNSELLORS)
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
            throw new UserNotFoundException("Counsellor not found for ID: " + counsellor.getUserName());
        }
    }
    
    public List<Counsellor> getAllCounsellors() {
        Firestore firestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> querySnapshot = firestore.collection(COUNSELLORS).get();

        List<Counsellor> counsellors = new ArrayList<>();
        try {
            for (QueryDocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            	Counsellor counsellor = doc.toObject(Counsellor.class);
            	counsellors.add(counsellor);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching all counsellors", e);
        }
        return counsellors;
    }
    
    public List<Counsellor> getAllCounsellorsSortedByRating() {
        Firestore firestore = FirestoreClient.getFirestore();
        Query query = firestore.collection(COUNSELLORS).orderBy("rating", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<Counsellor> counsellors = new ArrayList<>();
        try {
            for (QueryDocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            	Counsellor counsellor = doc.toObject(Counsellor.class);
            	counsellors.add(counsellor);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching counsellors sorted by rating", e);
        }
        return counsellors;
    }
    
    public List<Counsellor> getCounsellorsByState(StateType state) {
        Firestore firestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> querySnapshot = firestore.collection(COUNSELLORS)
                .whereEqualTo("state", state.name())
                .get();

        List<Counsellor> counsellors = new ArrayList<>();
        try {
            for (QueryDocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            	Counsellor counsellor = doc.toObject(Counsellor.class);
            	counsellors.add(counsellor);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching counsellors by state", e);
        }
        return counsellors;
    }
    
    public void updateUserPhotoUrl(String userId, String photoUrl) {
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection(COUNSELLORS).document(userId).update("photoUrl", photoUrl);
    }
}