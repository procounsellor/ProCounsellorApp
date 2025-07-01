package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.CounsellorState;
import com.catalyst.ProCounsellor.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@Service
public class CounsellorService {
	
	@Autowired
	private SharedService sharedService;
	
    private static final String COUNSELLORS = "counsellors";
    
    Firestore firestore = FirestoreClient.getFirestore();
    
    public String applyPendingUpdates(String userName) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Step 1: Get the update map from 'updates/{userName}'
        DocumentSnapshot updatesSnapshot = db.collection("updates").document(userName).get().get();
        if (!updatesSnapshot.exists()) {
            throw new RuntimeException("No update document found for user: " + userName);
        }

        Map<String, Object> updates = updatesSnapshot.getData();
        if (updates == null || updates.isEmpty()) {
            throw new RuntimeException("Update data is empty for user: " + userName);
        }

        // Step 2: Remove metadata fields if needed
        updates.remove("lastUpdatedAt");  // Optional: remove Firestore internal fields

        // Step 3: Apply updates to counsellors/{userName}
        DocumentReference counsellorDoc = db.collection("counsellors").document(userName);
        ApiFuture<WriteResult> future = counsellorDoc.set(updates, SetOptions.merge());
        WriteResult result = future.get();
        
        ApiFuture<WriteResult> deleteFuture = db.collection("updates").document(userName).delete();
        deleteFuture.get(); // Wait for delete to complete


