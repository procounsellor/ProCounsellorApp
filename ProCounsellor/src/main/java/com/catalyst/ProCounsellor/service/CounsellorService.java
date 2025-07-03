package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.config.JwtKeyProvider;
import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.CounsellorState;
import com.catalyst.ProCounsellor.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.jsonwebtoken.Jwts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@Service
public class CounsellorService {
	
	@Autowired
	private SharedService sharedService;
	
	@Autowired
    private OTPService otpService;
	
	@Autowired 
    private MailOtpService mailOtpService;
	
    private static final String COUNSELLORS = "counsellors";
    
    Firestore firestore = FirestoreClient.getFirestore();
    
    private static final Logger logger = LoggerFactory.getLogger(CounsellorService.class);
    
   
    public boolean verifyCounsellorPhoneNumber(String phoneNumber, String otp)
            throws FirebaseAuthException, ExecutionException, InterruptedException {

        logger.info("Starting verification and signup/login flow for phone number: {}", phoneNumber);

        if (!otpService.verifyOtp(phoneNumber, otp)) {
            logger.warn("OTP verification failed for phone number: {}", phoneNumber);
            throw new RuntimeException("Invalid or expired OTP. Please try again.");
        }

        logger.info("OTP verified successfully for phone number: {}", phoneNumber);
		return true;
    }
    
    public String generateMailOtp(String email) {
		return mailOtpService.generateOtp(email);
	}


	public boolean verifyMailOtp(String email, String otp) {
		return mailOtpService.verifyOtp(email, otp);
	}

    // Signup functionality
	public String counsellorSignup(Counsellor counsellor) throws Exception {
		if(!counsellor.isEmailOtpVerified() || !counsellor.isPhoneOtpVerified()) {
			throw new Exception("Email or phone number has not been verified yet");
		}
        Firestore dbFirestore = FirestoreClient.getFirestore();

        counsellor.setUserName(counsellor.getPhoneNumber().replaceFirst("^\\+\\d{2}", ""));
        counsellor.setRole("counsellor");

        logger.info("Attempting signup for counsellor with email: {}", counsellor.getEmail());

        // Validation
        if (counsellor.getFirstName() == null || counsellor.getFirstName().isEmpty()) {
            logger.warn("Validation failed: First name is missing");
            return "First name is mandatory and cannot be null or empty.";
        }
        if (counsellor.getLastName() == null || counsellor.getLastName().isEmpty()) {
            logger.warn("Validation failed: Last name is missing");
            return "Last name is mandatory and cannot be null or empty.";
        }
        if (counsellor.getPhoneNumber() == null || counsellor.getPhoneNumber().isEmpty()) {
            logger.warn("Validation failed: Phone number is missing");
            return "Phone number is mandatory and cannot be null or empty.";
        }
        if (counsellor.getEmail() == null || counsellor.getEmail().isEmpty()) {
            logger.warn("Validation failed: Email is missing");
            return "Email is mandatory and cannot be null or empty.";
        }
        if (counsellor.getPassword() == null || counsellor.getPassword().isEmpty()) {
            logger.warn("Validation failed: Password is missing");
            return "Password is mandatory and cannot be null or empty.";
        }
        if (counsellor.getRatePerYear() == null || counsellor.getRatePerYear() <= 0) {
            logger.warn("Validation failed: Invalid rate per year");
            return "Rate per year must be greater than 0.";
        }
        if (counsellor.getRatePerMinute() == null || counsellor.getRatePerMinute() <= 0) {
            logger.warn("Validation failed: Invalid rate per minute");
            return "Rate per minute must be greater than 0.";
        }
        if (counsellor.getStateOfCounsellor() == null || counsellor.getStateOfCounsellor().toString().isEmpty()) {
            logger.warn("Validation failed: State is missing");
            return "State of counsellor cannot be null or empty.";
        }
        if (counsellor.getExpertise() == null || counsellor.getExpertise().isEmpty()) {
            logger.warn("Validation failed: Expertise is missing");
            return "Expertise cannot be null or empty.";
        }
        if (counsellor.getLanguagesKnow() == null || counsellor.getLanguagesKnow().toString().isEmpty()) {
            logger.warn("Validation failed: Languages known is missing");
            return "Language known of counsellor cannot be null or empty.";
        }
        if (counsellor.getWorkingDays() == null || counsellor.getWorkingDays().isEmpty()) {
            logger.warn("Validation failed: Working days are missing");
            return "Working days cannot be null or empty.";
        }
        if (counsellor.getOfficeStartTime() == null || counsellor.getOfficeStartTime().isEmpty()) {
            logger.warn("Validation failed: Office start time is missing");
            return "Office Start time cannot be null or empty.";
        }
        if (counsellor.getOfficeEndTime() == null || counsellor.getOfficeEndTime().toString().isEmpty()) {
            logger.warn("Validation failed: Office end time is missing");
            return "Office end time known of counsellor cannot be null or empty.";
        }
        if (counsellor.getFullOfficeAddress() == null) {
            logger.warn("Validation failed: Office address is missing");
            return "Full office address cannot be null or empty.";
        }

        // Check for uniqueness
        DocumentReference userDocRef = dbFirestore.collection(COUNSELLORS).document(counsellor.getUserName());
        if (userDocRef.get().get().exists()) {
            logger.warn("Signup failed: Username already exists - {}", counsellor.getUserName());
            return "User already exists with userName: " + counsellor.getUserName();
        }

        CollectionReference counsellorsCollection = dbFirestore.collection(COUNSELLORS);
        Query phoneQuery = counsellorsCollection.whereEqualTo("phoneNumber", counsellor.getPhoneNumber());
        Query emailQuery = counsellorsCollection.whereEqualTo("email", counsellor.getEmail());

        if (!phoneQuery.get().get().isEmpty()) {
            logger.warn("Signup failed: Phone number already exists - {}", counsellor.getPhoneNumber());
            return "Phone number already exists: " + counsellor.getPhoneNumber();
        }

        if (!emailQuery.get().get().isEmpty()) {
            logger.warn("Signup failed: Email already exists - {}", counsellor.getEmail());
            return "Email already exists: " + counsellor.getEmail();
        }

        // Save new counsellor
        ApiFuture<WriteResult> collectionsApiFuture = userDocRef.set(counsellor);
        logger.info("Counsellor signup successful for username: {}, time: {}", counsellor.getUserName(), collectionsApiFuture.get().getUpdateTime());

        return "Signup successful! User ID: " + counsellor.getUserName();
    }
	
