package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PER_TEMP_VERIFICATION")
@Data
public class TempVerification {

    @Id
    @Column(name = "PER_TEMP_VERIFICATION_ID", nullable = false, precision = 18)
    private Long tempVerificationId;

    @Column(name = "USER_ROLE", nullable = false, length = 20)
    private String userRole;

    @Column(name = "METHOD", nullable = false, length = 20)
    private String method;

    @Column(name = "OTP_SENT", nullable = false, length = 10)
    private String otpSent;

    @Column(name = "OTP_VERIFIED")
    private Boolean otpVerified;

    @Column(name = "VERIFICATION_TIME")
    private LocalDateTime verificationTime;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;

    @Column(name = "PHONE_NUMBER", nullable = false, precision = 10)
    private Long phoneNumber;

    @Column(name = "EMAIL", length = 150)
    private String email;

    @Column(name = "FAILED_ATTEMPTS")
    private Integer failedAttempts;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "LAST_UPDATED_AT")
    private LocalDateTime lastUpdatedAt;
}
