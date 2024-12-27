package com.catalyst.ProCounsellor.model;

import java.util.List;

import com.google.cloud.Timestamp;

import lombok.Data;

@Data
public class UserReview {
	private String reviewId;
    private String userName;
    private String photoUrl;
    private String counsellorName; 
    private String reviewText;
    private float rating;
    private Timestamp timestamp;
    private Integer noOfLikes;
    private List<UserReviewComments> comments;
}