package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "HRC_ORGANIZATION_VERIFICATION")
@Data
public class OrganizationVerification {

    @Id
    @Column(name = "ORGANIZATION_VERIFICATION_ID", nullable = false, precision = 18)
    private Long organizationVerificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUNSELLOR_ID", nullable = false)
    private CounsellorProfile counsellorProfile;

    @Column(name = "VERIFIED_BY_ADMIN", nullable = false)
    private Boolean verifiedByAdmin;

    @Column(name = "VERIFIED_BY", length = 100)
    private String verifiedBy;

    @Column(name = "VERIFICATION_TIME")
    private LocalDateTime verificationTime;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;
}
