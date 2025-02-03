package com.catalyst.ProCounsellor.model;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

import java.util.Date;

@Data
public class CallHistory {
    @DocumentId
    private String callId;
    
    private String callerId;
    private String receiverId;
    private String status;
    private Date startTime;
    private Date endTime;
    private int duration;
    private String callType;
}