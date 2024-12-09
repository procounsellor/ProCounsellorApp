package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.AuthService;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public String signup(@RequestBody User user) throws ExecutionException, InterruptedException {
        return authService.signup(user);
    }

    @PostMapping("/signin")
    public String signin(@RequestBody User user) throws ExecutionException, InterruptedException {
        return authService.signin(user);
    }
    
}

