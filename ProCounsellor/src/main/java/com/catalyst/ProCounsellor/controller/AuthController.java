package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.model.Admin;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.AdminAuthService;
import com.catalyst.ProCounsellor.service.AuthService;
import com.catalyst.ProCounsellor.service.CounsellorAuthService;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private CounsellorAuthService counsellorAuthService;
    
    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/userSignup")
    public String userSignup(@RequestBody User user) throws ExecutionException, InterruptedException {
        return authService.signup(user);
        
    }

    @PostMapping("/userSignin")
    public String userSignin(@RequestBody User user) throws ExecutionException, InterruptedException {
        return authService.signin(user);
    }
    
    @PostMapping("/counsellorSignup")
    public String counsellorSignup(@RequestBody Counsellor user) throws ExecutionException, InterruptedException {
        return counsellorAuthService.signup(user);
    }

    @PostMapping("/counsellorSignin")
    public String counsellorSignin(@RequestBody Counsellor user) throws ExecutionException, InterruptedException {
        return counsellorAuthService.signin(user);
    }
    
    @PostMapping("/adminSignup")
    public String adminSignup(@RequestBody Admin user) throws ExecutionException, InterruptedException {
        return adminAuthService.signup(user);
    }

    @PostMapping("/adminSignin")
    public String adminSignin(@RequestBody Admin user) throws ExecutionException, InterruptedException {
        return adminAuthService.signin(user);
    }
    
}

