package com.catalyst.ProCounsellor.model;

import java.util.Date;

import com.google.cloud.firestore.annotation.ServerTimestamp;

import lombok.Data;

@Data
public class Chat {
	private String id; // Firestore document ID
    private String userId;
    private String counselorId;

    @ServerTimestamp
    private Date createdAt;

}
