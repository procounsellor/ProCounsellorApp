package com.catalyst.ProCounsellor.model.feedingModel;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class CollegeRanking {
	@DocumentId
	private String collegeId;
	private String name;
    private String city;
    private int rank;
    private String state;
    private String category;
    private String description;
    private String imageUrl;
}
