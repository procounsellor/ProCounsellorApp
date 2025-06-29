package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "HRC_BOOKING_APPOINTMENTS")
@Data
public class BookingAppointment {

    @Id
    @Column(name = "APPOINTMENT_ID", nullable = false, precision = 18)
    private Long appointmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private PerAllPeople perAllPeople;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUNSELLOR_ID", nullable = false)
    private CounsellorProfile counsellorProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SLOT_ID", nullable = false)
    private AvailabilitySlot availabilitySlot;

    @Column(name = "SLOT_DATE", nullable = false)
    private LocalDate slotDate;

    @Column(name = "START_TIME", nullable = false)
    private LocalTime startTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalTime endTime;

    @Column(name = "BOOKED_AT")
    private LocalDateTime bookedAt;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "CANCELLATION_REASON", length = 255)
    private String cancellationReason;

    @Column(name = "RESCHEDULED_FROM_ID", precision = 18)
    private Long rescheduledFromId;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;
}
