package com.catalyst.ProCounsellor.controller;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

@RestController
@RequestMapping("/api/payment")
public class WebhookController {

    @PostMapping("/webhook")
    public String handleWebhook(HttpServletRequest request) {
        try {
            StringBuilder jsonPayload = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jsonPayload.append(line);
            }

            JSONObject webhookData = new JSONObject(jsonPayload.toString());
            System.out.println("Received Webhook: " + webhookData.toString());

            // Process payment status
            if ("payment.captured".equals(webhookData.getString("event"))) {
                System.out.println("Payment Successful: " + webhookData.getJSONObject("payload").getJSONObject("payment").getString("id"));
            }
        } catch (IOException e) {
            return "Error processing webhook";
        }
        return "Webhook received";
    }
}
