package com.catalyst.ProCounsellor.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UpiPaymentService {

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    public String createUpiPaymentOrder(double amount, String receipt) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);  // Amount in paise (₹1 = 100 paise)
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1);  // Auto capture payment

        Order order = razorpay.orders.create(orderRequest);
        return order.toString();
    }
}

