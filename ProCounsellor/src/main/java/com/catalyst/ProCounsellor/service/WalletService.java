package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.BankDetails;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.razorpay.FundAccount;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
	
    @Autowired
    private Firestore firestore;
    
    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;
    
    private final String USERS_COLLECTION = "users";
    private final String COUNSELLORS_COLLECTION = "counsellors";

    public String createPaymentOrder(double amount, String receipt) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
        
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);  // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1);
        
        Order order = razorpay.orders.create(orderRequest);
        return order.toString();
    }
    
    public void addFunds(String userName, double amount) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userName);
        DocumentSnapshot snapshot = userRef.get().get();
        
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("User not found");
        }
        
        Long currentBalance = snapshot.getLong("walletAmount");
        userRef.update("walletAmount", currentBalance + (long) amount);
    }

    public String withdrawFundsToBank(String userName, double amount) throws IOException, ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection(COUNSELLORS_COLLECTION).document(userName);
        DocumentSnapshot snapshot = userRef.get().get();

        if (!snapshot.exists()) {
            throw new IllegalArgumentException("Counsellor not found");
        }

        Long currentBalance = snapshot.getLong("walletAmount");
        if (currentBalance < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        BankDetails bankDetails = snapshot.toObject(Counsellor.class).getBankDetails();
        String accountNumber = bankDetails.getBankAccountNumber();
        String ifscCode = bankDetails.getIfscCode();

        if (accountNumber == null || ifscCode == null) {
            throw new IllegalArgumentException("Bank details not found for withdrawal");
        }

        // Deduct balance from Firestore wallet
        userRef.update("walletAmount", currentBalance - (long) amount);

        // Razorpay Payout API Request
        String apiUrl = "https://api.razorpay.com/v1/payouts";

        JSONObject payoutRequest = new JSONObject();
        payoutRequest.put("account_number", "Your_Razorpay_Account_Number"); // Replace with actual account number
        payoutRequest.put("fund_account", new JSONObject()
            .put("account_type", "bank_account")
            .put("bank_account", new JSONObject()
                .put("account_number", accountNumber)
                .put("ifsc", ifscCode))
            .put("contact", new JSONObject()
                .put("name", snapshot.getString("firstName") + " " + snapshot.getString("lastName"))
                .put("email", snapshot.getString("email"))
                .put("contact", snapshot.getString("phoneNumber"))
                .put("type", "vendor")));
        payoutRequest.put("amount", (int) (amount * 100)); // Convert to paise
        payoutRequest.put("currency", "INR");
        payoutRequest.put("mode", "IMPS");
        payoutRequest.put("purpose", "withdrawal");
        payoutRequest.put("queue_if_low_balance", true);

        // Send HTTP Request
        String response = sendHttpPost(apiUrl, payoutRequest.toString());

        return response;
    }
    
    private String sendHttpPost(String apiUrl, String jsonBody) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder()
            .encodeToString((keyId + ":" + keySecret).getBytes()));
        connection.setDoOutput(true);

        // Send request body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Get response
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }

        return response.toString();
    }

    public void transferFunds(String userName, String counsellorName, double amount) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userName);
        DocumentReference counsellorRef = firestore.collection(COUNSELLORS_COLLECTION).document(counsellorName);
        
        DocumentSnapshot userSnapshot = userRef.get().get();
        DocumentSnapshot counsellorSnapshot = counsellorRef.get().get();
        
        if (!userSnapshot.exists() || !counsellorSnapshot.exists()) {
            throw new IllegalArgumentException("User or Counsellor not found");
        }

        Long userBalance = userSnapshot.getLong("walletAmount");
        Long counsellorBalance = counsellorSnapshot.getLong("walletAmount");

        if (userBalance < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        userRef.update("walletAmount", userBalance - (long) amount);
        counsellorRef.update("walletAmount", counsellorBalance + (long) amount);
    }
}
