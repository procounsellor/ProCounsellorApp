package com.catalyst.ProCounsellor.model;

import java.util.Date;

import com.google.cloud.firestore.annotation.ServerTimestamp;

import lombok.Data;

@Data
public class Message {
	private String id; // Firestore document ID
    private String chatId;
    private String senderId;
    private String text;

    @ServerTimestamp
    private Date timestamp;
}
