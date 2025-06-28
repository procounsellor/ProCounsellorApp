package com.catalyst.ProCounsellor.dto;

public class MailOtpRequest {
    private String email;
    private String otp; // optional during OTP send

    // Constructors
    public MailOtpRequest() {}

    public MailOtpRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
