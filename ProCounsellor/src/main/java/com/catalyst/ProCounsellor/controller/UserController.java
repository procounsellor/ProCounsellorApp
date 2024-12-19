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
	    	boolean hasAlreadySubscribed = userService.isSubscribedToCounsellor(userId, counsellorId);
	    	if(!hasAlreadySubscribed) {
		        boolean result = userService.subscribeToCounsellor(userId, counsellorId);
		        if (result) {
		            return ResponseEntity.ok("Successfully subscribed to the counsellor.");
		        }
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
		                             .body("Subscription failed. Either the user or counsellor does not exist.");
	    	}
	    	else {
	    		return ResponseEntity.ok("Already subscribed to the counsellor.");
	    	}
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
	    	boolean isAlreadyFollwing = userService.hasFollowedCounsellor(userId, counsellorId);
	    	
	    	if(!isAlreadyFollwing) {
		        boolean result = userService.followCounsellor(userId, counsellorId);
		        if (result) {
		            return ResponseEntity.ok("Successfully followed the counsellor.");
		        }
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
		                             .body("Cannot follow. Either the user or counsellor does not exist.");
	    	}
	    	else {
	    		return ResponseEntity.ok("Already following the counsellor.");
	    	}
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
	public ResponseEntity<Boolean> isSubscribedToCounsellor(@PathVariable String userId, @PathVariable String counsellorId) {
	    boolean isSubscribed = userService.isSubscribedToCounsellor(userId, counsellorId);
	    return ResponseEntity.ok(isSubscribed);
	}
	
	@GetMapping("/{userId}/has-followed/{counsellorId}")
	public ResponseEntity<Boolean> hasFollowedCounsellor(@PathVariable String userId, @PathVariable String counsellorId) {
	    boolean hasFollowed = userService.hasFollowedCounsellor(userId, counsellorId);
	    if (hasFollowed) {
	        return ResponseEntity.ok(hasFollowed);
	    }
	    return ResponseEntity.ok(hasFollowed);
	}
	
	@DeleteMapping("/{userId}/unsubscribe/{counsellorId}")
	public ResponseEntity<String> unsubscribeCounsellor(
	        @PathVariable String userId, 
	        @PathVariable String counsellorId) {
	    try {
	        // Check if the user is subscribed to the counsellor
	        boolean isSubscribed = userService.isSubscribedToCounsellor(userId, counsellorId);

	        if (!isSubscribed) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("User is not subscribed to this counsellor.");
	        }

	        // Proceed to unsubscribe
	        boolean result = userService.unsubscribeCounsellor(userId, counsellorId);
	        if (result) {
	            return ResponseEntity.ok("Successfully unsubscribed from the counsellor.");
	        } else {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("An error occurred while trying to unsubscribe.");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("An unexpected error occurred.");
	    }
	}

	
	@DeleteMapping("/{userId}/unfollow/{counsellorId}")
	public ResponseEntity<String> unfollowCounsellor(
	        @PathVariable String userId, 
	        @PathVariable String counsellorId) {
	    try {
	        // Check if the user has followed to the counsellor
	        boolean hasFollowed = userService.hasFollowedCounsellor(userId, counsellorId);

	        if (!hasFollowed) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("User has not followed this counsellor.");
	        }

	        // Proceed to undollow
	        boolean result = userService.unfollowCounsellor(userId, counsellorId);
	        if (result) {
	            return ResponseEntity.ok("Successfully unfollowed the counsellor.");
	        } else {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("An error occurred while trying to unfollow.");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("An unexpected error occurred.");
	    }
	}


}
