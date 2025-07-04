package com.catalyst.ProCounsellor.dto;

import lombok.Data;

@Data
public class AppointmentBookingRequest {
    private String userId;
    private String counsellorId;
    private String date; // yyyy-MM-dd
    private String startTime; // HH:mm
    private String mode; // call, video, chat, office_visit
    private String notes;
}
