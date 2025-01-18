package com.catalyst.ProCounsellor.model;

import java.util.ArrayList;
import java.util.List;

import com.google.cloud.Timestamp;

import lombok.Data;

@Data
public class UserReview {
	private String reviewId;
    private String userName;
    private String counsellorName; 
    private String reviewText;
    private List<String> userIdsLiked = new ArrayList<>(); 
    private float rating;
    private Timestamp timestamp;
    private Integer noOfLikes;
    private List<UserReviewComments> comments;
}