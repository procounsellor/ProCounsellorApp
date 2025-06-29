package com.catalyst.ProCounsellor.modelNew;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "PER_ALL_PEOPLE")
@Data
public class PerAllPeople {
    @Id
    @Column(name = "PERSON_ID", nullable = false, precision = 18)
    private Long personId;

    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    private String firstName;

    @Column(name = "MIDDLE_NAME", length = 100)
    private String middleName;

    @Column(name = "LAST_NAME", length = 100)
    private String lastName;

    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    @Column(name = "PHONE_NUMBER", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "IS_EMAIL_VERIFIED", nullable = false)
    private Boolean isEmailVerified;

    @Column(name = "IS_PHONE_VERIFIED", nullable = false)
    private Boolean isPhoneVerified;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;

    @Column(name = "ROLE_NAME", nullable = false, length = 30)
    private String roleName;

}
