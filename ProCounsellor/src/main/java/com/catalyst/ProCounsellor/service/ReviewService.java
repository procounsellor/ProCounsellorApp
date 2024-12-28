package com.catalyst.ProCounsellor.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.model.UserReview;
import com.catalyst.ProCounsellor.model.UserReviewComments;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class ReviewService {

    @Autowired
    private FirebaseService firebaseService;
    
    Firestore firestore = FirestoreClient.getFirestore();


    // Post a review from a user to a counsellor
    public void postReview(String userName, String counsellorName, UserReview userReview) throws Exception {
            firebaseService.postReview(userName, counsellorName, userReview);
    }
    
    public void updateReview(String reviewId, UserReview updatedReview) throws Exception {
    	firebaseService.updateReview(reviewId, updatedReview);
	}
    
    // Post a review from a user to a counsellor
    public void deleteReview(String reviewId) throws Exception {
            firebaseService.deleteReview(reviewId);
    }

    // Fetch all reviews given by a specific user
    public List<UserReview> getReviewsByUser(String userName) throws InterruptedException, ExecutionException {
        return firebaseService.getUserReviews(userName);
    }

    // Fetch all reviews for a specific counselor
    public List<UserReview> getReviewsForCounsellor(String counsellorName) throws InterruptedException, ExecutionException {
        return firebaseService.getCounsellorReviews(counsellorName);
    }
    
    public String getCounsellorFullNameFromUserName(String counsellorName) {
    	return firebaseService.getCounsellorFullNameFromUserName(counsellorName);
    }
    
    public String getUserFullNameFromUserName(String userName) {
    	return firebaseService.getUserFullNameFromUserName(userName);
    }

    // Fetch a specific review from a user to a counselor
    public UserReview getReview(String userName, String counsellorName) throws Exception {
        return firebaseService.getSpecificReview(userName, counsellorName);
    }
    
    public void likeReview(String reviewId, String userId) throws ExecutionException, InterruptedException, Exception {
        DocumentReference reviewRef = firestore.collection("reviews").document(reviewId);
        DocumentSnapshot document = reviewRef.get().get();

        if (!document.exists()) {
            throw new Exception("Review with ID " + reviewId + " does not exist.");
        }

        // Fetch current List of userIdsLiked
        List<String> userIdsLiked = (List<String>) document.get("userIdsLiked");
        if (userIdsLiked == null) {
            userIdsLiked = new ArrayList<>();
        }

        // Only add userId if not already in the List
        if (!userIdsLiked.contains(userId)) {
            userIdsLiked.add(userId);
            reviewRef.update("userIdsLiked", userIdsLiked);

            // Update the noOfLikes field based on the size of userIdsLiked list
            reviewRef.update("noOfLikes", userIdsLiked.size());
        }
    }

    public void unlikeReview(String reviewId, String userId) throws ExecutionException, InterruptedException, Exception {
        DocumentReference reviewRef = firestore.collection("reviews").document(reviewId);
        DocumentSnapshot document = reviewRef.get().get();

        if (!document.exists()) {
            throw new Exception("Review with ID " + reviewId + " does not exist.");
        }

        // Fetch current List of userIdsLiked
        List<String> userIdsLiked = (List<String>) document.get("userIdsLiked");
        if (userIdsLiked == null || !userIdsLiked.contains(userId)) {
            throw new Exception("Review with ID " + reviewId + " does not have this user liked.");
        }

        // Remove the userId from the List
        userIdsLiked.remove(userId);
        reviewRef.update("userIdsLiked", userIdsLiked);

        // Update the noOfLikes field based on the size of userIdsLiked list
        reviewRef.update("noOfLikes", userIdsLiked.size());
    }

    public Integer getReviewLikes(String reviewId) throws ExecutionException, InterruptedException, Exception {
        DocumentReference reviewRef = firestore.collection("reviews").document(reviewId);
        DocumentSnapshot document = reviewRef.get().get();

        if (!document.exists()) {
            throw new Exception("Review with ID " + reviewId + " does not exist.");
        }

        List<String> userIdsLiked = (List<String>) document.get("userIdsLiked");
        return (userIdsLiked != null) ? userIdsLiked.size() : 0;
    }
    
    // Method to add a comment to a review
    public void addComment(String reviewId, String userName, UserReviewComments comment) throws ExecutionException, InterruptedException, Exception {
        DocumentReference reviewRef = firestore.collection("reviews").document(reviewId);
        DocumentSnapshot reviewSnapshot = reviewRef.get().get();

        if (!reviewSnapshot.exists()) {
            throw new Exception("Review with ID " + reviewId + " does not exist.");
        }

        List<UserReviewComments> comments = reviewSnapshot.toObject(UserReview.class).getComments();
        if (comments == null) {
            comments = new ArrayList<>();
        }

        comment.setUserReviewCommentId(UUID.randomUUID().toString());
        comment.setTimestamp(Timestamp.now());
        comment.setUserName(userName);
        comment.setPhotoUrl(firebaseService.getUserPhotoUrl(userName));

        comments.add(comment);
        reviewRef.update("comments", comments);
    }
    
    public void updateComment(String reviewId, String commentId, UserReviewComments updatedComment) throws Exception {
        DocumentReference reviewRef = firestore.collection("reviews").document(reviewId);
        DocumentSnapshot reviewSnapshot = reviewRef.get().get();

        if (!reviewSnapshot.exists()) {
            throw new Exception("Review with ID " + reviewId + " does not exist.");
        }

        List<UserReviewComments> comments = reviewSnapshot.toObject(UserReview.class).getComments();
        if (comments == null) {
            throw new Exception("No comments found for review with ID " + reviewId);
        }

        // Find the comment and update it
        boolean commentUpdated = false;
        for (UserReviewComments comment : comments) {
            if (comment.getUserReviewCommentId().equals(commentId)) {
                comment.setCommentText(updatedComment.getCommentText());
                comment.setTimestamp(Timestamp.now());
                commentUpdated = true;
                break;
            }
        }

        if (!commentUpdated) {
            throw new Exception("Comment with ID " + commentId + " not found in review.");
        }

        reviewRef.update("comments", comments).get(); // Update the comments array
    }
    
    public void deleteComment(String reviewId, String commentId) throws ExecutionException, InterruptedException, Exception {
        DocumentReference reviewRef = firestore.collection("reviews").document(reviewId);
        DocumentSnapshot reviewSnapshot = reviewRef.get().get();

        if (!reviewSnapshot.exists()) {
            throw new Exception("Review with ID " + reviewId + " does not exist.");
        }

        // Retrieve the list of comments for the review
        List<UserReviewComments> comments = reviewSnapshot.toObject(UserReview.class).getComments();
        if (comments == null) {
            throw new Exception("No comments found for review with ID " + reviewId);
        }

        // Find the comment by userReviewCommentId and remove it
        UserReviewComments commentToDelete = null;
        for (UserReviewComments comment : comments) {
            if (comment.getUserReviewCommentId().equals(commentId)) {
                commentToDelete = comment;
                break;
            }
        }

        if (commentToDelete == null) {
            throw new Exception("Comment with ID " + commentId + " not found.");
        }

        // Remove the comment from the list
        comments.remove(commentToDelete);

        // Update the review document with the modified list of comments
        reviewRef.update("comments", comments);
    }

    // Method to fetch all comments of a review
    public List<UserReviewComments> getComments(String reviewId) throws ExecutionException, InterruptedException, Exception {
        DocumentReference reviewRef = firestore.collection("reviews").document(reviewId);
        DocumentSnapshot reviewSnapshot = reviewRef.get().get();

        if (!reviewSnapshot.exists()) {
            throw new Exception("Review with ID " + reviewId + " does not exist.");
        }

        UserReview review = reviewSnapshot.toObject(UserReview.class);
        return review.getComments() != null ? review.getComments() : new ArrayList<>();
    }

	
}

