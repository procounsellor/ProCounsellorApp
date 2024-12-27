package com.catalyst.ProCounsellor.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.model.UserReview;
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
public class FirebaseService {
	
	Firestore firestore = FirestoreClient.getFirestore();
	
	 public User getUserById(String userId) throws ExecutionException, InterruptedException {
	        DocumentSnapshot snapshot = firestore.collection("users").document(userId).get().get();
	        return snapshot.exists() ? snapshot.toObject(User.class) : null;
	    }

	    public void updateUser(User user) throws ExecutionException, InterruptedException {
	        firestore.collection("users").document(user.getUserName()).set(user).get();
	    }
	    
	    public Counsellor getCounsellorById(String counsellorId) throws ExecutionException, InterruptedException {
	        DocumentSnapshot snapshot = firestore.collection("counsellors").document(counsellorId).get().get();
	        return snapshot.exists() ? snapshot.toObject(Counsellor.class) : null;
	    }

	    public void updateCounsellor(Counsellor counsellor) throws ExecutionException, InterruptedException {
	        firestore.collection("counsellors").document(counsellor.getUserName()).set(counsellor).get();
	    }
	    
	    private boolean doesDocumentExist(String collection, String documentId) throws InterruptedException, ExecutionException {
	        DocumentReference ref = firestore.collection(collection).document(documentId);
	        return ref.get().get().exists();
	    }
	    
	    public void postReview(String userName, String counsellorName, UserReview review) throws Exception {   	
	        String reviewId = UUID.randomUUID().toString();
	        DocumentReference documentReference = firestore.collection("reviews").document(reviewId);
	        review.setReviewId(reviewId);
	        
	        review.setUserName(userName);
	        String photoUrl = getUserPhotoUrl(userName);
	        review.setPhotoUrl(photoUrl);
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
	    
	    public List<UserReview> getUserReviews(String userName) throws InterruptedException, ExecutionException {
	        DocumentReference userRef = firestore.collection("users").document(userName);
	        DocumentSnapshot document = userRef.get().get();

	        if (document.exists()) {
	            // Directly cast the result to List<String>
	            List<String> reviewIds = (List<String>) document.get("userReviewIds");

	            if (reviewIds == null) {
	                return new ArrayList<>();
	            }

	            List<UserReview> allReviews = new ArrayList<>();
	            for (String reviewId : reviewIds) {
	                UserReview review = getReviewFromReviewId(reviewId);
	                if (review != null) {
	                    allReviews.add(review);
	                }
	            }
	            return allReviews;
	        }
	        return new ArrayList<>();
	    }
	    
	    public List<UserReview> getCounsellorReviews(String counsellorName) throws InterruptedException, ExecutionException {
	        DocumentReference counsellorRef = firestore.collection("counsellors").document(counsellorName);
	        DocumentSnapshot document = counsellorRef.get().get();

	        if (document.exists()) {
	            // Directly cast the result to List<String>
	            List<String> reviewIds = (List<String>) document.get("reviewIds");

	            if (reviewIds == null) {
	                return new ArrayList<>();
	            }

	            List<UserReview> allReviews = new ArrayList<>();
	            for (String reviewId : reviewIds) {
	                UserReview review = getReviewFromReviewId(reviewId);
	                if (review != null) {
	                    allReviews.add(review);
	                }
	            }
	            return allReviews;
	        }
	        return new ArrayList<>();
	    }


	    // Fetch a specific review from a user to a counselor
	    public UserReview getSpecificReview(String userName, String counsellorName) throws InterruptedException, ExecutionException, Exception {
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
	    
	    private UserReview getReviewFromReviewId(String reviewId) throws InterruptedException, ExecutionException {
			DocumentSnapshot userReview = firestore.collection("reviews").document(reviewId).get().get();
			return userReview.exists() ? userReview.toObject(UserReview.class) : null;
		}
	    
}
