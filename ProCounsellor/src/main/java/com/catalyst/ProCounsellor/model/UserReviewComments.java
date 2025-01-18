package com.catalyst.ProCounsellor.model;

import com.google.cloud.Timestamp;

import lombok.Data;

@Data
public class UserReviewComments {
	private String userReviewCommentId;
	private String userName;
	private String userFullName;
	private String photoUrl;
	private String commentText;
	private Timestamp timestamp;
}
