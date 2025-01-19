package com.catalyst.ProCounsellor.service;


import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Admin;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class AdminService {

    private static final String ADMINS = "admins";

    // Signup functionality
    public String signup(Admin user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(ADMINS).document(user.getUserName());

        // Check if user already exists
        if (documentReference.get().get().exists()) {
            return "User already exists with ID: " + user.getUserName();
        }

        // Save new user
        ApiFuture<WriteResult> collectionsApiFuture = documentReference.set(user);
        return "Signup successful! User ID: " + user.getUserName();
    }

    // Signin functionality
    public HttpStatus signin(String identifier, String password) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference adminsCollection = dbFirestore.collection(ADMINS);

        // Determine the identifier type and query the Firestore
        Query query;
        if (identifier.contains("@")) {
            query = adminsCollection.whereEqualTo("email", identifier);
        } else if (identifier.contains("+91")) {
            query = adminsCollection.whereEqualTo("phoneNumber", identifier);
        } else {
            DocumentReference docRef = adminsCollection.document(identifier);

            // Check if the document exists
            DocumentSnapshot documentSnapshot = docRef.get().get();
            if (documentSnapshot.exists()) {
                Admin existingAdmin = documentSnapshot.toObject(Admin.class);

                // Validate the password
                if (existingAdmin.getPassword().equals(password)) {
                    return HttpStatus.OK;
                } else {
                    throw new InvalidCredentialsException("Invalid credentials provided.");
                }
            } else {
                throw new UserNotFoundException("Admin not found for userName: " + identifier);
            }
        }

        // Execute the query for email or phoneNumber
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        if (!documents.isEmpty()) {
            QueryDocumentSnapshot document = documents.get(0);
            Admin existingAdmin = document.toObject(Admin.class);

            if (existingAdmin.getPassword().equals(password)) {
                return HttpStatus.OK;
            } else {
                throw new InvalidCredentialsException("Invalid credentials provided.");
            }
        } else {
            throw new UserNotFoundException("Admin not found for the provided credentials.");
        }
    }
    
    public String getAdminId(String identifier) throws InterruptedException, ExecutionException {
	    Firestore dbFirestore = FirestoreClient.getFirestore();
	    CollectionReference adminsCollection = dbFirestore.collection(ADMINS);
	    Query query;

	    if (identifier.matches("^.+@.+\\..+$")) {
	        query = adminsCollection.whereEqualTo("email", identifier).limit(1);
	    } else if (identifier.matches("^\\+91\\d{10}$")) {
	        query = adminsCollection.whereEqualTo("phoneNumber", identifier).limit(1);
	    } else {
	        return identifier;
	    }

	    List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

	    if (!documents.isEmpty()) {
	        Admin admin = documents.get(0).toObject(Admin.class);
	        return admin.getUserName();
	    }

	    throw new UserNotFoundException("No admin found for identifier: " + identifier);
	}
    
	 public Admin getAdminById(String adminId) throws ExecutionException, InterruptedException {
		 	Firestore firestore = FirestoreClient.getFirestore();
	        DocumentSnapshot snapshot = firestore.collection("admins").document(adminId).get().get();
	        return snapshot.exists() ? snapshot.toObject(Admin.class) : null;
	    }
 
}
