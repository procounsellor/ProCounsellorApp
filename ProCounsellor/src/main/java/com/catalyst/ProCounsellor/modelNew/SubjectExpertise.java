package com.catalyst.ProCounsellor.modelNew;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "HRC_SUBJECT_EXPERTISE")
@Data
public class SubjectExpertise {

    @Id
    @Column(name = "SUBJECT_ID", nullable = false, precision = 18)
    private Long subjectId;

    @Column(name = "SUBJECT_CODE", nullable = false, length = 50)
    private String subjectCode;

    @Column(name = "SUBJECT_NAME", nullable = false, length = 100)
    private String subjectName;
}
