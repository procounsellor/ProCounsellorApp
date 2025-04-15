package com.catalyst.ProCounsellor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.model.CallHistory;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.agora.media.RtcTokenBuilder2;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AgoraTokenService {

    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference agoraCallSignalling;
    @Autowired
	private SharedService sharedService;

    private String appId = System.getenv("AGORA_APP_ID");
    private String appCertificate = System.getenv("AGORA_APP_CERTIFICATE");
    private String tokenExpiry2 = System.getenv("AGORA_TOKEN_EXPIRY");
    private int tokenExpiry = Integer.parseInt(tokenExpiry2);

    public AgoraTokenService(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
        this.agoraCallSignalling = firebaseDatabase.getReference("agora_call_signaling");
    }

    public String generateToken(String channelName, int uid) {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        int expireTimestamp = (int) ((new Date().getTime() / 1000) + tokenExpiry);

        return tokenBuilder.buildTokenWithUid(
                appId,
                appCertificate,
                channelName,
                uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                expireTimestamp,
                expireTimestamp
        );
    }
    
    public void startCall(String channelId, String callerId, String receiverId, String callType) {
        String callId = channelId;
 
        CallHistory callHistory = new CallHistory();
        callHistory.setCallId(callId);
        callHistory.setCallerId(callerId);
        callHistory.setReceiverId(receiverId);
        callHistory.setStatus("ongoing");
        callHistory.setStartTime(System.currentTimeMillis());
        callHistory.setCallType(callType);
 
        firebaseDatabase.getReference("calls").child(callId).setValueAsync(callHistory);
    }
    
    public void pickedCall(String callId) {
    	long pickedTimeMillis = System.currentTimeMillis();
    	
    	DatabaseReference callRef = firebaseDatabase.getReference("calls").child(callId);
        callRef.child("pickedTime").setValueAsync(pickedTimeMillis);
    }

    public void sendCallNotification(String receiverFCMToken, String senderName, String channelId, String receiverId, String callType) {
        // Step 1: Save signaling data to Firebase Realtime DB
        agoraCallSignalling.child(receiverId).setValueAsync(new CallSession(senderName, channelId, callType));
        startCall(channelId, senderName, receiverId, callType);

        // Step 2: Build notification object
        Notification notification = Notification.builder()
            .setTitle("Incoming Call")
            .setBody(senderName + " is calling you...")
            .build();

        // Step 3: Build APNs config (iOS)
        ApnsConfig apnsConfig = ApnsConfig.builder()
            .putHeader("apns-priority", "10")
            .setAps(
                Aps.builder()
                    .setSound("default")
                    .setContentAvailable(true)
                    .build()
            )
            .build();

        // ✅ Step 4: Build Android config
        AndroidConfig androidConfig = AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .setNotification(AndroidNotification.builder()
                .setSound("default")
                .setChannelId("high_importance_channel") // ✅ This must match client channel ID
                .build())
            .build();

        // Step 5: Build and send final message
        Message message = Message.builder()
            .setToken(receiverFCMToken)
            .putData("type", "incoming_call")
            .putData("channelId", channelId)
            .putData("callerName", senderName)
            .putData("receiverName", receiverId)
            .putData("callType", callType)
            .setApnsConfig(apnsConfig)
            .setAndroidConfig(androidConfig) // ✅ Added for Android
            .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Call Notification Sent!");
        } catch (Exception e) {
            System.err.println("❌ Error sending notification: " + e.getMessage());
        }
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
                    	//TODO using start time right now which needs to be changed to picked time later on as picked time implementation has not been done yet.
                        if (callHistory.getStartTime() != 0) {//getPICKED
                            long durationMillis = endTimeMillis - callHistory.getStartTime();//getPICKED
                            callHistory.setDuration(formatDuration(durationMillis));
                            callRef.child("duration").setValueAsync(callHistory.getDuration());
                        } else {
                            callRef.child("duration").setValueAsync("0");
                            callRef.child("status").setValueAsync("Missed Call");
                            callHistory.setStatus("Missed Call");
                            callRef.child("missedCallStatusSeen").setValueAsync(false);                       }
 
                        try {
                        	saveCallDetailsToCallerAndReceiver(callHistory, callHistory.getCallerId(), callHistory.getReceiverId());
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
                        if (callHistory.getStartTime() != 0) {//getPICKED
                            long durationMillis = endTimeMillis - callHistory.getStartTime();//getPICKED
                            callHistory.setDuration(formatDuration(durationMillis));
                            callRef.child("duration").setValueAsync(callHistory.getDuration());
                        } else {
                            callRef.child("duration").setValueAsync("0");
                            callRef.child("status").setValueAsync("Declined");
                            callHistory.setStatus("Declined");
                        }
 
                        try {
                        	saveCallDetailsToCallerAndReceiver(callHistory, callHistory.getCallerId(), callHistory.getReceiverId());
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
 
    
    public void saveCallDetailsToCallerAndReceiver(CallHistory callHistory, String callerId, String receiverId) {
        try {
            DocumentSnapshot docRef = FirestoreClient.getFirestore()
                .collection("users").document(callerId).get().get();
 
            boolean isCallerUser = docRef.exists(); // If
 
            User user = isCallerUser ? sharedService.getUserById(callerId) : sharedService.getUserById(receiverId);
            Counsellor counsellor = isCallerUser ? sharedService.getCounsellorById(receiverId) : sharedService.getCounsellorById(callerId);
 
            if (user.getCallHistory() == null) {
                user.setCallHistory(new ArrayList<>());
            }
 
            if (counsellor.getCallHistory() == null) {
                counsellor.setCallHistory(new ArrayList<>());
            }
 
            user.getCallHistory().add(callHistory);
            counsellor.getCallHistory().add(callHistory);
 
            sharedService.updateUser(user);
            sharedService.updateCounsellor(counsellor);
            System.out.println("Call details updated for User & Counsellor.");
        } catch (Exception e) {
            System.err.println("Error saving call history: " + e.getMessage());
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

    static class CallSession {
        public String callerName;
        public String channelId;
        public String callType;

        public CallSession() {}
        public CallSession(String callerName, String channelId, String callType) {
            this.callerName = callerName;
            this.channelId = channelId;
            this.callType = callType;
        }
    }
}
