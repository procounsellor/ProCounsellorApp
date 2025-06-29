package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PER_ADDRESSES")
@Data
public class Address {

    @Id
    @Column(name = "ADDRESS_ID", nullable = false, precision = 18)
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private PerAllPeople perAllPeople;

    @Column(name = "ADDRESS_TYPE", nullable = false, length = 20)
    private String addressType;

    @Column(name = "ADDRESS_TEXT", nullable = false, length = 500)
    private String addressText;

    @Column(name = "CITY", length = 100)
    private String city;

    @Column(name = "STATE", length = 100)
    private String state;

    @Column(name = "COUNTRY", length = 100)
    private String country;

    @Column(name = "PIN_CODE", length = 15)
    private String pinCode;

    @Column(name = "LATITUDE", length = 150)
    private String latitude;

    @Column(name = "LONGITUDE", length = 100)
    private String longitude;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;
}
