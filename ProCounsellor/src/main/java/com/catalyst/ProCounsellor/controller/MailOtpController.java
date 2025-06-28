package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalyst.ProCounsellor.dto.MailOtpRequest;
import com.catalyst.ProCounsellor.service.MailOtpService;

@RestController
@RequestMapping("/api/otp")
public class MailOtpController {

    @Autowired 
    private MailOtpService otpService;

    @PostMapping("/send")
    public String sendOtp(@RequestBody MailOtpRequest request) {
        otpService.generateOtp(request.getEmail());
        return "OTP sent";
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody MailOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());

        if (isValid) {
            return ResponseEntity.ok("OTP verified ✅");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired OTP ❌");
        }
    }
}
