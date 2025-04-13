package com.catalyst.ProCounsellor.service;
 
import com.catalyst.ProCounsellor.model.CallHistory;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
 
@Service
public class CallService {
 
    @Autowired
    private FirebaseDatabase firebaseDatabase;
    @Autowired
	private SharedService sharedService;
 
    public String startCall(String callerId, String receiverId, String callType) {
        String callId = firebaseDatabase.getReference("calls").push().getKey();
        if (callId == null) {
            throw new RuntimeException("Failed to generate callId");
        }
 
        CallHistory callHistory = new CallHistory();
        callHistory.setCallId(callId);
        callHistory.setCallerId(callerId);
        callHistory.setReceiverId(receiverId);
        callHistory.setStatus("ongoing");
        callHistory.setStartTime(System.currentTimeMillis());
        callHistory.setCallType(callType);
 
        firebaseDatabase.getReference("calls").child(callId).setValueAsync(callHistory);
        firebaseDatabase.getReference("callsByUser").child(receiverId).child("incoming_calls").setValueAsync(callHistory);
        return callId;
    }
 
    public void endCall(String callId) {
        DatabaseReference callRef = firebaseDatabase.getReference("calls").child(callId);
 
        long endTimeMillis = System.currentTimeMillis();
        callRef.child("endTime").setValueAsync(endTimeMillis);
        callRef.child("status").setValueAsync("completed");
 
        callRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    CallHistory callHistory = dataSnapshot.getValue(CallHistory.class);
                    if (callHistory != null) {
                        // Ensure pickedTime exists before calculating duration
                        if (callHistory.getPickedTime() != 0) {
                            long durationMillis = endTimeMillis - callHistory.getPickedTime();
                            callHistory.setDuration(formatDuration(durationMillis));
                            callRef.child("duration").setValueAsync(callHistory.getDuration());
                        } else {
                            callRef.child("duration").setValueAsync("0");
                            callRef.child("status").setValueAsync("Missed Call");
                            callHistory.setStatus("Missed Call");
                            callRef.child("missedCallStatusSeen").setValueAsync(false);                       }
 
                        try {
                            saveCallDetailsToUserAndCounsellor(callHistory, callHistory.getCallerId(), callHistory.getReceiverId());
                        } catch (Exception e) {
                            System.err.println("Error saving call details: " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Call record not found for callId: " + callId);
                }
            }
 
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error fetching call details: " + databaseError.getMessage());
            }
        });
    }
    
    public void declinedCall(String callId) {
        DatabaseReference callRef = firebaseDatabase.getReference("calls").child(callId);
 
        long endTimeMillis = System.currentTimeMillis();
        callRef.child("endTime").setValueAsync(endTimeMillis);
        callRef.child("status").setValueAsync("completed");
 
        callRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    CallHistory callHistory = dataSnapshot.getValue(CallHistory.class);
                    if (callHistory != null) {
                        // Ensure pickedTime exists before calculating duration
                        if (callHistory.getPickedTime() != 0) {
                            long durationMillis = endTimeMillis - callHistory.getPickedTime();
                            callHistory.setDuration(formatDuration(durationMillis));
                            callRef.child("duration").setValueAsync(callHistory.getDuration());
                        } else {
                            callRef.child("duration").setValueAsync("0");
                            callRef.child("status").setValueAsync("Declined");
                            callHistory.setStatus("Declined");
                        }
 
                        try {
                            saveCallDetailsToUserAndCounsellor(callHistory, callHistory.getCallerId(), callHistory.getReceiverId());
                        } catch (Exception e) {
                            System.err.println("Error saving call details: " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Call record not found for callId: " + callId);
                }
            }
 
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error fetching call details: " + databaseError.getMessage());
            }
        });
    }
    
    public void saveCallDetailsToUserAndCounsellor(CallHistory callHistory, String callerId, String receiverId) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();

            // 🔍 Check if caller is a user
            boolean callerIsUser = firestore.collection("users").document(callerId).get().get().exists();

            // 🔍 Check if receiver is a user
            boolean receiverIsUser = firestore.collection("users").document(receiverId).get().get().exists();

            // 🔁 Prepare call participants
            User user = null;
            Counsellor counsellor = null;

            if (callerIsUser) {
                user = sharedService.getUserById(callerId);
            } else {
                counsellor = sharedService.getCounsellorById(callerId);
            }

            if (receiverIsUser) {
                if (user == null) {
                    user = sharedService.getUserById(receiverId);
                }
            } else {
                if (counsellor == null) {
                    counsellor = sharedService.getCounsellorById(receiverId);
                }
            }

            // ✅ Make sure both exist before proceeding
            if (user != null) {
                if (user.getCallHistory() == null) {
                    user.setCallHistory(new ArrayList<>());
                }
                user.getCallHistory().add(callHistory);
                sharedService.updateUser(user);
            }

            if (counsellor != null) {
                if (counsellor.getCallHistory() == null) {
                    counsellor.setCallHistory(new ArrayList<>());
                }
                counsellor.getCallHistory().add(callHistory);
                sharedService.updateCounsellor(counsellor);
            }

            System.out.println("✅ Call details updated for available participants.");
        } catch (Exception e) {
            System.err.println("❌ Error saving call history: " + e.getMessage());
        }
    }

 
 
    /**
     * Converts milliseconds into a formatted string.
     */
    private String formatDuration(long durationMillis) {
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
 
        if (minutes > 0) {
            return minutes + " min " + seconds + " sec";
        } else {
            return seconds + " sec";
        }
    }
 
 
    public void saveCallOffer(String callId, String sdp) {
        firebaseDatabase.getReference("call_signaling").child(callId).child("offer").setValueAsync(sdp);
    }
 
    public void saveCallAnswer(String callId, String sdp) {
        long pickedTimeMillis = System.currentTimeMillis();
 
        DatabaseReference callRef = firebaseDatabase.getReference("calls").child(callId);
        callRef.child("pickedTime").setValueAsync(pickedTimeMillis);
 
        firebaseDatabase.getReference("call_signaling").child(callId).child("answer").setValueAsync(sdp);
    }
 
 
    public void addIceCandidate(String callId, Object candidateObj, String senderId) {
        if (!(candidateObj instanceof Map)) {
            System.out.println("Received invalid ICE candidate format.");
            return;
        }
 
        // Convert candidate object to Map to extract fields
        @SuppressWarnings("unchecked")
        Map<String, Object> candidateMap = (Map<String, Object>) candidateObj;
 
        // Extract actual ICE candidate fields
        Map<String, Object> iceCandidate = new HashMap<>();
        iceCandidate.put("candidate", candidateMap.get("candidate"));
        iceCandidate.put("sdpMid", candidateMap.get("sdpMid"));
        iceCandidate.put("sdpMLineIndex", candidateMap.get("sdpMLineIndex"));
        iceCandidate.put("senderId", senderId);
 
        // Store the properly formatted ICE candidate in Firebase
        firebaseDatabase.getReference("call_signaling")
            .child(callId)
            .child("ice_candidates")
            .push()
            .setValueAsync(iceCandidate);
    }
}