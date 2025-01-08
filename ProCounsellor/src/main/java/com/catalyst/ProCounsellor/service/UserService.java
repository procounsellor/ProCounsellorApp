package com.catalyst.ProCounsellor.service;


import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.AllowedStates;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.Courses;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserService {
	
	@Autowired
	private FirebaseService firebaseService;
	
	Firestore firestore = FirestoreClient.getFirestore();
	
    private static final String USERS = "users";
    
    // New Signup functionality
    public String newSignup(String phoneNumber) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        // Check for uniqueness of phoneNumber
        CollectionReference usersCollection = dbFirestore.collection(USERS);
        Query phoneQuery = usersCollection.whereEqualTo("phoneNumber", phoneNumber);

        if (!phoneQuery.get().get().isEmpty()) {
            return "Phone number already exists: " + phoneNumber;
        }

        // Create new user object
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setUserName(phoneNumber); // Use phone number as userName
        user.setRole("user");

        // Save new user
        DocumentReference userDocRef = dbFirestore.collection(USERS).document(user.getUserName());
        ApiFuture<WriteResult> collectionsApiFuture = userDocRef.set(user);

        return "Signup successful! User ID: " + user.getUserName();
    }
    
    public boolean isPhoneNumberExists(String phoneNumber) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference usersCollection = dbFirestore.collection(USERS);
        Query phoneQuery = usersCollection.whereEqualTo("phoneNumber", phoneNumber);

        return !phoneQuery.get().get().isEmpty();
    }
    

    // Signup functionality
    public String signup(User user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        // Validate mandatory fields
        if (user.getUserName() == null || user.getUserName().isEmpty()) {
            return "UserName is mandatory and cannot be null or empty.";
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            return "Phone number is mandatory and cannot be null or empty.";
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return "Email is mandatory and cannot be null or empty.";
        }
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            return "First name is mandatory and cannot be null or empty.";
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            return "Last name is mandatory and cannot be null or empty.";
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "Password is mandatory and cannot be null or empty.";
        }
        if (user.getUserInterestedStateOfCounsellors() == null || user.getUserInterestedStateOfCounsellors().isEmpty()) {
            return "User interested state of counsellors cannot be null or empty.";
        }
        if (user.getInterestedCourse() == null || user.getInterestedCourse().toString().isEmpty()) {
            return "Interested course cannot be null or empty.";
        }

        // Check for uniqueness of userName
        DocumentReference userDocRef = dbFirestore.collection(USERS).document(user.getUserName());
        if (userDocRef.get().get().exists()) {
            return "User already exists with userName: " + user.getUserName();
        }

        // Check for uniqueness of phoneNumber and email
        CollectionReference usersCollection = dbFirestore.collection(USERS);
        Query phoneQuery = usersCollection.whereEqualTo("phoneNumber", user.getPhoneNumber());
        Query emailQuery = usersCollection.whereEqualTo("email", user.getEmail());

        if (!phoneQuery.get().get().isEmpty()) {
            return "Phone number already exists: " + user.getPhoneNumber();
        }
        if (!emailQuery.get().get().isEmpty()) {
            return "Email already exists: " + user.getEmail();
        }

        // Save new user
        ApiFuture<WriteResult> collectionsApiFuture = userDocRef.set(user);
        return "Signup successful! User ID: " + user.getUserName();
    }

    // Signin functionality
    public String signin(String identifier, String password) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference userCollection = dbFirestore.collection(USERS);

        // Determine the identifier type and query the Firestore
        Query query;
        if (identifier.contains("@")) {
            // If it contains '@', treat it as email
            query = userCollection.whereEqualTo("email", identifier);
        } else if (identifier.matches("\\d+")) {
            // If it's numeric, treat it as phone number
            query = userCollection.whereEqualTo("phoneNumber", identifier);
        } else {
            // Otherwise, treat it as userName (DocumentId)
            DocumentReference docRef = userCollection.document(identifier);

            // Check if the document exists
            DocumentSnapshot documentSnapshot = docRef.get().get();
            if (documentSnapshot.exists()) {
                User existingUser = documentSnapshot.toObject(User.class);

                // Validate the password
                if (existingUser.getPassword().equals(password)) {
                    return "Signin successful for User ID: " + identifier;
                } else {
                    throw new InvalidCredentialsException("Invalid credentials provided.");
                }
            } else {
                throw new UserNotFoundException("User not found for userName: " + identifier);
            }
        }

        // Execute the query for email or phoneNumber
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        if (!documents.isEmpty()) {
            // Fetch the first matching document
            QueryDocumentSnapshot document = documents.get(0);
            User existingUser = document.toObject(User.class);

            // Validate the password
            if (existingUser.getPassword().equals(password)) {
                String userName = document.getId(); // Get the DocumentId as userName
                return "Signin successful for User ID: " + userName;
            } else {
                throw new InvalidCredentialsException("Invalid credentials provided.");
            }
        } else {
            throw new UserNotFoundException("Counsellor not found for the provided credentials.");
        }
    }


    
    public boolean subscribeToCounsellor(String userId, String counsellorId) {
        try {
            // Retrieve the user and counsellor from Firebase
            User user = firebaseService.getUserById(userId);
            Counsellor counsellor = firebaseService.getCounsellorById(counsellorId);

            if (user == null || counsellor == null) {
                return false; // User or Counsellor not found
            }

            // Check and initialize the user's subscribedCounsellorIds list if it's null
            if (user.getSubscribedCounsellorIds() == null) {
                user.setSubscribedCounsellorIds(new ArrayList<>());
            }

            // Add the counsellor's ID to the user's subscribedCounsellorIds list
            if (!user.getSubscribedCounsellorIds().contains(counsellorId)) {
                user.getSubscribedCounsellorIds().add(counsellorId);
            }

            // Check and initialize the counsellor's clientIds list if it's null
            if (counsellor.getClientIds() == null) {
                counsellor.setClientIds(new ArrayList<>());
            }

            // Add the user's ID to the counsellor's clientIds list
            if (!counsellor.getClientIds().contains(userId)) {
                counsellor.getClientIds().add(userId);
            }

            // Update both entities in Firebase
            firebaseService.updateUser(user);
            firebaseService.updateCounsellor(counsellor);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Counsellor> getSubscribedCounsellors(String userId) {
        try {
            User user = firebaseService.getUserById(userId);
            if (user != null && user.getSubscribedCounsellorIds() != null) {
                List<Counsellor> subscribedCounsellors = new ArrayList<>();
                for (String counsellorId : user.getSubscribedCounsellorIds()) {
                    Counsellor counsellor = firebaseService.getCounsellorById(counsellorId);
                    if (counsellor != null) {
                        subscribedCounsellors.add(counsellor);
                    }
                }
                return subscribedCounsellors;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if user not found or error occurs
    }

	public List<Counsellor> getFollowedCounsellors(String userId) {
		try {
            User user = firebaseService.getUserById(userId);
            if (user != null && user.getFollowedCounsellorsIds() != null) {
                List<Counsellor> followedCounsellors = new ArrayList<>();
                for (String counsellorId : user.getFollowedCounsellorsIds()) {
                    Counsellor counsellor = firebaseService.getCounsellorById(counsellorId);
                    if (counsellor != null) {
                    	followedCounsellors.add(counsellor);
                    }
                }
                return followedCounsellors;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if user not found or error occurs
	}

	public boolean followCounsellor(String userId, String counsellorId) {
		try {
            // Retrieve the user and counsellor from Firebase
            User user = firebaseService.getUserById(userId);
            Counsellor counsellor = firebaseService.getCounsellorById(counsellorId);

            if (user == null || counsellor == null) {
                return false; // User or Counsellor not found
            }

            // Check and initialize the user's followedCounsellorIds list if it's null
            if (user.getFollowedCounsellorsIds() == null) {
                user.setFollowedCounsellorsIds(new ArrayList<>());
            }

            // Add the counsellor's ID to the user's followedCounsellorIds list
            if (!user.getFollowedCounsellorsIds().contains(counsellorId)) {
                user.getFollowedCounsellorsIds().add(counsellorId);
            }

            // Check and initialize the counsellor's clientIds list if it's null
            if (counsellor.getFollowerIds() == null) {
                counsellor.setFollowerIds(new ArrayList<>());
            }

            // Add the user's ID to the counsellor's clientIds list
            if (!counsellor.getFollowerIds().contains(userId)) {
                counsellor.getFollowerIds().add(userId);
            }

            // Update both entities in Firebase
            firebaseService.updateUser(user);
            firebaseService.updateCounsellor(counsellor);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	
	public boolean isSubscribedToCounsellor(String userId, String counsellorId) {
	    try {
	        User user = firebaseService.getUserById(userId); // Retrieve the user
	        if (user != null && user.getSubscribedCounsellorIds() != null) {
	            return user.getSubscribedCounsellorIds().contains(counsellorId); // Check if counsellorId exists in subscribed list
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return false; // Return false if user or counsellor not found
	}

	public boolean hasFollowedCounsellor(String userId, String counsellorId) {
		try {
	        User user = firebaseService.getUserById(userId); // Retrieve the user
	        if (user != null && user.getFollowedCounsellorsIds() != null) {
	            return user.getFollowedCounsellorsIds().contains(counsellorId); // Check if counsellorId exists in followed list
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return false; // Return false if user or counsellor not found
	}
	
	public boolean unsubscribeCounsellor(String userId, String counsellorId) {
	    try {
	        // Fetch user and counsellor
	        User user = firebaseService.getUserById(userId);
	        Counsellor counsellor = firebaseService.getCounsellorById(counsellorId);

	        if (user == null || counsellor == null) {
	            return false; // Return false if user or counsellor doesn't exist
	        }

	        // Remove counsellor from user's subscribedCounsellorIds
	        if (user.getSubscribedCounsellorIds() != null) {
	            user.getSubscribedCounsellorIds().remove(counsellorId);
	        }

	        // Remove user from counsellor's clientIds
	        if (counsellor.getClientIds() != null) {
	            counsellor.getClientIds().remove(userId);
	        }

	        // Update both entities in Firebase
	        firebaseService.updateUser(user);
	        firebaseService.updateCounsellor(counsellor);

	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public boolean unfollowCounsellor(String userId, String counsellorId) {
	    try {
	        // Fetch user and counsellor
	        User user = firebaseService.getUserById(userId);
	        Counsellor counsellor = firebaseService.getCounsellorById(counsellorId);

	        if (user == null || counsellor == null) {
	            return false; // Return false if user or counsellor doesn't exist
	        }

	        // Remove counsellor from user's followedCounsellorIds
	        if (user.getFollowedCounsellorsIds() != null) {
	            user.getFollowedCounsellorsIds().remove(counsellorId);
	        }

	        // Remove user from counsellor's clientIds
	        if (counsellor.getFollowerIds() != null) {
	            counsellor.getFollowerIds().remove(userId);
	        }

	        // Update both entities in Firebase
	        firebaseService.updateUser(user);
	        firebaseService.updateCounsellor(counsellor);

	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public void updateUserPhotoUrl(String userId, String photoUrl) {
		Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection(USERS).document(userId).update("photo", photoUrl);
	}
	
	 public User updateUserFields(String userId, Map<String, Object> updates) throws ExecutionException, InterruptedException {
	        DocumentReference docRef = firestore.collection(USERS).document(userId);

	        // Perform the update
	        ApiFuture<WriteResult> writeResult = docRef.update(updates);

	        // Fetch the updated user
	        DocumentSnapshot document = docRef.get().get();
	        if (document.exists()) {
	            return document.toObject(User.class);
	        } else {
	            throw new RuntimeException("User not found");
	        }
	    }

	 
	 /**
	 * Update the user state in Firebase Realtime Database.
	 *
	 * @param userName the username of the user
	 * @param state    the presence state to be updated
	 * @return true if update is successful
	 */
	 public boolean updateUserState(String userName, String state) {
	        try {
	            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("userStates");

	            // Check if the user exists in Firestore
	            ApiFuture<DocumentSnapshot> future = firestore.collection(USERS).document(userName).get();
	            DocumentSnapshot document = future.get();

	            if (!document.exists()) {
	                System.err.println("User not found in Firestore: " + userName);
	                return false; // User does not exist, deny the update
	            }

	            // Proceed to update the Realtime Database
	            UserState userState = new UserState();
	            userState.setUserName(userName);
	            userState.setState(state);

	            databaseReference.child(userName).setValueAsync(userState);
	            return true;

	        } catch (Exception e) {
	            // Log error
	            System.err.println("Error updating user state: " + e.getMessage());
	            return false;
	        }
	    }
	 
	 
	 /**
	  * Check if the user is online by fetching their state from Firebase Realtime Database.
	  *
	  * @param userName the username of the user
	  * @return true if the user's state is "online", false otherwise
	  */
	 public boolean isUserOnline(String userName) throws InterruptedException {
		    // Create a CountDownLatch initialized to 1 (indicating that we need to wait for 1 event)
		    CountDownLatch latch = new CountDownLatch(1);

		    // Variable to hold the result
		    final boolean[] isOnline = {false};

		    // Get the reference to the user's state in the Realtime Database
		    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("userStates").child(userName);

		    // Add listener to fetch the user's state
		    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
		        @Override
		        public void onDataChange(DataSnapshot dataSnapshot) {
		            // Check if the user exists and retrieve their state
		            if (dataSnapshot.exists()) {
		                UserState userState = dataSnapshot.getValue(UserState.class);
		                if (userState != null) {
		                    // Check if the state is 'online'
		                    isOnline[0] = "online".equalsIgnoreCase(userState.getState());
		                }
		            } else {
		                System.err.println("User state not found in Realtime Database for: " + userName);
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
	 
	 public List<Counsellor> getCounsellorsByCourse(Courses course) throws ExecutionException, InterruptedException {
	        ApiFuture<QuerySnapshot> future = firestore
	                .collection("counsellors")
	                .whereArrayContains("expertise", course) 
	                .get();

	        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
	        return documents.stream()
	                        .map(doc -> doc.toObject(Counsellor.class))
	                        .collect(Collectors.toList());
	    }
	 
	 	/**
	     * Fetch counsellors whose 'expertise' (list of Courses) contains the user’s interestedCourse
	     * AND whose 'stateOfCounsellor' equals the specified state.
	     */
	    public List<Counsellor> getCounsellorsByCourseAndState(Courses course, AllowedStates state)
	            throws ExecutionException, InterruptedException {
	        
	        ApiFuture<QuerySnapshot> future = firestore
	                .collection("counsellors")
	                .whereArrayContains("expertise", course)
	                .whereEqualTo("stateOfCounsellor", state) 
	                .get();

	        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
	        return documents.stream()
	                        .map(doc -> doc.toObject(Counsellor.class))
	                        .collect(Collectors.toList());
	    }
	    
	    
	    public String getUserNameFromEmail(String email) throws ExecutionException, InterruptedException {
	        Firestore dbFirestore = FirestoreClient.getFirestore();
	        CollectionReference usersCollection = dbFirestore.collection(USERS);

	        // Query to find user by email
	        Query query = usersCollection.whereEqualTo("email", email);
	        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

	        if (!documents.isEmpty()) {
	            User user = documents.get(0).toObject(User.class);
	            return user.getUserName();
	        } else {
	            throw new UserNotFoundException("No user found with email: " + email);
	        }
	    }

	    public String getUserNameFromPhoneNumber(String phoneNumber) throws ExecutionException, InterruptedException {
	        Firestore dbFirestore = FirestoreClient.getFirestore();
	        CollectionReference usersCollection = dbFirestore.collection(USERS);

	        // Query to find user by phone number
	        Query query = usersCollection.whereEqualTo("phoneNumber", phoneNumber);
	        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

	        if (!documents.isEmpty()) {
	            User user = documents.get(0).toObject(User.class);
	            return user.getUserName();
	        } else {
	            throw new UserNotFoundException("No user found with phone number: " + phoneNumber);
	        }
	    }
	    
	    public User getUserById(String userId) throws ExecutionException, InterruptedException {
	        DocumentSnapshot snapshot = firestore.collection(USERS).document(userId).get().get();
	        return snapshot.exists() ? snapshot.toObject(User.class) : null;
	    }

}
