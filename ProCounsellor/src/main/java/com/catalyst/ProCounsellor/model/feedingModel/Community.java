package com.catalyst.ProCounsellor.model.feedingModel;

import java.util.List;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class Community {
	@DocumentId
	private String communityId;
    private String name;
    private int members;
    private String description;
    private String image;
    private List<String> listOfPostIdInCommunity;
}
