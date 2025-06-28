package com.catalyst.ProCounsellor.service;


import com.catalyst.ProCounsellor.dto.MailOtpEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;

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
    private final Map<String, MailOtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Random random = new Random();
    

    @Autowired
    private JavaMailSender mailSender;

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        otpStore.put(email, new MailOtpEntry(otp, LocalDateTime.now()));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\nIt is valid for 5 minutes.");
        message.setFrom("your_email@gmail.com"); // optional

        mailSender.send(message);
        return otp;
    }


    public boolean verifyOtp(String email, String otp) {
    	MailOtpEntry entry = otpStore.get(email);
        return entry != null &&
               entry.getOtp().equals(otp) &&
               Duration.between(entry.getCreatedAt(), LocalDateTime.now()).toSeconds() < 300;
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
