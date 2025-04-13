package com.catalyst.ProCounsellor.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String senderId;
    private String text;
    private String receiverFcmToken;
}
