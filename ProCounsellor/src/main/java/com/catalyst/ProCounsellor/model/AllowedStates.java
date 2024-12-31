package com.catalyst.ProCounsellor.model;

public enum AllowedStates {
	KARNATAKA("Karnataka"),
	MAHARASHTRA("Maharashtra"),
	TAMILNADU("Tamil Nadu"), 
	OTHERS("Others");

    private final String description;

    AllowedStates(String description) {
        this.description = description;
    }
}
