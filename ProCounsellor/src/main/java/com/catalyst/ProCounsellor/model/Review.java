package com.catalyst.ProCounsellor.model;

import java.util.List;

import lombok.Data;

@Data
public class Review {
	private String userName;
	private String review;
	private List<String> replies;
	private String likes;
}
