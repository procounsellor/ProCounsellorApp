package com.catalyst.ProCounsellor.model.feedingModel;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class TrendingCourses {
	@DocumentId
	private String courseId;
	private String name;
    private String category;
    private TrendingCoursesDescription description;
    private String imageUrl;
}
