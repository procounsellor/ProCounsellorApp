package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PER_BOOKING_HISTORY")
@Data
public class BookingHistory {

    @Id
    @Column(name = "BOOKING_HISTORY_ID", nullable = false, precision = 18)
    private Long bookingHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "APPOINTMENT_ID", nullable = false)
    private BookingAppointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private PerAllPeople perAllPeople;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUNSELLOR_ID", nullable = false)
    private CounsellorProfile counsellorProfile;

    @Column(name = "ACTION", nullable = false, length = 30)
    private String action;

    @Column(name = "ACTION_TIMESTAMP")
    private LocalDateTime actionTimestamp;

    @Column(name = "ACTOR", nullable = false, length = 20)
    private String actor;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;
}
