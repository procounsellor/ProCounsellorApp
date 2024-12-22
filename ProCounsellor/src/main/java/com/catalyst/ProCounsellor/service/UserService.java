package com.catalyst.ProCounsellor.service;


import com.catalyst.ProCounsellor.exception.InvalidCredentialsException;
import com.catalyst.ProCounsellor.exception.UserNotFoundException;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Service
public class UserService {
	
	@Autowired
	private FirebaseService firebaseService;
	
	Firestore firestore = FirestoreClient.getFirestore();
	
    private static final String USERS = "users";

    // Signup functionality
    public String signup(User user) throws ExecutionException, InterruptedException {
    	System.out.println(user.getFirstName());
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(USERS).document(user.getUserName());

        // Check if user already exists
        if (documentReference.get().get().exists()) {
            return "User already exists with ID: " + user.getUserName();
        }

        // Save new user
        ApiFuture<WriteResult> collectionsApiFuture = documentReference.set(user);
        return "Signup successful! User ID: " + user.getUserName();
    }

    // Signin functionality
    public String signin(User user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentSnapshot documentSnapshot = dbFirestore.collection(USERS)
                                                        .document(user.getUserName())
                                                        .get()
                                                        .get();

        if (documentSnapshot.exists()) {
            User existingUser = documentSnapshot.toObject(User.class);
            if (existingUser.getPassword().equals(user.getPassword())) {
                return "Signin successful for User ID: " + user.getUserName();
            } else {
                throw new InvalidCredentialsException("Invalid credentials provided.");
            }
        } else {
            throw new UserNotFoundException("User not found for User ID: " + user.getUserName());
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


}
