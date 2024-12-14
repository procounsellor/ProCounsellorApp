package com.catalyst.ProCounsellor.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.StateType;
import com.catalyst.ProCounsellor.service.CounsellorService;
import com.catalyst.ProCounsellor.service.PhotoService;

@RestController
@RequestMapping("/api/counsellor")
public class CounsellorController {
	
	@Autowired
    private CounsellorService counsellorService;
	
	@Autowired
	private PhotoService photoService;
	
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

}
