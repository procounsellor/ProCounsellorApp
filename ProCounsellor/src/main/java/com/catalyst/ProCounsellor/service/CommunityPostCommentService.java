package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.feedingModel.CommunityPostComment;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CommunityPostCommentService {

    private static final String COMMENT_COLLECTION = "communityPostComments";

    public CommunityPostComment createComment(CommunityPostComment comment) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COMMENT_COLLECTION).document();
        comment.setCommunityPostCommentId(docRef.getId()); // Auto-generate comment ID
        ApiFuture<WriteResult> future = docRef.set(comment);
        future.get();
        return comment;
    }

    public CommunityPostComment getCommentById(String commentId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COMMENT_COLLECTION).document(commentId);
        DocumentSnapshot document = docRef.get().get();
        return document.exists() ? document.toObject(CommunityPostComment.class) : null;
    }

    public List<CommunityPostComment> getCommentsByPostId(String postId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        Query query = firestore.collection(COMMENT_COLLECTION).whereEqualTo("postId", postId);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
        return documents.stream().map(doc -> doc.toObject(CommunityPostComment.class)).collect(Collectors.toList());
    }

    public CommunityPostComment updateComment(String commentId, CommunityPostComment updatedComment) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        updatedComment.setCommunityPostCommentId(commentId);
        firestore.collection(COMMENT_COLLECTION).document(commentId).set(updatedComment).get();
        return updatedComment;
    }

    public String deleteComment(String commentId) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection(COMMENT_COLLECTION).document(commentId).delete().get();
        return "Comment with ID " + commentId + " deleted successfully.";
    }
}
