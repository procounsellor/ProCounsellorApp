package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.config.JwtUtil;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.processor.dto.CollegeResponse;
import com.catalyst.ProCounsellor.processor.service.CollegeService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/website/college")
public class CollegeController {

    @Autowired
    private CollegeService collegeService;

    @GetMapping
    public ResponseEntity<?> getCollege(@RequestParam String name, 
                                        @RequestParam String userId, 
                                        HttpServletRequest request) {
        try {
            User user = JwtUtil.getAuthenticatedUser(request);

            if (!user.getUserName().equals(userId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
            }

            CollegeResponse response = collegeService.getCollegeInfo(name);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error fetching college data: " + ex.getMessage());
        }
    }
}
