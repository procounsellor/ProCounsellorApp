package com.catalyst.ProCounsellor.model;

import java.util.List;

import com.google.cloud.Timestamp;

import lombok.Data;

@Data
public class UserReview {
    private String userName;
    private String counsellorName; 
    private String reviewText;
    private int rating;
    private Timestamp timestamp;
    private Integer likes;
    private List<String> replies;
}