package com.catalyst.ProCounsellor.model;

public enum DegreeType {
    ELEVENTH_DEGREE("11th Degree"),
    GRADUATE_DEGREE("Graduate Degree"),
    POST_GRADUATE_DEGREE("Post Graduate Degree");

    private final String description;

    DegreeType(String description) {
        this.description = description;
    }

}