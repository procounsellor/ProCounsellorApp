package com.catalyst.ProCounsellor.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.catalyst.ProCounsellor.model.UserReviewComments;
import com.google.cloud.Timestamp;

import lombok.Data;

@Data
public class SendCounsellorReviews {
	private String reviewId;
    private String userName;
    private String counsellorName;
    private String userFullName;
    private String userPhotoUrl;
    private String reviewText;
    private List<String> userIdsLiked = new ArrayList<>(); 
    private double rating;
    private Timestamp timestamp;
    private Integer noOfLikes;
    private List<Map<String, Object>> comments;

}
