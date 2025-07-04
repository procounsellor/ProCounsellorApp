package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.config.JwtKeyProvider;
import com.catalyst.ProCounsellor.dto.MailOtpRequest;
import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Admin;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.AdminService;
import com.catalyst.ProCounsellor.service.CounsellorService;
import com.catalyst.ProCounsellor.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

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
    private AdminService adminService;
    
    @PostMapping("/generateOtp")
    public ResponseEntity<String> generateOtp(@RequestParam String phoneNumber) {
    	phoneNumber=phoneNumber.replace(" ", "+");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is mandatory and cannot be null or empty.");
        }
        String response = userService.generateAndSendOtp(phoneNumber);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verifyAndUserSignup")
    public ResponseEntity<Map<String, Object>> verifyAndSignupOrSignin(
            @RequestParam String phoneNumber, @RequestParam String otp) {
        
        phoneNumber = phoneNumber.replace(" ", "+");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number is mandatory and cannot be null or empty."));
        }

        try {
            Map<String, Object> result = userService.handleVerificationAndSignup(phoneNumber, otp);
            return ResponseEntity.status((HttpStatus) result.get("status")).body(result);
        } catch (RuntimeException | FirebaseAuthException | InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/isUserDetailsNull")
    public ResponseEntity<Boolean> isUserDetailsNull(@RequestParam String userId) throws ExecutionException, InterruptedException {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(false);
        }

        // Fetch the user details
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(true);
        }

        // Check if both fields are null
        boolean isNull = (user.getUserInterestedStateOfCounsellors() == null || user.getUserInterestedStateOfCounsellors().isEmpty())
                && user.getInterestedCourse() == null;

        return ResponseEntity.ok(isNull);
    }
    
    @PostMapping("/verifyCounsellorPhoneNumber")
    public ResponseEntity<Boolean> verifyCounsellorPhoneNumber(
            @RequestParam String phoneNumber, @RequestParam String otp) {

        phoneNumber = phoneNumber.replace(" ", "+");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body(false);
        }

        try {
            boolean result = counsellorService.verifyCounsellorPhoneNumber(phoneNumber, otp);
            if(result) {
            	return ResponseEntity.ok(true);
            }
            else {
            	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
            }
            
        } catch (RuntimeException | FirebaseAuthException | InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    
    @PostMapping("/send-mail")
    public ResponseEntity<String> sendOtp(@RequestBody MailOtpRequest request) {
        counsellorService.generateMailOtp(request.getEmail());
        return ResponseEntity.ok("OTP sent ✅");
    }

    @PostMapping("/verify-mail")
    public ResponseEntity<String> verifyOtp(@RequestBody MailOtpRequest request) {
        boolean isValid = counsellorService.verifyMailOtp(request.getEmail(), request.getOtp());
        if (isValid) {
            return ResponseEntity.ok("OTP verified ✅");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired OTP ❌");
        }
    }
    
    @PostMapping("/counsellorSignup")
    public ResponseEntity<Map<String, Object>> counsellorSignup(@RequestBody Counsellor counsellor) throws ExecutionException, InterruptedException {
        try {
            String message = counsellorService.counsellorSignup(counsellor);
            return buildResponse(message, HttpStatus.CREATED);
        } catch (Exception e) {
            return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/counsellorSignin")
    public ResponseEntity<Map<String, Object>> counsellorSignin(
            @RequestParam String identifier,
            @RequestParam String password
    ) throws ExecutionException, InterruptedException, FirebaseAuthException {
        try {
            Map<String, Object> response = counsellorService.signinAndGenerateTokens(identifier, password);
            return ResponseEntity.ok(response);

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
        String message = adminService.signup(admin);
        return buildResponse(message, HttpStatus.CREATED);
    }

    @PostMapping("/adminSignin")
    public ResponseEntity<Map<String, Object>> adminSignin(@RequestParam String identifier, @RequestParam String password) throws ExecutionException, InterruptedException, FirebaseAuthException {
    	try {
            HttpStatus status = adminService.signin(identifier, password);
            String jwtToken = null;
            String userId = adminService.getAdminId(identifier);
            String firebaseCustomToken = null;
            
            if (status == HttpStatus.OK) {
            	firebaseCustomToken = FirebaseAuth.getInstance().createCustomToken(userId);
                // Generate JWT Token using the centralized key
                jwtToken = Jwts.builder()
                        .setSubject(userId)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 86400000 * 365))
                        .signWith(JwtKeyProvider.getSigningKey())
                        .compact();
            }

            return ResponseEntity.status(status).body(Map.of(
                    "firebaseCustomToken", firebaseCustomToken,
                    "jwtToken", jwtToken,
                    "userId", userId));
            
        } catch (UserNotFoundException e) {
            return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidCredentialsException e) {
            return buildResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", status.value());
        return new ResponseEntity<>(response, status);
    }
}
