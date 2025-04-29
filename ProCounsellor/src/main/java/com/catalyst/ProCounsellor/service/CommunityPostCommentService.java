package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.feedingModel.CommunityPost;
import com.catalyst.ProCounsellor.model.feedingModel.CommunityPostComment;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
public class CommunityPostCommentService {

    private static final String POST_COLLECTION = "communityPosts";

    public CommunityPostComment addComment(CommunityPostComment comment) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference postRef = firestore.collection(POST_COLLECTION).document(comment.getPostId());

        DocumentSnapshot postSnapshot = postRef.get().get();
        if (!postSnapshot.exists()) {
            throw new RuntimeException("Post with ID " + comment.getPostId() + " not found.");
        }

        CommunityPost post = postSnapshot.toObject(CommunityPost.class);

        if (post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }
        
        comment.setTimestamp(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        post.getComments().add(comment);

        postRef.set(post).get();

        return comment;
    }

    public List<CommunityPostComment> getCommentsByPostId(String postId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference postRef = firestore.collection(POST_COLLECTION).document(postId);
        DocumentSnapshot postSnapshot = postRef.get().get();

        if (!postSnapshot.exists()) {
            throw new RuntimeException("Post with ID " + postId + " not found.");
        }

        CommunityPost post = postSnapshot.toObject(CommunityPost.class);

        return post.getComments() != null ? post.getComments() : new ArrayList<>();
    }

    public String deleteComment(String postId, int commentIndex) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference postRef = firestore.collection(POST_COLLECTION).document(postId);
        DocumentSnapshot postSnapshot = postRef.get().get();

        if (!postSnapshot.exists()) {
            throw new RuntimeException("Post with ID " + postId + " not found.");
        }

        CommunityPost post = postSnapshot.toObject(CommunityPost.class);

        if (post.getComments() == null || commentIndex < 0 || commentIndex >= post.getComments().size()) {
            throw new RuntimeException("Invalid comment index.");
        }

        post.getComments().remove(commentIndex); // remove by index
        postRef.set(post).get();

        return "Comment deleted successfully.";
    }
}

