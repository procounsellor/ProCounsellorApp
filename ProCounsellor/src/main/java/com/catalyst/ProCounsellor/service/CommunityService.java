package com.catalyst.ProCounsellor.service;


import com.catalyst.ProCounsellor.model.feedingModel.Community;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    private static final String COLLECTION_NAME = "communities";

    public Community createCommunity(Community community) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        community.setCommunityId(docRef.getId()); // Auto-generated ID
        ApiFuture<WriteResult> future = docRef.set(community);
        future.get(); // wait for completion
        return community;
    }

    public Community getCommunityById(String communityId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(communityId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Community.class);
        } else {
            return null; // or throw custom exception if needed
        }
    }

    public List<Community> getAllCommunities() throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                        .map(doc -> doc.toObject(Community.class))
                        .collect(Collectors.toList());
    }

    public Community updateCommunity(String communityId, Community updatedCommunity) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(communityId);
        updatedCommunity.setCommunityId(communityId);
        ApiFuture<WriteResult> future = docRef.set(updatedCommunity);
        future.get(); // wait for completion
        return updatedCommunity;
    }

    public String deleteCommunity(String communityId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(communityId);
        ApiFuture<WriteResult> writeResult = docRef.delete();
        writeResult.get(); // wait for completion
        return "Community with ID " + communityId + " deleted successfully.";
    }
}
