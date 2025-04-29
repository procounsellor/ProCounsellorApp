package com.catalyst.ProCounsellor.model.feedingModel;

import java.util.List;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class CommunityPost {
	@DocumentId
	private String postId;
    private String communityId;
    private String author;
    private String type;
    private String content;
    private String timestamp;
    private String image; //if they want to attach image as well
    private List<CommunityPostComment> comments;
}