        return "Counsellor document updated successfully at " + result.getUpdateTime();
    }

    
    public String saveCounsellorUpdates(String userName, Map<String, Object> updates)
            throws ExecutionException, InterruptedException {

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException("Update map cannot be null or empty.");
        }

        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference updateDocRef = dbFirestore.collection("updates").document(userName);

        // Add a timestamp
        updates.put("lastUpdatedAt", FieldValue.serverTimestamp());

        // Merge the updates into the document
        ApiFuture<WriteResult> future = updateDocRef.set(updates, SetOptions.merge());
        WriteResult result = future.get();

        return "Updates saved successfully at " + result.getUpdateTime();
    }


    // Signup functionality
    public String counsellorSignup(Counsellor counsellor) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        counsellor.setUserName(counsellor.getPhoneNumber().replaceFirst("^\\+\\d{2}", ""));
        counsellor.setRole("counsellor");

        // Validate mandatory fields
        if (counsellor.getFirstName() == null || counsellor.getFirstName().isEmpty()) {
            return "First name is mandatory and cannot be null or empty.";
        }
        if (counsellor.getLastName() == null || counsellor.getLastName().isEmpty()) {
            return "Last name is mandatory and cannot be null or empty.";
        }
        if (counsellor.getPhoneNumber() == null || counsellor.getPhoneNumber().isEmpty()) {
            return "Phone number is mandatory and cannot be null or empty.";
        }
        if (counsellor.getEmail() == null || counsellor.getEmail().isEmpty()) {
            return "Email is mandatory and cannot be null or empty.";
        }
        if (counsellor.getPassword() == null || counsellor.getPassword().isEmpty()) {
            return "Password is mandatory and cannot be null or empty.";
        }
        if (counsellor.getRatePerYear() == null || counsellor.getRatePerYear() <= 0) {
            return "Rate per year must be greater than 0.";
        }
        if (counsellor.getStateOfCounsellor() == null || counsellor.getStateOfCounsellor().toString().isEmpty()) {
            return "State of counsellor cannot be null or empty.";
        }
        if (counsellor.getExpertise() == null || counsellor.getExpertise().isEmpty()) {
            return "Expertise cannot be null or empty.";
        }

        // Check for uniqueness of userName
        DocumentReference userDocRef = dbFirestore.collection(COUNSELLORS).document(counsellor.getUserName());
        if (userDocRef.get().get().exists()) {
            return "User already exists with userName: " + counsellor.getUserName();
        }

        // Check for uniqueness of phoneNumber and email
        CollectionReference counsellorsCollection = dbFirestore.collection(COUNSELLORS);
        Query phoneQuery = counsellorsCollection.whereEqualTo("phoneNumber", counsellor.getPhoneNumber());
        Query emailQuery = counsellorsCollection.whereEqualTo("email", counsellor.getEmail());

        if (!phoneQuery.get().get().isEmpty()) {
            return "Phone number already exists: " + counsellor.getPhoneNumber();
        }
        if (!emailQuery.get().get().isEmpty()) {
            return "Email already exists: " + counsellor.getEmail();
        }

        // Save new counsellor
        ApiFuture<WriteResult> collectionsApiFuture = userDocRef.set(counsellor);
        return "Signup successful! User ID: " + counsellor.getUserName();
    }


    // Signin functionality
    public HttpStatus counsellorSignin(String identifier, String password) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference counsellorsCollection = dbFirestore.collection(COUNSELLORS);

        // Determine the identifier type and query the Firestore
        Query query;
        if (identifier.contains("@")) {
            query = counsellorsCollection.whereEqualTo("email", identifier);
        } else if (identifier.contains("+91")) {
            query = counsellorsCollection.whereEqualTo("phoneNumber", identifier);
        } else {
            DocumentReference docRef = counsellorsCollection.document(identifier);

            // Check if the document exists
            DocumentSnapshot documentSnapshot = docRef.get().get();
            if (documentSnapshot.exists()) {
                Counsellor existingCounsellor = documentSnapshot.toObject(Counsellor.class);

                // Validate the password
                if (existingCounsellor.getPassword().equals(password)) {
                    return HttpStatus.OK;
                } else {
                    throw new InvalidCredentialsException("Invalid credentials provided.");
                }
            } else {
                throw new UserNotFoundException("Counsellor not found for userName: " + identifier);
            }
        }

        // Execute the query for email or phoneNumber
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        if (!documents.isEmpty()) {
            QueryDocumentSnapshot document = documents.get(0);
            Counsellor existingCounsellor = document.toObject(Counsellor.class);

            if (existingCounsellor.getPassword().equals(password)) {
                return HttpStatus.OK;
            } else {
                throw new InvalidCredentialsException("Invalid credentials provided.");
            }
        } else {
            throw new UserNotFoundException("Counsellor not found for the provided credentials.");
        }
    }


    
    public List<Counsellor> getAllCounsellors() {
        Firestore firestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> querySnapshot = firestore.collection(COUNSELLORS).get();

        List<Counsellor> counsellors = new ArrayList<>();
        try {
            for (QueryDocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            	Counsellor counsellor = doc.toObject(Counsellor.class);
            	counsellors.add(counsellor);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching all counsellors", e);
        }
        return counsellors;
    }
    
    public List<Counsellor> getAllCounsellorsSortedByRating() {
        Firestore firestore = FirestoreClient.getFirestore();
        Query query = firestore.collection(COUNSELLORS).orderBy("rating", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<Counsellor> counsellors = new ArrayList<>();
        try {
            for (QueryDocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            	Counsellor counsellor = doc.toObject(Counsellor.class);
            	counsellors.add(counsellor);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching counsellors sorted by rating", e);
        }
        return counsellors;
    }
    
    public List<Counsellor> getOnlineCounsellors() throws InterruptedException, ExecutionException {
        List<Counsellor> onlineCounsellorsList = new ArrayList<>();
        List<String> onlineCounsellorNames = new ArrayList<>();

        // Initialize Firebase Realtime Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("counsellorStates");

        // Create a CountDownLatch to block the thread until the data is fetched
        CountDownLatch latch = new CountDownLatch(1);

        // Fetch the data asynchronously
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                	Map<String, Object> counsellorData = (Map<String, Object>) childSnapshot.getValue();
                    String state = (String) counsellorData.get("state"); 
                    if ("online".equalsIgnoreCase(state)) {
                        onlineCounsellorNames.add(childSnapshot.getKey());
                    }
                }
                // Release the latch to allow the thread to continue
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error fetching online counsellors: " + databaseError.getMessage());
                latch.countDown(); // Release the latch even if there was an error
            }
        });

        // Block until the latch is released (data is fetched)
        latch.await();

        // Fetch the counsellor objects synchronously for each online counsellor
        for (String counsellorName : onlineCounsellorNames) {
            Counsellor counsellor = getCounsellorById(counsellorName);
            if (counsellor != null) {
                onlineCounsellorsList.add(counsellor);
            }
        }

        return onlineCounsellorsList;
    }
    
    public void updateUserPhotoUrl(String userId, String photoUrl) {
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection(COUNSELLORS).document(userId).update("photoUrl", photoUrl);
    }
    
    public List<User> getSubscribedClients(String counsellorId) {
        try {
            Counsellor counsellor = getCounsellorById(counsellorId);
            if (counsellor != null && counsellor.getClientIds() != null) {
                List<User> subscribedClients = new ArrayList<>();
                for (String userId : counsellor.getClientIds()) {
                    User user = sharedService.getUserById(userId);
                    if (user != null) {
                        subscribedClients.add(user);
                    }
                }
                return subscribedClients; // Return the list of full User objects
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if counsellor not found or error occurs
    }

	public List<User> getFollowers(String counsellorId) {
		try {
            Counsellor counsellor = getCounsellorById(counsellorId);
            if (counsellor != null && counsellor.getFollowerIds() != null) {
                List<User> followers = new ArrayList<>();
                for (String userId : counsellor.getFollowerIds()) {
                    User user = sharedService.getUserById(userId);
                    if (user != null) {
                    	followers.add(user);
                    }
                }
                return followers; // Return the list of full User objects
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if counsellor not found or error occurs
	}
	
	public boolean hasClient(String counsellorId, String userId) {
	    try {
	        Counsellor counsellor = getCounsellorById(counsellorId); // Retrieve the counsellor
	        if (counsellor != null && counsellor.getClientIds() != null) {
	            return counsellor.getClientIds().contains(userId); // Check if userId exists in client list
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return false; // Return false if counsellor or user not found
	}

	public boolean hasFollower(String counsellorId, String userId) {
		   try {
		        Counsellor counsellor = getCounsellorById(counsellorId); // Retrieve the counsellor
		        if (counsellor != null && counsellor.getFollowerIds() != null) {
		            return counsellor.getFollowerIds().contains(userId); // Check if userId exists in fs list
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    return false; // Return false if counsellor or user not found
	}
	
	public Counsellor updateCounsellorFields(String counsellorId, Map<String, Object> updates) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COUNSELLORS).document(counsellorId);

        // Perform the update
        ApiFuture<WriteResult> writeResult = docRef.update(updates);

        // Fetch the updated Counsellor
        DocumentSnapshot document = docRef.get().get();
        if (document.exists()) {
            return document.toObject(Counsellor.class);
        } else {
            throw new RuntimeException("Counsellor not found");
        }
    }
	
	
	/**
	 * Update the user state in Firebase Realtime Database.
	 *
	 * @param userName the counsellorName of the user
	 * @param state    the presence state to be updated
	 * @return true if update is successful
	 */
	 public boolean updateCounsellorState(String counsellorName, String state) {
	        try {
	            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("counsellorStates");

	            // Check if the user exists in Firestore
	            ApiFuture<DocumentSnapshot> future = firestore.collection(COUNSELLORS).document(counsellorName).get();
	            DocumentSnapshot document = future.get();

	            if (!document.exists()) {
	                System.err.println("Counsellor not found in Firestore: " + counsellorName);
	                return false; // User does not exist, deny the update
	            }

	            // Proceed to update the Realtime Database
	            CounsellorState counsellorState = new CounsellorState();
	            counsellorState.setCounsellorName(counsellorName);
	            counsellorState.setState(state);

	            databaseReference.child(counsellorName).setValueAsync(counsellorState);
	            return true;

	        } catch (Exception e) {
	            // Log error
	            System.err.println("Error updating counsellor state: " + e.getMessage());
	            return false;
	        }
	    }
	 
	 /**
	  * Check if the counsellor is online by fetching their state from Firebase Realtime Database.
	  *
	  * @param userName the counsellorName of the user
	  * @return true if the counsellor's state is "online", false otherwise
	  */
	 public boolean isCounsellorOnline(String counsellorName) throws InterruptedException {
		    // Create a CountDownLatch initialized to 1 (indicating that we need to wait for 1 event)
		    CountDownLatch latch = new CountDownLatch(1);

		    // Variable to hold the result
		    final boolean[] isOnline = {false};

		    // Get the reference to the user's state in the Realtime Database
		    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("counsellorStates").child(counsellorName);

		    // Add listener to fetch the user's state
		    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
		        @Override
		        public void onDataChange(DataSnapshot dataSnapshot) {
		            // Check if the counsellor exists and retrieve their state
		            if (dataSnapshot.exists()) {
		                CounsellorState counsellorState = dataSnapshot.getValue(CounsellorState.class);
		                if (counsellorState != null) {
		                    // Check if the state is 'online'
		                    isOnline[0] = "online".equalsIgnoreCase(counsellorState.getState());
		                }
		            } else {
		                System.err.println("Counsellor state not found in Realtime Database for: " + counsellorName);
		            }
		            
		            // Decrease the latch count, indicating that the operation has completed
		            latch.countDown();
		        }

		        @Override
		        public void onCancelled(DatabaseError databaseError) {
		            // Log error
		            System.err.println("Error fetching user state: " + databaseError.getMessage());
		            // Decrease the latch count in case of an error
		            latch.countDown();
		        }
		    });

		    // Wait for the latch to count down to 0 (i.e., for the callback to complete)
		    latch.await();

		    // Return the result after the callback completes
		    return isOnline[0];
		}

	 public String getCounsellorId(String identifier) throws InterruptedException, ExecutionException {
		    Firestore dbFirestore = FirestoreClient.getFirestore();
		    CollectionReference counsellorsCollection = dbFirestore.collection(COUNSELLORS);
		    Query query;

		    if (identifier.matches("^.+@.+\\..+$")) {
		        query = counsellorsCollection.whereEqualTo("email", identifier).limit(1);
		    } else if (identifier.matches("^\\+91\\d{10}$")) {
		        query = counsellorsCollection.whereEqualTo("phoneNumber", identifier).limit(1);
		    } else {
		    	return identifier;
		    }

		    List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

		    if (!documents.isEmpty()) {
		        Counsellor counsellor = documents.get(0).toObject(Counsellor.class);
		        return counsellor.getUserName();
		    }

		    throw new UserNotFoundException("No counsellor found for identifier: " + identifier);
		}
	 
	 public Counsellor getCounsellorById(String counsellorId) throws ExecutionException, InterruptedException {
	        DocumentSnapshot snapshot = firestore.collection("counsellors").document(counsellorId).get().get();
	        return snapshot.exists() ? snapshot.toObject(Counsellor.class) : null;
	    }


	 public void markFollowersNotificationAsSeen(String counsellorId, String userId) {
		 	DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("realtimeSubscribers");
		 	Map<String, Object> updates = new HashMap<>();
			updates.put(userId, true);
			dbRef.child(counsellorId).updateChildrenAsync(updates);
		}
	 
	 public void markSubscribersNotificationAsSeen(String counsellorId, String userId) {
		 DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("realtimeSubscribers");
		 Map<String, Object> updates = new HashMap<>();
		 updates.put(userId, true);
		 dbRef.child(counsellorId).updateChildrenAsync(updates);
	  }
}
