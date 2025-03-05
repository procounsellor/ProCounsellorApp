package com.catalyst.ProCounsellor.controller;

import com.catalyst.ProCounsellor.service.UpiPaymentService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private UpiPaymentService upiPaymentService;

    @PostMapping("/upi")
    public ResponseEntity<String> createUpiOrder(@RequestParam double amount, @RequestParam String receipt) {
        try {
            String orderResponse = upiPaymentService.createUpiPaymentOrder(amount, receipt);
            return ResponseEntity.ok(orderResponse);
        } catch (RazorpayException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
