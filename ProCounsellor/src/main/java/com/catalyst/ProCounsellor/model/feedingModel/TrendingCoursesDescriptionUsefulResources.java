package com.catalyst.ProCounsellor.model.feedingModel;

import java.util.List;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class TrendingCoursesDescriptionUsefulResources {
	@DocumentId
	private String trendingCoursesDescriptionUsefulResourcesId;
	private List<String> books;
    private List<String> websites;
    private List<String> tools;
}
