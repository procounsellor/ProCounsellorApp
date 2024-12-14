package com.catalyst.ProCounsellor.model;

import java.util.List;

import lombok.Data;

@Data
public class College {
	private String collegeId;
	private String collegeName;
	private String collegeCity;
	private String collegeAddress;
	private List<String> coursesOfferedByCollege;
}
