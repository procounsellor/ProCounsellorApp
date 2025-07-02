package com.catalyst.ProCounsellor.model;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class Course {
	@DocumentId
    private String courseId;
	private String name;
    private String image;   
    private String description;
    private String duration;
    private String tagline;
//    Student enrolled with us //later
//    Rating //later
}
