package com.catalyst.ProCounsellor.controller;

import com.catalyst.ProCounsellor.dto.MessageRequest;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.User;
import com.catalyst.ProCounsellor.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/start-chat")
    public ResponseEntity<?> startChat(@RequestParam String userId, @RequestParam String counsellorId) {
        try {
            String chatId = chatService.startChat(userId, counsellorId);
            return ResponseEntity.ok(Collections.singletonMap("chatId", chatId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting chat.");
        }
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable String chatId, @RequestBody MessageRequest messageRequest) {
        try {
            chatService.sendMessage(chatId, messageRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("Message sent!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending message.");
        }
    }
    
    //To send Media
    @PostMapping("/{chatId}/files")
    public ResponseEntity<?> sendFileMessage(
            @PathVariable String chatId,
            @RequestParam("senderId") String senderId,
            @RequestParam("receiverFcmToken") String receiverFcmToken,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = chatService.sendFileMessage(chatId, senderId, receiverFcmToken, file);
            return ResponseEntity.status(HttpStatus.CREATED).body("File sent successfully! URL: " + fileUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending file.");
        }
    }

    @GetMapping("/{chatId}/messages")
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> getChatMessages(@PathVariable String chatId) {
        // Call the service method and handle the result asynchronously
        return chatService.getChatMessages(chatId)
                .thenApply(messages -> ResponseEntity.ok(messages))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));  // Return an empty body on error
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkChatExists(
            @RequestParam String userId,
            @RequestParam String counsellorId) throws ExecutionException, InterruptedException {
        boolean exists = chatService.doesChatExist(userId, counsellorId);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/user/{userId}/counsellors")
    public ResponseEntity<?> getCounsellorsForUser(@PathVariable String userId) {
        try {
            List<Counsellor> counsellors = chatService.getCounsellorsForUser(userId);
            
            return ResponseEntity.ok(counsellors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching counsellors.");
        }
    }

    @GetMapping("/counsellor/{counsellorId}/users")
    public ResponseEntity<?> getUsersForCounsellor(@PathVariable String counsellorId) {
        try {
            List<User> users = chatService.getUsersForCounsellor(counsellorId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching users.");
        }
    }
    
    @GetMapping("/{chatId}/messages/{messageId}/is-seen")
    public CompletableFuture<ResponseEntity<Boolean>> isMessageSeen(
            @PathVariable String chatId,
            @PathVariable String messageId) {
        return chatService.isMessageSeen(chatId, messageId)
                .thenApply(isSeen -> ResponseEntity.ok(isSeen))
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof IllegalArgumentException) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
                });
    }
    
    @PostMapping("/{chatId}/messages/{messageId}/mark-seen")
    public CompletableFuture<ResponseEntity<String>> markMessageAsSeen(
            @PathVariable String chatId,
            @PathVariable String messageId) {
        return chatService.markMessageAsSeen(chatId, messageId)
                .thenApply(v -> ResponseEntity.ok("Message marked as seen."))
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof IllegalArgumentException) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found.");
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error marking message as seen.");
                });
    }
}
