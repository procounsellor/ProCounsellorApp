package com.catalyst.ProCounsellor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catalyst.ProCounsellor.model.CallHistory;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;


import com.google.cloud.storage.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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
        callRef.child("status").setValueAsync("accepted");
    }

    public void sendCallNotification(String receiverFCMToken, String senderName, String channelId, String receiverId, String callType)
            throws ExecutionException, InterruptedException {
        
        User user = sharedService.getUserById(receiverId);
        Counsellor counsellor = null;

        String platform;
        String voipToken;

        if (user == null) {
        	System.out.println("not user");
            counsellor = sharedService.getCounsellorById(receiverId);
            platform = counsellor != null ? counsellor.getPlatform() : "";
            voipToken = counsellor != null ? counsellor.getVoipToken() : "";
        } else {
        	System.out.println("user");
            platform = user.getPlatform();
            voipToken = user.getVoipToken();
        }

        if ("ios".equalsIgnoreCase(platform) && voipToken != null && !voipToken.isEmpty()) {
            sendVoIPCallNotification(voipToken, senderName, channelId, receiverId, callType, false);
        } else {
            sendFcmNotification(receiverFCMToken, senderName, channelId, receiverId, callType);
        }
    }
    
    public void sendVoIPCallNotification(String voipToken, String callerName, String channelId, String receiverId, String callType, boolean isCancel) {
        try {
            // Step 1: Save signaling info only if it's not a cancel request
            if (!isCancel) {
                agoraCallSignalling.child(receiverId)
                    .setValueAsync(new CallSession(callerName, channelId, callType));
                startCall(channelId, callerName, receiverId, callType);
            }

            // Step 2: Load .p12 file from GCS
            String bucketName = "voipcert";
            String objectName = "voip_cert.p12";
            Storage storage = StorageOptions.getDefaultInstance().getService();
            Blob blob = storage.get(bucketName, objectName);

            if (blob == null) {
                System.err.println("❌ Could not find voip_cert.p12 in GCS.");
                return;
            }

            byte[] p12Bytes = blob.getContent();
            InputStream p12Stream = new ByteArrayInputStream(p12Bytes);
            String p12Password = "ProCounsellor@2024";

            // Step 3: Initialize APNs Client
            ApnsClient apnsClient = new ApnsClientBuilder()
                    .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                    .setClientCredentials(p12Stream, p12Password)
                    .build();

            // Step 4: Construct JSON payload
            Map<String, Object> payloadMap = new HashMap<>();

            if (isCancel) {
                // Cancel Push Payload
                payloadMap.put("type", "cancel_call");
                payloadMap.put("channelId", channelId);
            } else {
                // Normal Incoming Call Payload
                payloadMap.put("id", UUID.randomUUID().toString());
                payloadMap.put("nameCaller", callerName);
                payloadMap.put("handle", callerName); // ✅ REQUIRED
                payloadMap.put("type", callType.equalsIgnoreCase("video") ? 1 : 0);
                payloadMap.put("duration", 60000);
                payloadMap.put("textAccept", "Answer");
                payloadMap.put("textDecline", "Decline");
                payloadMap.put("textMissedCall", "Missed call");
                payloadMap.put("textCallback", "Call back");

                Map<String, Object> ios = new HashMap<>();
                ios.put("iconName", "CallKitIcon");
                ios.put("handleType", "generic");
                ios.put("supportsVideo", true);
                ios.put("maximumCallGroups", 2);
                ios.put("maximumCallsPerCallGroup", 1);
                payloadMap.put("ios", ios);

                Map<String, String> extra = new HashMap<>();
                extra.put("channelId", channelId);
                extra.put("callerName", callerName);
                extra.put("receiverName", receiverId);
                extra.put("callType", callType);
                payloadMap.put("extra", extra);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(payloadMap);

            // Step 5: Prepare push
            String topic = "com.catalyst.ProCounsellor.voip";
            String token = TokenUtil.sanitizeTokenString(voipToken);

            SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(
                    token,
                    topic,
                    payload
            );

            // Step 6: Send push
            PushNotificationResponse<SimpleApnsPushNotification> response =
                    apnsClient.sendNotification(pushNotification).get();

            if (response.isAccepted()) {
                if (isCancel) {
                    System.out.println("✅ VoIP cancel notification accepted by APNs");
                } else {
                    System.out.println("✅ VoIP call notification accepted by APNs");
                }
            } else {
                System.err.println("❌ VoIP notification rejected: " + response.getRejectionReason());
            }

            apnsClient.close().get();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error sending VoIP notification: " + e.getMessage());
        }
    }

    public void sendFcmNotification(String receiverFCMToken, String senderName, String channelId, String receiverId, String callType) {
        // ✅ Step 1: Save signaling data to Firebase Realtime DB
        agoraCallSignalling.child(receiverId).setValueAsync(new CallSession(senderName, channelId, callType));
        startCall(channelId, senderName, receiverId, callType);

        // ✅ Step 2: Build Android config
        AndroidConfig androidConfig = AndroidConfig.builder()
        	    .setPriority(AndroidConfig.Priority.HIGH)
        	    .build();

        // ✅ Step 3: Build FCM data payload
        Message message = Message.builder()
            .setToken(receiverFCMToken)
            .putData("type", "incoming_call")
            .putData("channelId", channelId)
            .putData("callerName", senderName)
            .putData("receiverName", receiverId)
            .putData("callType", callType)
            .setAndroidConfig(androidConfig)
            .build();

        // ✅ Step 4: Send push
        try {
            FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Android Call Notification Sent!");
        } catch (Exception e) {
            System.err.println("❌ Error sending Android call notification: " + e.getMessage());
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
            // Check who is the user and who is the counsellor
            DocumentSnapshot callerDoc = FirestoreClient.getFirestore()
                    .collection("users").document(callerId).get().get();
            DocumentSnapshot receiverDoc = FirestoreClient.getFirestore()
                    .collection("users").document(receiverId).get().get();

            // Fetch caller and receiver user/counsellor objects
            User callerUser = callerDoc.exists() ? sharedService.getUserById(callerId) : null;
            Counsellor callerCounsellor = callerDoc.exists() ? null : sharedService.getCounsellorById(callerId);

            User receiverUser = receiverDoc.exists() ? sharedService.getUserById(receiverId) : null;
            Counsellor receiverCounsellor = receiverDoc.exists() ? null : sharedService.getCounsellorById(receiverId);

            // Update call history for each entity if not null
            if (callerUser != null) {
                if (callerUser.getCallHistory() == null) {
                    callerUser.setCallHistory(new ArrayList<>());
                }
                callerUser.getCallHistory().add(callHistory);
                sharedService.updateUser(callerUser);
            }

            if (callerCounsellor != null) {
                if (callerCounsellor.getCallHistory() == null) {
                    callerCounsellor.setCallHistory(new ArrayList<>());
                }
                callerCounsellor.getCallHistory().add(callHistory);
                sharedService.updateCounsellor(callerCounsellor);
            }

            if (receiverUser != null) {
                if (receiverUser.getCallHistory() == null) {
                    receiverUser.setCallHistory(new ArrayList<>());
                }
                receiverUser.getCallHistory().add(callHistory);
                sharedService.updateUser(receiverUser);
            }

            if (receiverCounsellor != null) {
                if (receiverCounsellor.getCallHistory() == null) {
                    receiverCounsellor.setCallHistory(new ArrayList<>());
                }
                receiverCounsellor.getCallHistory().add(callHistory);
                sharedService.updateCounsellor(receiverCounsellor);
            }

            System.out.println("Call details updated for both Caller and Receiver.");
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
