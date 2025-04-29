package com.catalyst.ProCounsellor.model.feedingModel;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class CommunityPostComment {
	@DocumentId
	private String communityPostCommentId;
	private String postId;
    private String author;
    private String content;
    private String timestamp;
}