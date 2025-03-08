package com.catalyst.ProCounsellor.model;

import java.util.List;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;
import lombok.var;

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
    private BankDetails bankDetails;
    private String photoUrl;
    private String password;
    private String organisationName;
    private String experience;
    private String role;
    private List<ActivityLog> activityLog;
    private List<CallHistory> callHistory;
    private AllowedStates stateOfCounsellor;
    private List<String> chatIdsCreatedForCounsellor;
    private Double ratePerYear;
    private List<Courses> expertise; //align with user's interested course
    private Integer noOfClients;
    private Integer noOfFollowers;
    private List<String> clientIds;
    private List<String> followerIds;
    private var rating;
    private List<String> languagesKnow;
    private List<String> reviewIds;
    private String minuteSpendOnCall;
    private String minuteSpendOnVideoCall;
    //to be verified by admin;
    private boolean isVerified;
    private StateType state;
}
