package com.catalyst.ProCounsellor.model.feedingModel;

import lombok.Data;

@Data
public class CommunityPostComment {
	private String postId;
    private String author;
    private String content;
    private String timestamp;
}