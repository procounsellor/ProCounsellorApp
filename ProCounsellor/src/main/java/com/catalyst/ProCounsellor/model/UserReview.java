package com.catalyst.ProCounsellor.model;

import java.util.List;

import lombok.Data;

@Data
public class UserReview {
	private String userName;
	private String counsellorName;
	private String review;
	private List<String> replies;
	private String likes;
}
