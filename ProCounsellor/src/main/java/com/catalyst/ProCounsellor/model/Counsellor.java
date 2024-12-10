package com.catalyst.ProCounsellor.model;

import lombok.Data;

@Data
public class Counsellor {
    private String userName;      
    private String firstName;   
    private String lastName;   
    private String phoneNumber; 
    private String email;   
    private String password;
    private String organisationName;
    private String experience;
    //make enum
    private String role;
      
}
