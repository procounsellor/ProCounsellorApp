package com.catalyst.ProCounsellor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
@RestController
@RequestMapping("api/phoneOtp")
public class MessageOtpController {

    private String apiKey = System.getenv("OTP_API_KEY");

    private final String BASE_URL = "https://2factor.in/API/V1/";

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(@RequestParam String phoneNumber) {
        String url = "https://2factor.in/API/V1/" + apiKey + "/SMS/" + phoneNumber + "/AUTOGEN";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return ResponseEntity.ok(response.getBody());
    }
//    https://2factor.in/API/V1/:api_key/SMS/:phone_number/AUTOGEN/:otp_template_name
//    	https://2factor.in/API/V1/:api_key/SMS/:phone_number/AUTOGEN2/:otp_template_name

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String phoneNumber, @RequestParam String otp) {
        String url = BASE_URL + apiKey + "/SMS/VERIFY/" + phoneNumber + "/" + otp;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return ResponseEntity.ok(response.getBody());
    }
}
