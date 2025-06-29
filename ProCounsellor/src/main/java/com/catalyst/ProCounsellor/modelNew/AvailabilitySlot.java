package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "HRC_AVAILABILITY_SLOTS")
@Data
public class AvailabilitySlot {

    @Id
    @Column(name = "SLOT_ID", nullable = false, precision = 18)
    private Long slotId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUNSELLOR_ID", nullable = false)
    private CounsellorProfile counsellorProfile;

    @Column(name = "SLOT_DATE", nullable = false)
    private LocalDate slotDate;

    @Column(name = "START_TIME", nullable = false)
    private LocalTime startTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalTime endTime;

    @Column(name = "DURATION_MINUTES", precision = 3)
    private Integer durationMinutes;

    @Column(name = "IS_BOOKED")
    private Boolean isBooked;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;

    @Column(name = "NON_AVAIL_START", nullable = false)
    private LocalDate nonAvailStart;

    @Column(name = "NON_AVAIL_END")
    private LocalDate nonAvailEnd;
}
