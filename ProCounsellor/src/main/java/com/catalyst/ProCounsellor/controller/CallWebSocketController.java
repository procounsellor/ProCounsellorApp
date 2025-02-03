package com.catalyst.ProCounsellor.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class CallWebSocketController {

    @MessageMapping("/call")
    @SendTo("/topic/call")
    public String sendCallSignal(String message) {
        return message;
    }
}
