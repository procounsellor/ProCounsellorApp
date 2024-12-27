package com.catalyst.ProCounsellor.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalyst.ProCounsellor.model.UserReview;
import com.catalyst.ProCounsellor.model.UserReviewComments;
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
    
    @PutMapping("/{reviewId}")
    public ResponseEntity<String> updateReview(
            @PathVariable String reviewId, @RequestBody UserReview updatedReview) {
        
        try {
            reviewService.updateReview(reviewId, updatedReview);
            return ResponseEntity.ok("Review updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update review: " + e.getMessage());
        }
    }
    
    // DELETE API to delete a review
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable String reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok("Review deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
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
            @PathVariable String counsellorName) throws Exception {
        return ResponseEntity.ok(reviewService.getReview(userName, counsellorName));
    }
    
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<String> likeReview(@PathVariable String reviewId) {
        try {
            reviewService.likeReview(reviewId);
            return ResponseEntity.ok("Review liked successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    @PostMapping("/{reviewId}/unlike")
    public ResponseEntity<String> unlikeReview(@PathVariable String reviewId) {
        try {
            reviewService.unlikeReview(reviewId);
            return ResponseEntity.ok("Review unliked successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    @GetMapping("/{reviewId}/likes")
    public ResponseEntity<Integer> getReviewLikes(@PathVariable String reviewId) {
        try {
            Integer noOfLikes = reviewService.getReviewLikes(reviewId);
            return ResponseEntity.ok(noOfLikes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    // POST API to add a comment to a review
    @PostMapping("/{reviewId}/comments/{userName}")
    public ResponseEntity<String> addComment(
            @PathVariable String reviewId,
            @PathVariable String userName,
            @RequestBody UserReviewComments comment) {
        try {
            reviewService.addComment(reviewId, userName, comment);
            return ResponseEntity.ok("Comment added successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    // POST API to update an existing comment to a review
    @PutMapping("/{reviewId}/comments/{commentId}")
    public ResponseEntity<String> updateComment(
            @PathVariable String reviewId, 
            @PathVariable String commentId, 
            @RequestBody UserReviewComments updatedComment) {
        try {
            reviewService.updateComment(reviewId, commentId, updatedComment);
            return ResponseEntity.ok("Comment updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    // Delete a comment from a review
    @DeleteMapping("/{reviewId}/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable String reviewId, @PathVariable String commentId) {
        try {
            reviewService.deleteComment(reviewId, commentId);
            return ResponseEntity.ok("Comment deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // GET API to fetch all comments of a review
    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<List<UserReviewComments>> getComments(@PathVariable String reviewId) {
        try {
            List<UserReviewComments> comments = reviewService.getComments(reviewId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    
}
