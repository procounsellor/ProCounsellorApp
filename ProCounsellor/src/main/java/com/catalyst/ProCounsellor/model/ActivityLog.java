package com.catalyst.ProCounsellor.model;

import com.google.cloud.Timestamp;

import lombok.Data;

@Data
public class ActivityLog {
    private String activity;
    private Timestamp timestamp;
}
