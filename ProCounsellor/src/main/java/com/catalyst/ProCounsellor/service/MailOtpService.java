package com.catalyst.ProCounsellor.service;


import com.catalyst.ProCounsellor.dto.MailOtpEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class MailOtpService {
	private static final Logger logger = LoggerFactory.getLogger(MailOtpService.class);
    private final Map<String, MailOtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Random random = new Random();
    

    @Autowired
    private JavaMailSender mailSender;

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        otpStore.put(email, new MailOtpEntry(otp, LocalDateTime.now()));

        logger.info("Generated OTP for email {}: {}", email, otp); // avoid logging OTP in production

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp + "\nIt is valid for 5 minutes.");
            message.setFrom("your_email@gmail.com");

            mailSender.send(message);
            logger.info("OTP email sent to {}", email);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }

        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        MailOtpEntry entry = otpStore.get(email);
        if (entry == null) {
            logger.warn("No OTP entry found for email: {}", email);
            return false;
        }

        if (!entry.getOtp().equals(otp)) {
            logger.warn("Invalid OTP provided for email: {}", email);
            return false;
        }

        long ageInSeconds = Duration.between(entry.getCreatedAt(), LocalDateTime.now()).toSeconds();
        if (ageInSeconds >= 300) {
            logger.warn("Expired OTP for email: {}", email);
            return false;
        }

        logger.info("OTP verified successfully for email: {}", email);
        return true;
    }

//    public void registerOrLoginWithFirebase(String email) throws FirebaseAuthException {
//        UserRecord user;
//        try {
//            user = FirebaseAuth.getInstance().getUserByEmail(email);
//        } catch (FirebaseAuthException e) {
//            user = FirebaseAuth.getInstance().createUser(new CreateRequest().setEmail(email));
//        }
//
//        String customToken = FirebaseAuth.getInstance().createCustomToken(user.getUid());
//        System.out.println("Firebase custom token: " + customToken);
//    }
}
