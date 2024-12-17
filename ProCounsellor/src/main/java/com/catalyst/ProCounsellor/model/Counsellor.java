package com.catalyst.ProCounsellor.model;

import java.util.List;

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
    private String photoUrl;// to be changed in the proper format
    private String password;
    private String organisationName;
    private String experience;
    private String role;
    private Address address;
    private Double ratePerMinuteCall;
    private Double ratePerMinuteVideoCall;
    private Double ratePerMinuteChat;
    private String noOfClients;
    private List<String> clientIds;
    private List<String> followerIds;
    private Double rating;
    private String noOfFollowers;
    private List<String> languagesKnow;
    private List<CounsellorReview> reviews;
    private String minuteSpendOnChat;
    private String minuteSpendOnCall;
    private String minuteSpendOnVideoCall;
    //to be verified by admin;
    private boolean isVerified;
    private String cityOfCounsellor;
    private StateType state;
}
