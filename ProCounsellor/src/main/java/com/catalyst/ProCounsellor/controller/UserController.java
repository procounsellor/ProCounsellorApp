package com.catalyst.ProCounsellor.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.FirebaseService;
import com.catalyst.ProCounsellor.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

	@Autowired
    private UserService userService;
	
	@Autowired
	private FirebaseService firebaseService;
	
	@GetMapping("/{userId}")
	public User getUserById(@PathVariable String userId) throws ExecutionException, InterruptedException {	
		return firebaseService.getUserById(userId);
	}

	@PostMapping("/{userId}/subscribe/{counsellorId}")
	public ResponseEntity<String> subscribeToCounsellor(@PathVariable String userId, @PathVariable String counsellorId) {
	    try {
	        boolean result = userService.subscribeToCounsellor(userId, counsellorId);
	        if (result) {
	            return ResponseEntity.ok("Successfully subscribed to the counsellor.");
	        }
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                             .body("Subscription failed. Either the user or counsellor does not exist.");
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("An error occurred while subscribing: " + e.getMessage());
	    }
	}

	@GetMapping("/{userId}/subscribed-counsellors")
	public ResponseEntity<?> getSubscribedCounsellors(@PathVariable String userId) {
	    try {
	        List<Counsellor> subscribedCounsellors = userService.getSubscribedCounsellors(userId);
	        if (subscribedCounsellors == null || subscribedCounsellors.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                                 .body("No counsellors subscribed by user with ID: " + userId);
	        }
	        return ResponseEntity.ok(subscribedCounsellors);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("An error occurred while retrieving subscribed counsellors: " + e.getMessage());
	    }
	}
	
	
	@PostMapping("/{userId}/follow/{counsellorId}")
	public ResponseEntity<String> followCounsellor(@PathVariable String userId, @PathVariable String counsellorId) {
	    try {
	        boolean result = userService.followCounsellor(userId, counsellorId);
	        if (result) {
	            return ResponseEntity.ok("Successfully followed the counsellor.");
	        }
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                             .body("Cannot follow. Either the user or counsellor does not exist.");
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("An error occurred while following: " + e.getMessage());
	    }
	}

	@GetMapping("/{userId}/followed-counsellors")
	public ResponseEntity<?> getFollowedCounsellors(@PathVariable String userId) {
	    try {
	        List<Counsellor> followedCounsellors = userService.getFollowedCounsellors(userId);
	        if (followedCounsellors == null || followedCounsellors.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                                 .body("No counsellors followed by user with ID: " + userId);
	        }
	        return ResponseEntity.ok(followedCounsellors);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("An error occurred while retrieving followed counsellors: " + e.getMessage());
	    }
	}
	
	@GetMapping("/{userId}/is-subscribed/{counsellorId}")
	public ResponseEntity<String> isSubscribedToCounsellor(@PathVariable String userId, @PathVariable String counsellorId) {
	    boolean isSubscribed = userService.isSubscribedToCounsellor(userId, counsellorId);
	    if (isSubscribed) {
	        return ResponseEntity.ok("User has subscribed to the counsellor.");
	    }
	    return ResponseEntity.ok("User has NOT subscribed to the counsellor.");
	}
	
	@GetMapping("/{userId}/has-followed/{counsellorId}")
	public ResponseEntity<String> hasFollowedCounsellor(@PathVariable String userId, @PathVariable String counsellorId) {
	    boolean hasFollowed = userService.hasFollowedCounsellor(userId, counsellorId);
	    if (hasFollowed) {
	        return ResponseEntity.ok("User has followed the counsellor.");
	    }
	    return ResponseEntity.ok("User has NOT followed the counsellor.");
	}

}
