package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "HRC_COUNSELLOR_WORKING_DAYS")
@Data
public class CounsellorWorkingDay {

    @Id
    @Column(name = "WORKING_DAY_ID", nullable = false, precision = 18)
    private Long workingDayId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUNSELLOR_ID", nullable = false)
    private CounsellorProfile counsellorProfile;

    @Column(name = "DAY_OF_WEEK", nullable = false, length = 10)
    private String dayOfWeek;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;
}