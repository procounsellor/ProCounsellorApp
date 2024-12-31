package com.catalyst.ProCounsellor.model;

public enum Courses {
	HSC("HSC"),
	ENGINEERING("Engineering"),
	MEDICAL("Medical"), 
	MBA("MBA"),
	OTHERS("Others");

    private final String description;

    Courses(String description) {
        this.description = description;
    }

}
