package com.catalyst.ProCounsellor.controller;

import com.catalyst.ProCounsellor.service.CallService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/calls")
public class CallController {

    @Autowired
    private CallService callService;

    //Creates a new call with ongoing status.
    @PostMapping("/start")
    public String startCall(@RequestParam String callerId, @RequestParam String receiverId, @RequestParam String callType) {
        return callService.startCall(callerId, receiverId, callType);
    }

    //Updates endTime, status, and calculates duration.
    @PostMapping("/{callId}/end")
    public void endCall(@PathVariable String callId) {
        callService.endCall(callId);
    }

    
    //Stores SDP offer.
    @PostMapping("/{callId}/offer")
    public void saveCallOffer(@PathVariable String callId, @RequestBody Map<String, String> request) {
        String sdp = request.get("sdp");
        callService.saveCallOffer(callId, sdp);
    }
    
    //Stores SDP answer.
    @PostMapping("/{callId}/answer")
    public void saveCallAnswer(@PathVariable String callId, @RequestBody Map<String, String> request) {
        String sdp = request.get("sdp");
        if (sdp != null) {
            callService.saveCallAnswer(callId, sdp);
        } else {
            System.out.println("SDP answer is missing in the request.");
        }
    }

    // Adds ICE candidates sent from the front end.
    @PostMapping("/{callId}/candidate")
    public void addIceCandidate(@PathVariable String callId, @RequestBody Map<String, Object> request) {
        // Check if the 'candidate' field is already a Map, if not unwrap it
        Object candidate = request.get("candidate");
        if (candidate instanceof Map) {
            // Proceed with the candidate as it is a valid map
            String senderId = (String) request.get("senderId");

            if (senderId != null) {
                callService.addIceCandidate(callId, candidate, senderId);
            } else {
                System.out.println("Invalid senderId received");
            }
        } else {
            System.out.println("Invalid ICE Candidate received: " + request);
        }
    }
}
