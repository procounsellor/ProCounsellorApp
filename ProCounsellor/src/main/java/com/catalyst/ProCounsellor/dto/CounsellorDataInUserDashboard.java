package com.catalyst.ProCounsellor.dto;

import lombok.Data;

@Data
public class CounsellorDataInUserDashboard {
    private String counsellorName;
    private String firstName;
    private String lastName;
    private String photoUrlSmall;
    private Integer rating;
    private String chargesPerYear;
}
