package com.catalyst.ProCounsellor.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class AppointmentBooking {
    @DocumentId
    private String appointmentId;

    private String userId;
    private String counsellorId;

    private String date; // Format: yyyy-MM-dd
    private String startTime; // Format: HH:mm
    private String endTime;   // Format: HH:mm

    private String mode; // e.g., "call", "video", "chat"
    private String status; // "booked", "cancelled", "reschedule,"completed","user didnt come"
    
    private String notes; // Optional note from user
    private String counsellorRemarks;

    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    private boolean userAttended;//counsellor will set it after completion of the meeting as true or false
    
    private String userRating;
    private String userFeedback;

}
