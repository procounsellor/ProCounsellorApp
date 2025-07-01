package com.catalyst.ProCounsellor.requestValidator;

import org.apache.commons.lang3.StringUtils;
import com.catalyst.ProCounsellor.constant.AppConstants;

public class GenerateOTPRequestValidator {
	public static void validateInput(String phoneNumber) {
	    if (StringUtils.isBlank(phoneNumber)) {
	        throw new IllegalArgumentException("Phone number must not be empty.");
	    }

	    String digitsOnly = phoneNumber.replaceAll("\\D", "");

	    if (!StringUtils.isNumeric(digitsOnly)) {
	        throw new IllegalArgumentException("Phone number must contain only digits.");
	    }

	    if (digitsOnly.length() < 10 || digitsOnly.length() > 13) {
	        throw new IllegalArgumentException("Phone number must be between 10 to 13 digits.");
	    }
	}
}
