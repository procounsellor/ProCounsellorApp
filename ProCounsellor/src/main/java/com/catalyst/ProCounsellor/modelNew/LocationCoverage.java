package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "HRC_LOCATION_COVERAGE")
@Data
public class LocationCoverage {

    @Id
    @Column(name = "LOCATION_COVERAGE_ID", nullable = false, precision = 18)
    private Long locationCoverageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUNSELLOR_ID", nullable = false)
    private CounsellorProfile counsellorProfile;

    @Column(name = "COVERAGE_SCOPE", nullable = false, length = 50)
    private String coverageScope;

    @Column(name = "STATE_NAME", length = 100)
    private String stateName;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;
}
