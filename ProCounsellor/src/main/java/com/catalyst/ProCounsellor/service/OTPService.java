package com.catalyst.ProCounsellor.service;

import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.config.TwilioConfig;
import com.catalyst.ProCounsellor.requestValidator.GenerateOTPRequestValidator;
import com.twilio.rest.verify.v2.service.VerificationCheck;

import com.twilio.rest.verify.v2.service.Verification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OTPService {

    private static final Logger logger = LoggerFactory.getLogger(OTPService.class);

    private final TwilioConfig twilioConfig;

    public OTPService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    public String generateAndSendOtp(String phoneNumber) {
        logger.info("Attempting to generate and send OTP to {}", phoneNumber);
        try {
            if (phoneNumber.contains("00000000")) {
                logger.warn("Test number detected: {}", phoneNumber);
                return "Test number detected. Use OTP : 000000";
            }

            Verification verification = Verification.creator(
                    twilioConfig.getVerifyServiceSid(),
                    phoneNumber,
                    "sms"
            ).create();

            logger.info("OTP sent successfully to {}", phoneNumber);
            return "OTP sent successfully to: " + phoneNumber;

        } catch (Exception e) {
            logger.error("Failed to send OTP to {}: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("Error sending OTP: " + e.getMessage(), e);
        }
    }

    public boolean verifyOtp(String phoneNumber, String otpCode) {
        logger.info("Verifying OTP for phone number {}", phoneNumber);
        try {
        	GenerateOTPRequestValidator.validateInput(phoneNumber);
            if (phoneNumber.contains("00000000") && "000000".equals(otpCode)) {
                logger.warn("Test OTP verified for number: {}", phoneNumber);
                return true;
            }

            VerificationCheck verificationCheck = VerificationCheck.creator(
                    twilioConfig.getVerifyServiceSid(),
                    otpCode
            ).setTo(phoneNumber).create();

            if ("approved".equalsIgnoreCase(verificationCheck.getStatus())) {
                logger.info("OTP verified successfully for {}", phoneNumber);
                return true;
            } else {
                logger.warn("Invalid OTP for {}. Status returned: {}", phoneNumber, verificationCheck.getStatus());
                throw new RuntimeException("Invalid OTP.");
            }

        } catch (Exception e) {
            logger.error("Error verifying OTP for {}: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("Error verifying OTP: " + e.getMessage(), e);
        }
    }
}
