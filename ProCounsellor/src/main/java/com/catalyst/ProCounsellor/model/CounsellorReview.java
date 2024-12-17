package com.catalyst.ProCounsellor.model;

import java.util.List;

import lombok.Data;

@Data
public class CounsellorReview {
	private String username;
	private float rating;
	private String review;
	private List<String> replies;
	private Integer likes;
}
