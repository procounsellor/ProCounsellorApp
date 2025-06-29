package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PER_PERSON_PROFILE_PHOTOS")
@Data
public class PersonProfilePhoto {

    @Id
    @Column(name = "PROFILE_PHOTOS_ID", nullable = false, precision = 18)
    private Long profilePhotosId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private PerAllPeople perAllPeople;

    @Column(name = "PHOTO_TYPE", nullable = false, length = 50)
    private String photoType;

    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;

    @Column(name = "SMALL_DIMENSION", nullable = false, length = 255)
    private String smallDimension;

    @Column(name = "LARGE_DIMENSION", nullable = false, length = 255)
    private String largeDimension;

}
