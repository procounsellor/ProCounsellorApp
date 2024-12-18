package com.catalyst.ProCounsellor.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.StateType;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.CounsellorService;
import com.catalyst.ProCounsellor.service.FirebaseService;
import com.catalyst.ProCounsellor.service.PhotoService;

@RestController
@RequestMapping("/api/counsellor")
public class CounsellorController {
	
	@Autowired
    private CounsellorService counsellorService;
	
	@Autowired
	private PhotoService photoService;
	
	@Autowired
	private FirebaseService firebaseService;
	
	@GetMapping("/all-counsellors")
    public List<Counsellor> getAllCounsellors() {
        return counsellorService.getAllCounsellors();
    }
	 
	@GetMapping("/sorted-by-rating")
	public List<Counsellor> getCounsellorsSortedByRating() {
	    return counsellorService.getAllCounsellorsSortedByRating();
	}
	 
	@GetMapping("/counsellors-online")
	public List<Counsellor> getCounsellorsWithOnlineState() {
	     return counsellorService.getCounsellorsByState(StateType.ONLINE);
	}
	
	@PostMapping("/{userId}/photo")
    public String updateUserPhoto(@PathVariable String userId, @RequestParam("photo") MultipartFile file) {
        try {
            String fileType = file.getContentType().split("/")[1];

            // Upload the photo and get the photo URL
            String photoUrl = photoService.uploadPhoto(userId, file.getBytes(), fileType);

            // Update the user's photo URL in Firestore
            counsellorService.updateUserPhotoUrl(userId, photoUrl);

            return "Photo updated successfully: " + photoUrl;
        } catch (IOException e) {
            return "Error uploading photo: " + e.getMessage();
        }
    }
	
	@GetMapping("/{counsellorId}/clients")
	public ResponseEntity<?> getSubscribedClients(@PathVariable String counsellorId) {
	    try {
	        List<User> clients = counsellorService.getSubscribedClients(counsellorId);
	        if (clients == null || clients.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                                 .body("No clients found for counsellor with ID: " + counsellorId);
	        }
	        return ResponseEntity.ok(clients);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("An error occurred while retrieving subscribed clients: " + e.getMessage());
	    }
	}
	
	@GetMapping("/{counsellorId}/followers")
	public ResponseEntity<?> getFollowers(@PathVariable String counsellorId) {
	    try {
	        List<User> followers = counsellorService.getFollowers(counsellorId);
	        if (followers == null || followers.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                                 .body("No followers found for counsellor with ID: " + counsellorId);
	        }
	        return ResponseEntity.ok(followers);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("An error occurred while retrieving followers: " + e.getMessage());
	    }
	}
	
	@GetMapping("/{counsellorId}")
	public Counsellor getCounsellorById(@PathVariable String counsellorId) throws ExecutionException, InterruptedException {	
		return firebaseService.getCounsellorById(counsellorId);
	}
	
	@GetMapping("/{counsellorId}/has-client/{userId}")
	public ResponseEntity<Boolean> hasClient(@PathVariable String counsellorId, @PathVariable String userId) {
	    boolean hasClient = counsellorService.hasClient(counsellorId, userId);
	    return ResponseEntity.ok(hasClient);
	    
	}
	
	@GetMapping("/{counsellorId}/has-follower/{userId}")
	public ResponseEntity<String> hasFollower(@PathVariable String counsellorId, @PathVariable String userId) {
	    boolean hasFollower = counsellorService.hasFollower(counsellorId, userId);
	    if (hasFollower) {
	        return ResponseEntity.ok("Counsellor has the user as a follower.");
	    }
	    return ResponseEntity.ok("Counsellor does not have the user as a follower.");
	}


}
