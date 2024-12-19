package com.catalyst.ProCounsellor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.model.UserReview;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
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
	    
	    // Post a review only if both user and counsellor exist
	    public void postReview(String userName, String counsellorName, UserReview review) throws Exception {
	        // Set the userName and counsellorName in the review
	        review.setUserName(userName);
	        review.setCounsellorName(counsellorName);

	        // First, check if both user and counsellor exist
	        DocumentReference userRef = firestore.collection("users").document(userName);
	        DocumentSnapshot userDoc = userRef.get().get();

	        if (!userDoc.exists()) {
	            throw new Exception("User with ID " + userName + " does not exist.");
	        }

	        DocumentReference counsellorRef = firestore.collection("counsellors").document(counsellorName);
	        DocumentSnapshot counsellorDoc = counsellorRef.get().get();

	        if (!counsellorDoc.exists()) {
	            throw new Exception("Counsellor with ID " + counsellorName + " does not exist.");
	        }

	        // Add the review to both the user's and the counsellor's review lists
	        addReviewToUser(userName, review);
	        addReviewToCounsellor(counsellorName, review);
	    }

	    // Add a review to the user's reviews
	    private void addReviewToUser(String userName, UserReview review) throws Exception {
	        DocumentReference userRef = firestore.collection("users").document(userName);
	        DocumentSnapshot userDoc = userRef.get().get();

	        if (!userDoc.exists()) {
	            throw new Exception("User with ID " + userName + " does not exist.");
	        }

	        // Fetch current reviews and add the new one
	        List<UserReview> currentReviews = userDoc.exists() ? userDoc.toObject(User.class).getUserReview() : new ArrayList<>();
	        if (currentReviews == null) {
	            currentReviews = new ArrayList<>();  // Initialize if null
	        }

	        currentReviews.add(review);
	        userRef.update("userReview", currentReviews);  // Update the user's review list
	    }

	    // Add a review to the counsellor's reviews
	    private void addReviewToCounsellor(String counsellorName, UserReview review) throws Exception {
	        DocumentReference counsellorRef = firestore.collection("counsellors").document(counsellorName);
	        DocumentSnapshot counsellorDoc = counsellorRef.get().get();

	        if (!counsellorDoc.exists()) {
	            throw new Exception("Counsellor with ID " + counsellorName + " does not exist.");
	        }

	        // Fetch current reviews and add the new one
	        List<UserReview> currentReviews = counsellorDoc.exists() ? counsellorDoc.toObject(Counsellor.class).getReviews() : new ArrayList<>();
	        if (currentReviews == null) {
	            currentReviews = new ArrayList<>();  // Initialize if null
	        }

	        currentReviews.add(review);
	        counsellorRef.update("reviews", currentReviews);  // Update the counsellor's review list
	    }
	    
	    // Fetch all reviews given by a user
	    public List<UserReview> getUserReviews(String userName) throws InterruptedException, ExecutionException {
	        DocumentReference userRef = firestore.collection("users").document(userName);
	        ApiFuture<DocumentSnapshot> future = userRef.get();
	        DocumentSnapshot document = future.get();

	        if (document.exists()) {
	            User user = document.toObject(User.class);
	            return user.getUserReview() != null ? user.getUserReview() : new ArrayList<>();
	        }
	        return new ArrayList<>();
	    }

	    // Fetch all reviews received by a counselor
	    public List<UserReview> getCounsellorReviews(String counsellorName) throws InterruptedException, ExecutionException {
	        DocumentReference counsellorRef = firestore.collection("counsellors").document(counsellorName);
	        ApiFuture<DocumentSnapshot> future = counsellorRef.get();
	        DocumentSnapshot document = future.get();

	        if (document.exists()) {
	            Counsellor counsellor = document.toObject(Counsellor.class);
	            return counsellor.getReviews() != null ? counsellor.getReviews() : new ArrayList<>();
	        }
	        return new ArrayList<>();
	    }

	    // Fetch a specific review from a user to a counselor
	    public UserReview getSpecificReview(String userName, String counsellorName) throws InterruptedException, ExecutionException {
	        DocumentReference userRef = firestore.collection("users").document(userName);
	        ApiFuture<DocumentSnapshot> future = userRef.get();
	        DocumentSnapshot document = future.get();

	        if (document.exists()) {
	            User user = document.toObject(User.class);
	            if (user.getUserReview() != null) {
	                return user.getUserReview().stream()
	                        .filter(review -> review.getCounsellorName().equals(counsellorName))
	                        .findFirst()
	                        .orElse(null);
	            }
	        }
	        return null;
	    }
}
