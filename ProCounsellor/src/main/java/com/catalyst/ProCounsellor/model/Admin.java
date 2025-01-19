package com.catalyst.ProCounsellor.model;

import lombok.Data;

@Data
public class Admin {
	private String userName;      
    private String firstName;   
    private String lastName;   
    private String photoUrl;
    private String phoneNumber; 
    private String email;   
    private String password;
    private String role;
}
