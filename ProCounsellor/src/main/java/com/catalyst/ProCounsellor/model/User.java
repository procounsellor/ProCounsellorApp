package com.catalyst.ProCounsellor.model;

import java.util.List;
import java.util.Map;

import com.catalyst.ProCounsellor.model.feedingModel.CollegeRanking;
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
    private List<Transaction> transactions;
    private BankDetails bankDetails;
    private String role;
    private List<ActivityLog> activityLog;
    private List<CallHistory> callHistory;
    private String photo;
    private String photoSmall;
    private List<String> userInterestedStateOfCounsellors;
    //mandatory field
    private String interestedCourse; //right now, allowing only one degree interest per user. //Aligning with counsellor's expertise
    private List<String> subscribedCounsellorIds; 
    private List<String> followedCounsellorsIds;
    private List<String> friendIds;
    private List<CollegeRanking> interestedColleges;
    private List<String> interestedLocationsForCollege;
    private List<String> userReviewIds;
    private List<Map<String,String>> chatIdsCreatedForUser;
    private List<String> languagesKnow;
    private String fcmToken;
    private String voipToken;
    private String platform;
    private String currectCallUUID;// handle call cancel
    private List<String> appointmentIds;
}

       
