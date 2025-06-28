package com.catalyst.ProCounsellor.dto;

import java.time.LocalDateTime;

public class MailOtpEntry {
    private String otp;
    private LocalDateTime createdAt;

    public MailOtpEntry(String otp, LocalDateTime createdAt) {
        this.otp = otp;
        this.createdAt = createdAt;
    }

    public String getOtp() {
        return otp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
