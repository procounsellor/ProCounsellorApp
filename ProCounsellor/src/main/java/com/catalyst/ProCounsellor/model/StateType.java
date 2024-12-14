package com.catalyst.ProCounsellor.model;

public enum StateType {
    ONLINE("Online"),
    OFFLINE("Offline"),
    TYPING("Typing");

    private final String description;

    StateType(String description) {
        this.description = description;
    }

}