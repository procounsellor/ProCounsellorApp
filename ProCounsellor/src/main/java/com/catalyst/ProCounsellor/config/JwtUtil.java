package com.catalyst.ProCounsellor.config;

import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Component;

import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.CounsellorService;
import com.catalyst.ProCounsellor.service.UserService;

@Component
public class JwtUtil {

    private static UserService userService;
    private static CounsellorService counsellorService;

    public JwtUtil(UserService userService, CounsellorService counsellorService) {
        JwtUtil.userService = userService;
        JwtUtil.counsellorService = counsellorService;
    }

    public static User getAuthenticatedUser(HttpServletRequest request) throws ExecutionException, InterruptedException {
        String phoneNumber = (String) request.getAttribute("phoneNumber");
        if (phoneNumber == null) throw new RuntimeException("Missing or invalid token");

        User user = userService.getUserFromPhoneNumber(phoneNumber);
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }

    public static Counsellor getAuthenticatedCounsellor(HttpServletRequest request) throws ExecutionException, InterruptedException {
        String phoneNumber = (String) request.getAttribute("phoneNumber");
        if (phoneNumber == null) throw new RuntimeException("Missing or invalid token");

        Counsellor counsellor = counsellorService.getCounsellorById(phoneNumber);
        if (counsellor == null) throw new RuntimeException("Counsellor not found");
        return counsellor;
    }
}

