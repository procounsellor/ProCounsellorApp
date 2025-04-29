package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.feedingModel.CommunityPost;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CommunityPostService {

    private static final String POST_COLLECTION = "communityPosts";

    public CommunityPost createPost(CommunityPost post) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(POST_COLLECTION).document();
        post.setPostId(docRef.getId());
        ApiFuture<WriteResult> future = docRef.set(post);
        future.get();
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
