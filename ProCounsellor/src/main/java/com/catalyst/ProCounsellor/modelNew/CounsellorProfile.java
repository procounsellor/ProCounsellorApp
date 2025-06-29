package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "HRC_COUNSELLOR_PROFILES")
@Data
public class CounsellorProfile {

    @Id
    @Column(name = "COUNSELLOR_ID", nullable = false, precision = 18)
    private Long counsellorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private PerAllPeople perAllPeople;

    @Column(name = "CHARGES_PER_MINUTE", nullable = false, precision = 6)
    private BigDecimal chargesPerMinute;

    @Column(name = "CHARGES_PER_SESSION", nullable = false, precision = 6)
    private BigDecimal chargesPerSession;

    @Column(name = "STANDALONE_CERTIFIED")
    private Boolean standaloneCertified;

    @Column(name = "ORGANIZATION_NAME", length = 255)
    private String organizationName;

    @Column(name = "ORGANIZATION_VERIFIED")
    private Boolean organizationVerified;

    @Column(name = "MAP_ADDRESS", length = 500)
    private String mapAddress;

    @Column(name = "MAP_COORDINATES", length = 100)
    private String mapCoordinates;

    @Column(name = "MAP_URL", length = 255)
    private String mapUrl;

    @Column(name = "PROFILE_CREATED")
    private LocalDateTime profileCreated;

    @Column(name = "PROFILE_UPDATED")
    private LocalDateTime profileUpdated;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;
}
