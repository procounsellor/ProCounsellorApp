package com.catalyst.ProCounsellor.model;

import java.util.List;
import java.util.Map;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class Counsellor {
	@DocumentId
    private String userName;
	
    private String firstName;   
    private String lastName;   
    private String phoneNumber; 
    private String description;// to be written in third party
    private String email;
    private Long walletAmount;
    private List<Transaction> transactions;
    private BankDetails bankDetails;
    private String photoUrl;
    private String password;
    private String organisationName;
    private String experience;
    private String role;
    private List<ActivityLog> activityLog;
    private List<CallHistory> callHistory;
    private AllowedStates stateOfCounsellor;
    private List<Map<String,String>> chatIdsCreatedForCounsellor;
    private Double ratePerYear;
    private List<Courses> expertise; //align with user's interested course
    private Integer noOfClients;
    private Integer noOfFollowers;
    private List<String> clientIds;
    private List<String> followerIds;
    private Integer rating;
    private List<String> languagesKnow;
    private List<String> reviewIds;
    private String minuteSpendOnCall;
    private String minuteSpendOnVideoCall;
    //to be verified by admin;
    private boolean isVerified;
    private StateType state;
    private String fcmToken;
    private String voipToken;
    private String platform;
}
