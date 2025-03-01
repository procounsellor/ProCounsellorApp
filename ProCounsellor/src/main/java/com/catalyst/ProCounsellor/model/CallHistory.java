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
    private long startTime;
    private long pickedTime;
    private long endTime; 
    private String duration; // Change from int to String
    private String callType;
}