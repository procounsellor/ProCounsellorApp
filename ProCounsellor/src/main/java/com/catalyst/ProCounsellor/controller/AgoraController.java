package com.catalyst.ProCounsellor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.service.AgoraTokenService;

@RestController
@RequestMapping("/api/agora")
public class AgoraController {
	@Autowired
    private AgoraTokenService agoraTokenService;
	
    @GetMapping("/token")
    public String getAgoraToken(@RequestParam String channelName, @RequestParam int uid) {
        return agoraTokenService.generateToken(channelName, uid);
    }
    
    @PostMapping("/send")
    public String sendCallNotification(@RequestBody CallNotificationRequest request) {
        agoraTokenService.sendCallNotification(
            request.getReceiverFCMToken(), 
            request.getSenderName(), 
            request.getChannelId(),
            request.getReceiverId()
        );
        return "✅ Notification sent successfully!";
    }
    
    // ✅ Create a class to map JSON request
    public static class CallNotificationRequest {
        private String receiverFCMToken;
        private String senderName;
        private String channelId;
        private String receiverId;

        // Getters & Setters
        public String getReceiverFCMToken() { return receiverFCMToken; }
        public void setReceiverFCMToken(String receiverFCMToken) { this.receiverFCMToken = receiverFCMToken; }
        
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }

        public String getChannelId() { return channelId; }
        public void setChannelId(String channelId) { this.channelId = channelId; }
        
        public String getReceiverId() { return receiverId; }
        public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    }
}
