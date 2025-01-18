package com.catalyst.ProCounsellor.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.dto.SendCounsellorReviews;
import com.catalyst.ProCounsellor.dto.SendUserReviews;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.model.UserReview;
import com.catalyst.ProCounsellor.model.UserReviewComments;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class ReviewService {
    Firestore firestore = FirestoreClient.getFirestore();
    
    private boolean doesDocumentExist(String collection, String documentId) throws InterruptedException, ExecutionException {
        DocumentReference ref = firestore.collection(collection).document(documentId);
        return ref.get().get().exists();
    }


    // Post a review from a user to a counsellor
    public void postReview(String userName, String counsellorName, UserReview review) throws Exception {   	
        String reviewId = UUID.randomUUID().toString();
        DocumentReference documentReference = firestore.collection("reviews").document(reviewId);
        review.setReviewId(reviewId);
        
        review.setUserName(userName);
        review.setCounsellorName(counsellorName);
        review.setTimestamp(Timestamp.now());
        review.setNoOfLikes(0);

        // First, check if both user and counsellor exist
        if (!doesDocumentExist("users", userName)) {
            throw new Exception("User with ID " + userName + " does not exist.");
        }

        if (!doesDocumentExist("counsellors", counsellorName)) {
            throw new Exception("Counsellor with ID " + counsellorName + " does not exist.");
        }
        
        // Save the review
        documentReference.set(review).get();

        // Add the review to both the user's and the counsellor's review lists
        addReviewIDToUser(userName, reviewId);
        addReviewIDToCounsellor(counsellorName, reviewId);
    }
    
    private void addReviewIDToUser(String userName, String reviewId) throws Exception {
        DocumentReference userRef = firestore.collection("users").document(userName);
        DocumentSnapshot userDoc = userRef.get().get();

        // Explicitly use GenericTypeIndicator for type-safe casting
        List<String> currentReviews = userDoc.exists() 
            ? (List<String>) userDoc.get("userReviewIds") 
            : new ArrayList<>();

        if (currentReviews == null) {
            currentReviews = new ArrayList<>();
        }

        currentReviews.add(reviewId);
        userRef.update("userReviewIds", currentReviews).get(); // Wait for the update to complete
    }
    
    private void addReviewIDToCounsellor(String counsellorName, String reviewId) throws Exception {
        DocumentReference counsellorRef = firestore.collection("counsellors").document(counsellorName);
        DocumentSnapshot counsellorDoc = counsellorRef.get().get();

        // Explicitly use GenericTypeIndicator for type-safe casting
        List<String> currentReviews = counsellorDoc.exists() 
            ? (List<String>) counsellorDoc.get("reviewIds") 
            : new ArrayList<>();

        if (currentReviews == null) {
            currentReviews = new ArrayList<>();
        }

        currentReviews.add(reviewId);
        counsellorRef.update("reviewIds", currentReviews).get(); // Wait for the update to complete
    }
    
    public void updateReview(String reviewId, UserReview updatedReview) throws Exception {
        DocumentReference reviewRef = firestore.collection("reviews").document(reviewId);
        DocumentSnapshot reviewDoc = reviewRef.get().get();

        if (!reviewDoc.exists()) {
            throw new Exception("Review with ID " + reviewId + " does not exist.");
        }

        // Prepare a map of the fields to update (without affecting likes and comments)
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("reviewText", updatedReview.getReviewText());  // Assuming updatedReview contains new review text
        updateFields.put("rating", updatedReview.getRating());          // Assuming updatedReview contains a rating

        // Update the review document without overwriting likes and comments
        reviewRef.update(updateFields).get(); 
    }
    
    // Post a review from a user to a counsellor
    public void deleteReview(String reviewId) throws Exception {
        DocumentReference reviewRef = firestore.collection("reviews").document(reviewId);
        DocumentSnapshot reviewDoc = reviewRef.get().get();
        UserReview review = getReviewFromReviewId(reviewId);
        String userName = review.getUserName();
        String counsellorName = review.getCounsellorName();

        if (!reviewDoc.exists()) {
            throw new Exception("Review with ID " + reviewId + " does not exist.");
        }

        // Delete the review from the reviews collection
        reviewRef.delete().get();

        // Remove the reviewId from the user's review list
        removeReviewIDFromUser(userName, reviewId);

        // Remove the reviewId from the counsellor's review list
        removeReviewIDFromCounsellor(counsellorName, reviewId);
    }
    
    private void removeReviewIDFromUser(String userName, String reviewId) throws Exception {
        DocumentReference userRef = firestore.collection("users").document(userName);
        DocumentSnapshot userDoc = userRef.get().get();

        if (!userDoc.exists()) {
            throw new Exception("User with ID " + userName + " does not exist.");
        }

        // Fetch current reviews and remove the reviewId
        List<String> currentReviews = userDoc.exists() ? (List<String>) userDoc.get("userReviewIds") : new ArrayList<>();
        currentReviews.remove(reviewId);

        userRef.update("userReviewIds", currentReviews).get(); // Wait for the update to complete
    }

    private void removeReviewIDFromCounsellor(String counsellorName, String reviewId) throws Exception {
        DocumentReference counsellorRef = firestore.collection("counsellors").document(counsellorName);
        DocumentSnapshot counsellorDoc = counsellorRef.get().get();

        if (!counsellorDoc.exists()) {
            throw new Exception("Counsellor with ID " + counsellorName + " does not exist.");
        }

        // Fetch current reviews and remove the reviewId
        List<String> currentReviews = counsellorDoc.exists() ? (List<String>) counsellorDoc.get("reviewIds") : new ArrayList<>();
        currentReviews.remove(reviewId);

        counsellorRef.update("reviewIds", currentReviews).get(); // Wait for the update to complete
    }

    // Fetch all reviews given by a specific user
    public List<SendUserReviews> getReviewsByUser(String userName) throws InterruptedException, ExecutionException {
        DocumentReference userRef = firestore.collection("users").document(userName);
        DocumentSnapshot document = userRef.get().get();

        if (document.exists()) {
            // Directly cast the result to List<String>
            List<String> reviewIds = (List<String>) document.get("userReviewIds");

            if (reviewIds == null) {
                return new ArrayList<>();
            }

            List<SendUserReviews> allReviews = new ArrayList<>();
            for (String reviewId : reviewIds) {
            	SendUserReviews review = getReviewByUserFromReviewId(reviewId);
                if (review != null) {
                    allReviews.add(review);
                }
            }
            return allReviews;
        }
        return new ArrayList<>();
    }

   


	// Fetch all reviews for a specific counselor
    public List<SendCounsellorReviews> getReviewsForCounsellor(String counsellorName) throws InterruptedException, ExecutionException {
        DocumentReference counsellorRef = firestore.collection("counsellors").document(counsellorName);
        DocumentSnapshot document = counsellorRef.get().get();

        if (document.exists()) {
            // Directly cast the result to List<String>
            List<String> reviewIds = (List<String>) document.get("reviewIds");

            if (reviewIds == null) {
                return new ArrayList<>();
            }

            List<SendCounsellorReviews> allReviews = new ArrayList<>();
            for (String reviewId : reviewIds) {
            	SendCounsellorReviews review = getReviewForCounsellorFromReviewId(reviewId);
                if (review != null) {
                    allReviews.add(review);
                }
            }
            return allReviews;
        }
        return new ArrayList<>();
    }
    
    public String getCounsellorFullNameFromUserName(String counsellorName) {
        try {
            DocumentSnapshot documentSnapshot = firestore.collection("counsellors")
                    .document(counsellorName)
                    .get()
                    .get();
            
            if (documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                
                if (firstName != null && lastName != null) {
                    return firstName + " " + lastName;
                } else {
                    return counsellorName;
                }
            } else {
                return "User not found";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving user details";
        }
    }
    
    public String getUserFullNameFromUserName(String userName) {
        try {
            DocumentSnapshot documentSnapshot = firestore.collection("users")
                    .document(userName)
                    .get()
                    .get();
            
            if (documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                
                if (firstName != null && lastName != null) {
                    return firstName + " " + lastName;
                } else {
                    return userName;
                }
            } else {
                return "Counsellor not found";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving Counsellor details";
        }
    }

    // Fetch a specific review from a user to a counselor
    public UserReview getReview(String userName, String counsellorName) throws InterruptedException, ExecutionException, Exception {
        // Reference the "reviews" collection in Firestore
        CollectionReference reviewsRef = firestore.collection("reviews");

        // Query for a review that matches the userName and counsellorName
        Query query = reviewsRef
                .whereEqualTo("userName", userName)
                .whereEqualTo("counsellorName", counsellorName);

        // Execute the query and get the results
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        // Ensure that only one review is returned
        if (documents.size() > 1) {
            throw new Exception("Multiple reviews found for the given user and counselor.");
        } else if (documents.isEmpty()) {
            throw new Exception("No review found for the given user and counselor.");
        }

        // Convert the document into a UserReview object
        QueryDocumentSnapshot document = documents.get(0);
        UserReview review = document.toObject(UserReview.class);

        return review;
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
        comment.setPhotoUrl(getUserPhotoUrl(userName));

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
    
    public String getUserPhotoUrl(String userName) throws InterruptedException, ExecutionException {
    	DocumentReference userRef = firestore.collection("users").document(userName);
        ApiFuture<DocumentSnapshot> future = userRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            User user = document.toObject(User.class);
            if (user != null) {
                return user.getPhoto();
            }
        }
        return null;
    }
    
    public String getCounsellorPhotoUrl(String counsellorName) throws InterruptedException, ExecutionException {
    	DocumentReference userRef = firestore.collection("counsellors").document(counsellorName);
        ApiFuture<DocumentSnapshot> future = userRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Counsellor counsellor = document.toObject(Counsellor.class);
            if (counsellor != null) {
                return counsellor.getPhotoUrl();
            }
        }
        return null;
    }
    
    private UserReview getReviewFromReviewId(String reviewId) throws InterruptedException, ExecutionException {
		DocumentSnapshot userReview = firestore.collection("reviews").document(reviewId).get().get();
		return userReview.exists() ? userReview.toObject(UserReview.class) : null;
	}
    
    private SendUserReviews getReviewByUserFromReviewId(String reviewId) throws InterruptedException, ExecutionException {
		DocumentSnapshot userReview = firestore.collection("reviews").document(reviewId).get().get();
		SendUserReviews sendUserReviews = new SendUserReviews();
		sendUserReviews.setReviewId(userReview.getString("reviewId"));
		sendUserReviews.setUserName(userReview.getString("userName"));
		String counsellorName = userReview.getString("counsellorName");
		sendUserReviews.setCounsellorName(counsellorName);
		String counsellorFullName = getCounsellorFullNameFromUserName(counsellorName);
		sendUserReviews.setCounsellorFullName(counsellorFullName);
		sendUserReviews.setCounsellorPhotoUrl(getCounsellorPhotoUrl(counsellorName));
		sendUserReviews.setReviewText(userReview.getString("reviewText"));
		sendUserReviews.setUserIdsLiked((List<String>) userReview.get("userIdsLiked"));
		sendUserReviews.setRating(userReview.getDouble("rating"));
		sendUserReviews.setTimestamp(userReview.getTimestamp("timestamp"));
		sendUserReviews.setNoOfLikes(userReview.getLong("noOfLikes").intValue());
		
		List<Map<String, Object>> listOfComments = (List<Map<String, Object>>) userReview.get("comments");
		
		if(listOfComments != null) {
			for (Map<String, Object> commentMap : listOfComments) {
			    String userNameOfComment = (String) commentMap.get("userName");
			    commentMap.put("userFullName", getUserFullNameFromUserName(userNameOfComment));
			    commentMap.put("photoUrl", getUserPhotoUrl(userNameOfComment));
			}
		}
        
		sendUserReviews.setComments(listOfComments);
		
		return sendUserReviews;
	}
    
    private SendCounsellorReviews getReviewForCounsellorFromReviewId(String reviewId) throws InterruptedException, ExecutionException {
		DocumentSnapshot userReview = firestore.collection("reviews").document(reviewId).get().get();
		SendCounsellorReviews sendCounsellorReviews = new SendCounsellorReviews();
		sendCounsellorReviews.setReviewId(userReview.getString("reviewId"));
		sendCounsellorReviews.setCounsellorName(userReview.getString("counsellorName"));
		String userName = userReview.getString("userName");
		sendCounsellorReviews.setUserName(userName);
		String userFullName = getUserFullNameFromUserName(userName);
		sendCounsellorReviews.setUserFullName(userFullName);
		sendCounsellorReviews.setUserPhotoUrl(getUserPhotoUrl(userName));
		sendCounsellorReviews.setReviewText(userReview.getString("reviewText"));
		sendCounsellorReviews.setUserIdsLiked((List<String>) userReview.get("userIdsLiked"));
		sendCounsellorReviews.setRating(userReview.getDouble("rating"));
		sendCounsellorReviews.setTimestamp(userReview.getTimestamp("timestamp"));
		sendCounsellorReviews.setNoOfLikes(userReview.getLong("noOfLikes").intValue());
		List<Map<String, Object>> listOfComments = (List<Map<String, Object>>) userReview.get("comments");
		
		if(listOfComments != null) {
			for (Map<String, Object> commentMap : listOfComments) {
			    String userNameOfComment = (String) commentMap.get("userName");
			    commentMap.put("userFullName", getUserFullNameFromUserName(userNameOfComment));
			    commentMap.put("photoUrl", getUserPhotoUrl(userNameOfComment));
			}
		}
        
		sendCounsellorReviews.setComments(listOfComments);
		
		return sendCounsellorReviews;
	}
}

