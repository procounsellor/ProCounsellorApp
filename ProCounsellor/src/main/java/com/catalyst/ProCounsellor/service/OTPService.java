package com.catalyst.ProCounsellor.service;

import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.config.TwilioConfig;
import com.twilio.rest.verify.v2.service.VerificationCheck;

import com.twilio.rest.verify.v2.service.Verification;


@Service
public class OTPService {

    private final TwilioConfig twilioConfig;

    public OTPService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    public String generateAndSendOtp(String phoneNumber) {
        try {
            Verification verification = Verification.creator(
                    twilioConfig.getVerifyServiceSid(),
                    phoneNumber,
                    "sms"
            ).create();
            return "OTP sent successfully to: " + phoneNumber;
        } catch (Exception e) {
            throw new RuntimeException("Error sending OTP: " + e.getMessage());
        }
    }

    public boolean verifyOtp(String phoneNumber, String otpCode) {
        try {
            VerificationCheck verificationCheck = VerificationCheck.creator(
                    twilioConfig.getVerifyServiceSid(),
                    otpCode
            ).setTo(phoneNumber).create();

            if ("approved".equals(verificationCheck.getStatus())) {
                return true;
            } else {
                throw new RuntimeException("Invalid OTP.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error verifying OTP: " + e.getMessage());
        }
    }


}
