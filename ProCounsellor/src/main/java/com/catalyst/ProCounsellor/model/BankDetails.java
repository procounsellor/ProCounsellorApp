package com.catalyst.ProCounsellor.model;

import lombok.Data;

@Data
public class BankDetails {
    private String bankAccountNumber;
    private String ifscCode;
    private String fullName;
}
