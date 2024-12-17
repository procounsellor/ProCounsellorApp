package com.catalyst.ProCounsellor.service;

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class FirebaseService {
	 public User getUserById(String userId) throws ExecutionException, InterruptedException {
	        Firestore firestore = FirestoreClient.getFirestore();
	        DocumentSnapshot snapshot = firestore.collection("users").document(userId).get().get();
	        return snapshot.exists() ? snapshot.toObject(User.class) : null;
	    }

	    public void updateUser(User user) throws ExecutionException, InterruptedException {
	        Firestore firestore = FirestoreClient.getFirestore();
	        firestore.collection("users").document(user.getUserName()).set(user).get();
	    }
	    
	    public Counsellor getCounsellorById(String counsellorId) throws ExecutionException, InterruptedException {
	    	Firestore firestore = FirestoreClient.getFirestore();
	        DocumentSnapshot snapshot = firestore.collection("counsellors").document(counsellorId).get().get();
	        return snapshot.exists() ? snapshot.toObject(Counsellor.class) : null;
	    }

	    public void updateCounsellor(Counsellor counsellor) throws ExecutionException, InterruptedException {
	    	Firestore firestore = FirestoreClient.getFirestore();
	        firestore.collection("counsellors").document(counsellor.getUserName()).set(counsellor).get();
	    }

}
