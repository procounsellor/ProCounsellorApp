package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.feedingModel.Community;
import com.catalyst.ProCounsellor.model.feedingModel.CommunityPost;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CommunityPostService {

    private static final String POST_COLLECTION = "communityPosts";

    public CommunityPost createPost(CommunityPost post) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        
        // Step 1: Create Post
        DocumentReference postDocRef = firestore.collection("communityPosts").document();
        post.setPostId(postDocRef.getId());
        post.setTimestamp(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        postDocRef.set(post).get(); // Save post

        // Step 2: Update Community with new Post
        DocumentReference communityDocRef = firestore.collection("communities").document(post.getCommunityId());
        DocumentSnapshot communitySnapshot = communityDocRef.get().get();
        
        if (communitySnapshot.exists()) {
            Community community = communitySnapshot.toObject(Community.class);

            // Safety check for null list
            if (community.getListOfPostIdInCommunity() == null) {
                community.setListOfPostIdInCommunity(new ArrayList<>());
            }

            community.getListOfPostIdInCommunity().add(post.getPostId());

            communityDocRef.set(community).get();
        } else {
            throw new RuntimeException("Community with ID " + post.getCommunityId() + " not found.");
        }

        return post;
    }

    public CommunityPost getPostById(String postId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(POST_COLLECTION).document(postId);
        DocumentSnapshot document = docRef.get().get();
        return document.exists() ? document.toObject(CommunityPost.class) : null;
    }

    public List<CommunityPost> getAllPosts() throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> documents = firestore.collection(POST_COLLECTION).get().get().getDocuments();
        return documents.stream().map(doc -> doc.toObject(CommunityPost.class)).collect(Collectors.toList());
    }

    public CommunityPost updatePost(String postId, CommunityPost updatedPost) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        updatedPost.setPostId(postId);
        firestore.collection(POST_COLLECTION).document(postId).set(updatedPost).get();
        return updatedPost;
    }

    public String deletePost(String postId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection(POST_COLLECTION).document(postId).delete().get();
        return "Post with ID " + postId + " deleted successfully.";
    }
}
