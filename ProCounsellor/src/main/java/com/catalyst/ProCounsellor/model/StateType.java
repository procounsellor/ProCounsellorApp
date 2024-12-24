package com.catalyst.ProCounsellor.model;

public enum StateType {
    ONLINE("online"),
    OFFLINE("offline");

    private final String description;

    StateType(String description) {
        this.description = description;
    }

}