package com.catalyst.ProCounsellor.model;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class UserState {
	@DocumentId
    private String userName;
	private String state;
}
