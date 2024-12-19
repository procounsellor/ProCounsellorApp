package com.catalyst.ProCounsellor.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.model.UserReview;

@Service
public class ReviewService {

    @Autowired
    private FirebaseService firebaseService;


    // Post a review from a user to a counsellor
    public void postReview(String userName, String counsellorName, UserReview userReview) throws Exception {
            firebaseService.postReview(userName, counsellorName, userReview);
    }

    // Fetch all reviews given by a specific user
    public List<UserReview> getReviewsByUser(String userName) throws InterruptedException, ExecutionException {
        return firebaseService.getUserReviews(userName);
    }

    // Fetch all reviews for a specific counselor
    public List<UserReview> getReviewsForCounsellor(String counsellorName) throws InterruptedException, ExecutionException {
        return firebaseService.getCounsellorReviews(counsellorName);
    }

    // Fetch a specific review from a user to a counselor
    public UserReview getReview(String userName, String counsellorName) throws InterruptedException, ExecutionException {
        return firebaseService.getSpecificReview(userName, counsellorName);
    }
}
