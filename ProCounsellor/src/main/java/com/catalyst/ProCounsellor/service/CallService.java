package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.CallHistory;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CallService {

    @Autowired
    private FirebaseDatabase firebaseDatabase;

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
        callHistory.setStartTime(new Date());
        callHistory.setCallType(callType);

        firebaseDatabase.getReference("calls").child(callId).setValueAsync(callHistory);
        firebaseDatabase.getReference("callsByUser").child(receiverId).child("incoming_calls").setValueAsync(callHistory);
        return callId;
    }

    public void endCall(String callId) {
        DatabaseReference callRef = firebaseDatabase.getReference("calls").child(callId);

        callRef.child("endTime").setValueAsync(new Date());
        callRef.child("status").setValueAsync("completed");

        callRef.child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long startTimeMillis = dataSnapshot.getValue(Long.class);
                    if (startTimeMillis != null) {
                        Date startTime = new Date(startTimeMillis);
                        long duration = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - startTime.getTime());
                        callRef.child("duration").setValueAsync(duration);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error fetching startTime: " + databaseError.getMessage());
            }
        });
    }

    public void saveCallOffer(String callId, String sdp) {
        firebaseDatabase.getReference("call_signaling").child(callId).child("offer").setValueAsync(sdp);
    }

    public void saveCallAnswer(String callId, String sdp) {
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
