package com.catalyst.ProCounsellor.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalyst.ProCounsellor.model.UserReview;
import com.catalyst.ProCounsellor.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/{userName}/{counsellorName}")
    public ResponseEntity<String> postReview(
            @PathVariable String userName,
            @PathVariable String counsellorName,
            @RequestBody UserReview userReview) {
        try {
            // Call the service to post the review
            reviewService.postReview(userName, counsellorName, userReview);
            // Return success response if review posted successfully
            return ResponseEntity.ok("Review posted successfully.");
        } catch (Exception e) {
            // Return error response if any exception occurs (e.g., user or counsellor doesn't exist)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
    
    // Get all reviews given by a user
    @GetMapping("/user/{userName}")
    public ResponseEntity<List<UserReview>> getUserReviews(@PathVariable String userName) throws InterruptedException, ExecutionException {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userName));
    }

    // Get all reviews received by a counselor
    @GetMapping("/counsellor/{counsellorName}")
    public ResponseEntity<List<UserReview>> getCounsellorReviews(@PathVariable String counsellorName) throws InterruptedException, ExecutionException {
        return ResponseEntity.ok(reviewService.getReviewsForCounsellor(counsellorName));
    }

    // Get a specific review from a user to a counselor
    @GetMapping("/{userName}/{counsellorName}")
    public ResponseEntity<UserReview> getSpecificReview(
            @PathVariable String userName,
            @PathVariable String counsellorName) throws InterruptedException, ExecutionException {
        return ResponseEntity.ok(reviewService.getReview(userName, counsellorName));
    }
}
