package com.catalyst.ProCounsellor.model;

import java.util.List;
import java.util.Map;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class User {
	@DocumentId
    private String userName;
	
    private String firstName;   
    private String lastName;   
    private String phoneNumber; 
    private String email;   
    private String password;
    private Long walletAmount;
    private BankDetails bankDetails;
    private String role;
    private List<ActivityLog> activityLog;
    private List<CallHistory> callHistory;
    private String photo;
    private List<AllowedStates> userInterestedStateOfCounsellors;
    //mandatory field
    private Courses interestedCourse;//right now, allowing only one degree interest per user. //Aligning with counsellor's expertise
    private List<String> subscribedCounsellorIds; 
    private List<String> followedCounsellorsIds;
    private List<College> interestedColleges;
    private List<String> interestedLocationsForCollege;
    private List<String> userReviewIds;
    private List<Map<String,String>> chatIdsCreatedForUser;
    private List<String> languagesKnow;
    private String fcmToken;
}

       
