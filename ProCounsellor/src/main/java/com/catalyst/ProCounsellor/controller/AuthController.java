package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Admin;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.AdminService;
import com.catalyst.ProCounsellor.service.CounsellorService;
import com.catalyst.ProCounsellor.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService authService;
    
    @Autowired
    private CounsellorService counsellorAuthService;
    
    @Autowired
    private AdminService adminAuthService;

    @PostMapping("/userSignup")
    public ResponseEntity<Map<String, Object>> userSignup(@RequestBody User user) throws ExecutionException, InterruptedException {
        String message = authService.signup(user);
        return buildResponse(message, HttpStatus.CREATED);
    }

    @PostMapping("/userSignin")
    public ResponseEntity<Map<String, Object>> userSignin(@RequestBody User user) throws ExecutionException, InterruptedException {
        try {
            String message = authService.signin(user);
            return buildResponse(message, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidCredentialsException e) {
            return buildResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }


    @PostMapping("/counsellorSignup")
    public ResponseEntity<Map<String, Object>> counsellorSignup(@RequestBody Counsellor counsellor) throws ExecutionException, InterruptedException {
        String message = counsellorAuthService.signup(counsellor);
        return buildResponse(message, HttpStatus.CREATED);
    }

    @PostMapping("/counsellorSignin")
    public ResponseEntity<Map<String, Object>> counsellorSignin(@RequestBody Counsellor counsellor) throws ExecutionException, InterruptedException {
        try {
            String message = counsellorAuthService.signin(counsellor);
            return buildResponse(message, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidCredentialsException e) {
            return buildResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
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
}
