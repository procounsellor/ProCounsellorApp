package com.catalyst.ProCounsellor.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.service.WalletService;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

@RestController
@RequestMapping("/api/payment")
public class WebhookController {
    @Autowired
    private WalletService walletService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        try {
            StringBuilder jsonPayload = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jsonPayload.append(line);
            }

            JSONObject webhookData = new JSONObject(jsonPayload.toString());
            System.out.println("Received Webhook: " + webhookData.toString());

            if ("payment.captured".equals(webhookData.getString("event"))) {
                JSONObject paymentData = webhookData.getJSONObject("payload").getJSONObject("payment");
                double amount = paymentData.getJSONObject("entity").getDouble("amount") / 100.0;
                String userName = paymentData.getJSONObject("entity").getString("notes");
                
                walletService.addFunds(userName, amount);
                return ResponseEntity.ok("Wallet updated successfully");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing webhook");
        }
        return ResponseEntity.ok("Webhook received");
    }
}
