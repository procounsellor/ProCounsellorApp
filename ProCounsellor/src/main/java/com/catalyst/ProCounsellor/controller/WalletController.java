package com.catalyst.ProCounsellor.controller;

import com.catalyst.ProCounsellor.service.WalletService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    @Autowired
    private WalletService walletService;

    @PostMapping("/add")
    public ResponseEntity<String> addFunds(@RequestParam String userName, @RequestParam double amount) {
        try {
            String orderResponse = walletService.createPaymentOrder(userName, amount, "order_" + userName + "_" + System.currentTimeMillis());
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawFunds(@RequestParam String userName, @RequestParam double amount) {
        try {
            String response = walletService.withdrawFundsToBank(userName, amount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferFunds(@RequestParam String userName, @RequestParam String counsellorName, @RequestParam double amount) {
        try {
            walletService.transferFunds(userName, counsellorName, amount);
            return ResponseEntity.ok("Funds transferred successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}