	public Map<String, Object> signinAndGenerateTokens(String identifier, String password)
	        throws ExecutionException, InterruptedException, FirebaseAuthException {

	    logger.info("Initiating signin and token generation for identifier: {}", identifier);

	    HttpStatus status = counsellorSignin(identifier, password);
	    String userId = getCounsellorId(identifier);

	    if (status != HttpStatus.OK) {
	        logger.warn("Signin failed for identifier: {}", identifier);
	        throw new InvalidCredentialsException("Invalid login attempt");
	    }

	    logger.info("Signin successful for userId: {}. Generating tokens...", userId);

	    String firebaseCustomToken = FirebaseAuth.getInstance().createCustomToken(userId);
	    logger.debug("Firebase custom token generated for userId: {}", userId);

	    String jwtToken = Jwts.builder()
	            .setSubject(userId)
	            .setIssuedAt(new Date())
	            .setExpiration(new Date(System.currentTimeMillis() + 86400000L * 365)) // 1 year
	            .signWith(JwtKeyProvider.getSigningKey())
	            .compact();

	    logger.debug("JWT token generated for userId: {}", userId);

	    return Map.of(
	            "firebaseCustomToken", firebaseCustomToken,
	            "jwtToken", jwtToken,
	            "userId", userId
	    );
	}

    // Signin functionality
	public HttpStatus counsellorSignin(String identifier, String password) throws ExecutionException, InterruptedException {
	    logger.info("Attempting counsellor signin for identifier: {}", identifier);

	    Firestore dbFirestore = FirestoreClient.getFirestore();
	    CollectionReference counsellorsCollection = dbFirestore.collection(COUNSELLORS);

	    Query query;

	    if (identifier.contains("@")) {
	        logger.debug("Identifier treated as email: {}", identifier);
	        query = counsellorsCollection.whereEqualTo("email", identifier);
	    } else if (identifier.contains("+91")) {
	        logger.debug("Identifier treated as phone number: {}", identifier);
	        query = counsellorsCollection.whereEqualTo("phoneNumber", identifier);
	    } else {
	        logger.debug("Identifier treated as userName: {}", identifier);
	        DocumentReference docRef = counsellorsCollection.document(identifier);
	        DocumentSnapshot documentSnapshot = docRef.get().get();

	        if (documentSnapshot.exists()) {
	            Counsellor existingCounsellor = documentSnapshot.toObject(Counsellor.class);
	            if (existingCounsellor.getPassword().equals(password)) {
	                logger.info("Signin successful for userName: {}", identifier);
	                return HttpStatus.OK;
	            } else {
	                logger.warn("Invalid password for userName: {}", identifier);
	                throw new InvalidCredentialsException("Invalid credentials provided.");
	            }
	        } else {
	            logger.warn("No counsellor found for userName: {}", identifier);
	            throw new UserNotFoundException("Counsellor not found for userName: " + identifier);
	        }
	    }

	    List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

	    if (!documents.isEmpty()) {
	        Counsellor existingCounsellor = documents.get(0).toObject(Counsellor.class);
	        if (existingCounsellor.getPassword().equals(password)) {
	            logger.info("Signin successful for identifier: {}", identifier);
	            return HttpStatus.OK;
	        } else {
	            logger.warn("Invalid password for identifier: {}", identifier);
	            throw new InvalidCredentialsException("Invalid credentials provided.");
	        }
	    } else {
	        logger.warn("No counsellor found for identifier: {}", identifier);
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
	 
	 public Counsellor getCounsellorFromPhoneNumber(String phoneNumber) throws ExecutionException, InterruptedException {
	        Firestore dbFirestore = FirestoreClient.getFirestore();
	        CollectionReference counsellorsCollection = dbFirestore.collection(COUNSELLORS);

	        // Query to find user by phone number
	        Query query = counsellorsCollection.whereEqualTo("phoneNumber", phoneNumber);
	        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

	        if (!documents.isEmpty()) {
	        	Counsellor counsellor = documents.get(0).toObject(Counsellor.class);
	            return counsellor;
	        } else {
	            throw new UserNotFoundException("No counsellor found with phone number: " + phoneNumber);
	        }
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
}
