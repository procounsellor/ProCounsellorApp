package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.config.JwtKeyProvider;
import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Admin;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.AdminService;
import com.catalyst.ProCounsellor.service.CounsellorService;
import com.catalyst.ProCounsellor.service.OTPService;
import com.catalyst.ProCounsellor.service.UserService;

import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CounsellorService counsellorService;
    
    @Autowired
    private AdminService adminAuthService;
    
    @Autowired
    private OTPService otpService;

    @PostMapping("/userSignup")
    public ResponseEntity<Map<String, Object>> userSignup(@RequestBody User user) throws ExecutionException, InterruptedException {
        String message = userService.signup(user);
        HttpStatus status = message.startsWith("Signup successful") ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return buildResponse(message, status);
    }

    @PostMapping("/userSignin")
    public ResponseEntity<Map<String, Object>> userSignin(@RequestParam String identifier, @RequestParam String password) throws ExecutionException, InterruptedException {
        try {
            String message = userService.signin(identifier, password);
            return buildResponse(message, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidCredentialsException e) {
            return buildResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/counsellorSignup")
    public ResponseEntity<Map<String, Object>> counsellorSignup(@RequestBody Counsellor counsellor) throws ExecutionException, InterruptedException {
        try {
            String message = counsellorService.signup(counsellor);
            return buildResponse(message, HttpStatus.CREATED);
        } catch (Exception e) {
            return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/counsellorSignin")
    public ResponseEntity<Map<String, Object>> counsellorSignin(@RequestParam String identifier, @RequestParam String password) throws ExecutionException, InterruptedException {
        try {
            String message = counsellorService.signin(identifier, password);
            return buildResponse(message, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidCredentialsException e) {
            return buildResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



    @PostMapping("/adminSignup")
    public ResponseEntity<Map<String, Object>> adminSignup(@RequestBody Admin admin) throws ExecutionException, InterruptedException {
        String message = adminAuthService.signup(admin);
        return buildResponse(message, HttpStatus.CREATED);
    }

    @PostMapping("/adminSignin")
    public ResponseEntity<Map<String, Object>> adminSignin(@RequestBody Admin admin) throws ExecutionException, InterruptedException {
        try {
        	String message = adminAuthService.signin(admin);
            return buildResponse(message, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidCredentialsException e) {
            return buildResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", status.value());
        return new ResponseEntity<>(response, status);
    }
    
    @PostMapping("/generateOtp")
    public ResponseEntity<String> generateOtp(@RequestParam String phoneNumber) {
    	phoneNumber=phoneNumber.replace(" ", "+");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is mandatory and cannot be null or empty.");
        }
        String response = otpService.generateAndSendOtp(phoneNumber);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verifyAndUserSignup")
    public ResponseEntity<Map<String, Object>> verifyAndSignup(
            @RequestParam String phoneNumber, @RequestParam String otp) throws ExecutionException, InterruptedException {

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number is mandatory and cannot be null or empty."));
        }

        if (!otpService.verifyOtp(phoneNumber, otp)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP. Please try again."));
        }

        String responseMessage;
        HttpStatus responseStatus;
        String jwtToken = null;
        String userId;
        
		if (userService.isPhoneNumberExists(phoneNumber)) {
            responseMessage = "Phone number already exists. User logged in successfully.";
            responseStatus = HttpStatus.OK;
            userId = userService.getUserNameFromPhoneNumber(phoneNumber);
        } else {
            responseMessage = userService.newSignup(phoneNumber);
            responseStatus = responseMessage.startsWith("Signup successful") ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST; // New signup
            userId = userService.getUserNameFromPhoneNumber(phoneNumber);
        }

        if (responseStatus != HttpStatus.BAD_REQUEST) {
            // Generate JWT Token using the centralized key
            jwtToken = Jwts.builder()
                    .setSubject(phoneNumber)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000 * 365))
                    .signWith(JwtKeyProvider.getSigningKey())
                    .compact();
        }
        System.out.println(JwtKeyProvider.getSigningKey().toString());

        return ResponseEntity.status(responseStatus).body(Map.of(
                "message", responseMessage,
                "jwtToken", jwtToken,
                "userId", userId));
    }

    
    @GetMapping("/isUserDetailsNull")
    public ResponseEntity<Boolean> isUserDetailsNull(@RequestParam String userId) throws ExecutionException, InterruptedException {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(false);
        }

        // Fetch the user details
        User user = userService.getUserById(userId);
        if (user == null) {
        	System.out.println("nulllll");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(true);
        }

        // Check if both fields are null
        boolean isNull = (user.getUserInterestedStateOfCounsellors() == null || user.getUserInterestedStateOfCounsellors().isEmpty())
                && user.getInterestedCourse() == null;

        return ResponseEntity.ok(isNull);
    }
}
