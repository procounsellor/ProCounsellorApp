package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PER_VERIFICATION_LOGS")
@Data
public class VerificationLog {

    @Id
    @Column(name = "VERIFICATION_LOGS_ID", nullable = false, precision = 18)
    private Long verificationLogsId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private PerAllPeople perAllPeople;

    @Column(name = "METHOD", nullable = false, length = 20)
    private String method;

    @Column(name = "OTP_SENT", nullable = false, length = 10)
    private Boolean otpSent;

    @Column(name = "OTP_VERIFIED")
    private Boolean otpVerified;

    @Column(name = "VERIFICATION_TIME")
    private LocalDateTime verificationTime;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;
}