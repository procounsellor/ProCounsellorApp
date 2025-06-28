package com.catalyst.ProCounsellor.model;

import lombok.Data;

@Data
public class Address {
	private String addressId;
	private String userId;
	private String role;
	private String houseNumber;
	private String floorNumber;
	private String streetName; 
	private String areaName;
	private String city;
	private String state;
	private String pinCode;
	private String latCoordinate;
	private String longCoordinate;
	private String mapLink;
}
