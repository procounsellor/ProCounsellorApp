package com.catalyst.ProCounsellor.model;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class CounsellorState {
	@DocumentId
    private String counsellorName;
	private String state;
}
