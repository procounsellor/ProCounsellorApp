package com.catalyst.ProCounsellor.model;

import lombok.Data;

@Data
public class Address {
	private String houseNumber;
	private String floorNumber;
	private String streetName; 
	private String areaName;
	private String city;
	private String state;
	private String coordinates;
	private Integer pinCode;
}
