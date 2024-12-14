package com.catalyst.ProCounsellor.model;

import java.util.List;

import lombok.Data;

@Data
public class Degree {
	private String degreeId;
	private String degreeName;
	private List<String> streamRequiredForDegree;
}
