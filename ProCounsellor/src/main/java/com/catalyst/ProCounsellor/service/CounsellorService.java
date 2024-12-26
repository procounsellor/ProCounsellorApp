package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.CounsellorState;
import com.catalyst.ProCounsellor.model.StateType;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.model.UserState;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@Service
public class CounsellorService {
	
	@Autowired
	private FirebaseService firebaseService;

    private static final String COUNSELLORS = "counsellors";
    
    Firestore firestore = FirestoreClient.getFirestore();

    // Signup functionality
    public String signup(Counsellor counsellor) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(COUNSELLORS).document(counsellor.getUserName());

        // Check if user already exists
        if (documentReference.get().get().exists()) {
            return "User already exists with ID: " + counsellor.getUserName();
        }

        // Save new user
        ApiFuture<WriteResult> collectionsApiFuture = documentReference.set(counsellor);
        return "Signup successful! User ID: " + counsellor.getUserName();
    }

    // Signin functionality
    public String signin(Counsellor counsellor) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentSnapshot documentSnapshot = dbFirestore.collection(COUNSELLORS)
                                                        .document(counsellor.getUserName())
                                                        .get()
                                                        .get();

        if (documentSnapshot.exists()) {
            Counsellor existingCounsellor = documentSnapshot.toObject(Counsellor.class);
            if (existingCounsellor.getPassword().equals(counsellor.getPassword())) {
                return "Signin successful for User ID: " + counsellor.getUserName();
            } else {
                throw new InvalidCredentialsException("Invalid credentials provided.");
            }
        } else {
            throw new UserNotFoundException("Counsellor not found for ID: " + counsellor.getUserName());
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
            Counsellor counsellor = firebaseService.getCounsellorById(counsellorName);
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
            Counsellor counsellor = firebaseService.getCounsellorById(counsellorId);
            if (counsellor != null && counsellor.getClientIds() != null) {
                List<User> subscribedClients = new ArrayList<>();
                for (String userId : counsellor.getClientIds()) {
                    User user = firebaseService.getUserById(userId);
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
            Counsellor counsellor = firebaseService.getCounsellorById(counsellorId);
            if (counsellor != null && counsellor.getFollowerIds() != null) {
                List<User> followers = new ArrayList<>();
                for (String userId : counsellor.getFollowerIds()) {
                    User user = firebaseService.getUserById(userId);
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
	        Counsellor counsellor = firebaseService.getCounsellorById(counsellorId); // Retrieve the counsellor
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
		        Counsellor counsellor = firebaseService.getCounsellorById(counsellorId); // Retrieve the counsellor
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

}
