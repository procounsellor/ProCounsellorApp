//package com.catalyst.ProCounsellor.controller;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.catalyst.ProCounsellor.service.OTPService;
//
//@RestController
//@RequestMapping("/api/otp")
//public class OTPController {
//
//    private final OTPService otpService;
//
//    public OTPController(OTPService otpService) {
//        this.otpService = otpService;
//    }
//
//    @PostMapping("/generate")
//    public ResponseEntity<String> generateOTP(@RequestParam String phoneNumber) {
//        try {
//        	phoneNumber = phoneNumber.replace(" ", "+");
//            String response = otpService.generateOTP(phoneNumber);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(e.getMessage());
//        }
//    }
//
//    @PostMapping("/verify")
//    public ResponseEntity<String> verifyOTP(@RequestParam String phoneNumber, @RequestParam String otpCode) {
//        try {
//        	phoneNumber = phoneNumber.replace(" ", "+");
//            String response = otpService.verifyOTP(phoneNumber, otpCode);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(e.getMessage());
//        }
//    }
//}
