package com.catalyst.ProCounsellor.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.catalyst.ProCounsellor.config.JwtUtil;
import com.catalyst.ProCounsellor.model.AppointmentBooking;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.CounsellorService;
import com.catalyst.ProCounsellor.service.PhotoService;
import com.catalyst.ProCounsellor.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/counsellor")
public class CounsellorController {
	
	@Autowired
    private CounsellorService counsellorService;
	
	@Autowired
    private UserService userService;
	
	@Autowired
	private PhotoService photoService;
	
	@PatchMapping("/{counsellorId}")
    public ResponseEntity<?> updateCounsellorFields(
            @PathVariable String counsellorId,
            @RequestBody Map<String, Object> updates,
            HttpServletRequest request) {
        try {
        	Counsellor authenticatedCounsellor = JwtUtil.getAuthenticatedCounsellor(request);
        	
        	 if (!authenticatedCounsellor.getUserName().equals(counsellorId)) {
        		 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
     	    }
        	
            Counsellor updatedCounsellor = counsellorService.updateCounsellorFields(counsellorId, updates);
            return ResponseEntity.ok(updatedCounsellor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
	
	@GetMapping("/{counsellorId}")
	public ResponseEntity<?> getCounsellorById(@PathVariable String counsellorId, HttpServletRequest request) 
	        throws ExecutionException, InterruptedException {

	    Counsellor authenticatedCounsellor = JwtUtil.getAuthenticatedCounsellor(request);

	    if (!authenticatedCounsellor.getUserName().equals(counsellorId)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied: counsellor ID mismatch");
	    }

	    Counsellor counsellor = counsellorService.getCounsellorById(counsellorId);

	    if (counsellor == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Counsellor not found");
	    }

	    return ResponseEntity.ok(counsellor);
	}
	
	@GetMapping("/getUserById")
	public ResponseEntity<?> getUserById(@RequestParam String counsellorId, @RequestParam String userId, HttpServletRequest request) 
	        throws ExecutionException, InterruptedException {

		Counsellor authenticatedCounsellor = JwtUtil.getAuthenticatedCounsellor(request);
		
	    if (!authenticatedCounsellor.getUserName().equals(counsellorId)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied: counsellor ID mismatch");
	    }

	    User user = userService.getUserById(userId);

	    if (userId == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	    }

	    return ResponseEntity.ok(user);
	}
	
	@GetMapping("/appointments")
    public ResponseEntity<List<AppointmentBooking>> getAppointmentsByCounsellor(
            @RequestParam String counsellorId, HttpServletRequest request) {
        try {
        	Counsellor counsellor = JwtUtil.getAuthenticatedCounsellor(request);

	        if (!counsellor.getUserName().equals(counsellorId)) {
	        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        }
            List<AppointmentBooking> appointments = counsellorService.getAppointmentsByCounsellorId(counsellorId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
	
	@GetMapping("/getAppointmentById")
	public ResponseEntity<AppointmentBooking> getAppointmentById(@RequestParam String counsellorId, @RequestParam String appointmentId, HttpServletRequest request) {
	    try {
	    	Counsellor counsellor = JwtUtil.getAuthenticatedCounsellor(request);

	        if (!counsellor.getUserName().equals(counsellorId)) {
	        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        }
	        
	        AppointmentBooking appointment = counsellorService.getAppointmentById(appointmentId);
	        if (appointment != null) {
	            return ResponseEntity.ok(appointment);
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}
	
	@GetMapping("/all-counsellors")
    public List<Counsellor> getAllCounsellors() {
        return counsellorService.getAllCounsellors();
    }
	 
	@GetMapping("/sorted-by-rating")
	public List<Counsellor> getCounsellorsSortedByRating() {
	    return counsellorService.getAllCounsellorsSortedByRating();
	}
	 
	@GetMapping("/counsellors-online")
	public List<Counsellor> getCounsellorsWithOnlineState() throws InterruptedException, ExecutionException {
	     return counsellorService.getOnlineCounsellors();
	}
	
	@PostMapping("/{userId}/photo")
    public String updateUserPhoto(@PathVariable String userId, @RequestParam("photo") MultipartFile file) {
        try {
            String fileType = file.getContentType().split("/")[1];

            // Upload the photo and get the photo URL
            String photoUrl = photoService.uploadPhoto(userId, file.getBytes(), fileType, "counsellor");

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
	            return ResponseEntity.ok("No clients found for counsellor with ID: " + counsellorId);
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
	
	@GetMapping("/{counsellorId}/has-client/{userId}")
	public ResponseEntity<Boolean> hasClient(@PathVariable String counsellorId, @PathVariable String userId) {
	    boolean hasClient = counsellorService.hasClient(counsellorId, userId);
	    return ResponseEntity.ok(hasClient);
	    
	}
	
	@GetMapping("/{counsellorId}/has-follower/{userId}")
	public ResponseEntity<Boolean> hasFollower(@PathVariable String counsellorId, @PathVariable String userId) {
	    boolean hasFollower = counsellorService.hasFollower(counsellorId, userId);
	    if (hasFollower) {
	        return ResponseEntity.ok(hasFollower);
	    }
	    return ResponseEntity.ok(hasFollower);
	}
	
	@PatchMapping("/{counsellorId}/verify")
	public ResponseEntity<Counsellor> verifyCounsellor(@PathVariable String counsellorId) {
	    try {
	        Map<String, Object> updates = new HashMap<>();
	        updates.put("verified", true); // Only update the 'verified' field to true

	        Counsellor updatedCounsellor = counsellorService.updateCounsellorFields(counsellorId, updates);
	        return ResponseEntity.ok(updatedCounsellor);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }
	}
	
	@PostMapping("/update-log/{userName}")
	public ResponseEntity<String> updateCounsellorLog(
	        @PathVariable String userName,
	        @RequestBody Map<String, Object> updates) {

	    try {
	        String result = counsellorService.saveCounsellorUpdates(userName, updates);
	        return ResponseEntity.ok(result);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
	    }
	}
	
	@PatchMapping("/apply-updates/{userName}")
	public ResponseEntity<String> applyUpdatesToCounsellor(@PathVariable String userName) {
	    try {
	        String result = counsellorService.applyPendingUpdates(userName);
	        return ResponseEntity.ok(result);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
	    }
	}


	
	/**
     * Update user state API using PathVariable.
     *
     * @param userName the counsellorName of the user
     * @param state    the presence state to be updated
     * @return ResponseEntity indicating success or failure
     */
    @PostMapping("/{counsellorName}/{state}")
    public ResponseEntity<String> updateCounsellorState(
            @PathVariable String counsellorName,
            @PathVariable String state) {

        boolean isUpdated = counsellorService.updateCounsellorState(counsellorName, state);
        if (isUpdated) {
            return ResponseEntity.ok("Counsellor state updated successfully.");
        } else {
            return ResponseEntity.status(500).body("Failed to update Counsellor state.");
        }
    }
    
    
    /**
     * Check if the user is online by their counsellorName.
     *
     * @param userName the counsellorName of the counsellor
     * @return true if the counsellor is online, false otherwise
     */
    @GetMapping("/{counsellorName}/isOnline")
    public boolean isOnline(@PathVariable String counsellorName) {
        try {
            return counsellorService.isCounsellorOnline(counsellorName);
        } catch (Exception e) {
            // Log the error and return false
            System.err.println("Error checking counsellor online status: " + e.getMessage());
            return false;
        }
    }
    
    @PostMapping("/mark-follower-notification-seen")
    public void markFollowersNotificationAsSeen(@RequestParam String counsellorId, @RequestParam String userId) {
    	counsellorService.markFollowersNotificationAsSeen(counsellorId, userId);
    }
    
    @PostMapping("/mark-subscriber-notification-seen")
    public void markSubscribersNotificationAsSeen(@RequestParam String counsellorId, @RequestParam String userId) {
    	counsellorService.markSubscribersNotificationAsSeen(counsellorId, userId);
    }
}
