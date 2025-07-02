package com.catalyst.ProCounsellor.model;


import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;
@Data
public class States {
	@DocumentId
    private String stateId;
	private String name;
    private String image;   
}
