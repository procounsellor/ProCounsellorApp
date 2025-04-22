package com.catalyst.ProCounsellor.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.catalyst.ProCounsellor.service.WalletService;
import com.razorpay.Utils;

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

            String payload = jsonPayload.toString();
            JSONObject webhookData = new JSONObject(payload);

            // Optional: Validate webhook signature
            String webhookSecret = "ProCounsellor@2024";
            String actualSignature = request.getHeader("X-Razorpay-Signature");
            boolean isValid = Utils.verifyWebhookSignature(payload, actualSignature, webhookSecret);

            if (!isValid) {
                System.out.println("⚠️ Invalid webhook signature!");
                return ResponseEntity.status(400).body("Invalid signature");
            }

            System.out.println("✅ Received Webhook: " + webhookData.toString());

            String event = webhookData.getString("event");
            if ("payment.captured".equals(event)) {
                JSONObject paymentEntity = webhookData.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                double amount = paymentEntity.getDouble("amount") / 100.0;
                String paymentId = paymentEntity.getString("id");

                JSONObject notes = paymentEntity.getJSONObject("notes");
                String userName = notes.getString("userName");

                walletService.addFunds(userName, amount, paymentId);
                return ResponseEntity.ok("Wallet updated successfully");
            }

            // Log other events if needed
            System.out.println("Unhandled event type: " + event);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error processing webhook");
        }
        return ResponseEntity.ok("Webhook received");
    }

}
