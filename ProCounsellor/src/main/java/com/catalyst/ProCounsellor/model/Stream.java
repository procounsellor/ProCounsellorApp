package com.catalyst.ProCounsellor.model;

public enum Stream {
    SCIENCE("Science"),
    COMMERCE("Commerce"),
    ARTS("Arts");

    private final String description;

    Stream(String description) {
        this.description = description;
    }

}