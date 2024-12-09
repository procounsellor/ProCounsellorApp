package com.catalyst.ProCounsellor.model;

import java.util.List;

import lombok.Data;

@Data
public class User {
    private String userName;      
    private String firstName;   
    private String lastName;   
    private String phoneNumber; 
    private String email;   
    private String password;
    private String role; 
    private List<Counsellor> subscribedCounsellors;
    private List<Counsellor> followedCounsellors;// to be brainstormed
    private Double balance; 
    private Address address;
    private boolean isConverted;
    private List<Degree> interestedDegree;
    private List<College> interestedColleges;
    
}

       
