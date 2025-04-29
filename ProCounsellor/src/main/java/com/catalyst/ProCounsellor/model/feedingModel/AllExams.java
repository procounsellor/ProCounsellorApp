package com.catalyst.ProCounsellor.model.feedingModel;

import java.util.List;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class AllExams {
	@DocumentId
	private String examId;
	private String name;
	private String country;
    private String level;
    private String category; //engineering, medical, law, management etc
    private String organization;
    private String description;
    private String imageUrl;
    private String exam_type;
    private String eligibility;
    private String mode;
    private String frequency;
    private String official_website;
    private String exam_date;
    private String registration_deadline;
    private String duration;
    private List<String> subjects;
    private String marking_scheme;
}
