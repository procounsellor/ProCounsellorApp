package com.catalyst.ProCounsellor.model;

import java.util.List;

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
    private String role; 
    private String photo;
    private List<String> subscribedCounsellorIds; 
    private List<String> followedCounsellorsIds;
    private Double balance; 
    private Address address;
    private boolean isConverted;
    private DegreeType degreeType;
    private Stream stream; 
    private List<Degree> interestedDegree;
    private List<College> interestedColleges;
    private List<String> interestedLocationsForCollege;
    private List<String> userReviewIds;
}

       
