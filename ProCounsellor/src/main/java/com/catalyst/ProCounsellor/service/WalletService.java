package com.catalyst.ProCounsellor.service;

import com.catalyst.ProCounsellor.model.BankDetails;
import com.catalyst.ProCounsellor.model.Counsellor;
import com.catalyst.ProCounsellor.model.Transaction;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
	
    @Autowired
    private Firestore firestore;
    
    // @Value("${razorpay.key_id}")
    // private String keyId;

    // @Value("${razorpay.key_secret}")
    // private String keySecret;

	private final String keyId;
    	private final String keySecret;
    
    private final String USERS_COLLECTION = "users";
    private final String COUNSELLORS_COLLECTION = "counsellors";

    public WalletService() {
        this.keyId = System.getenv("RAZORPAY_KEY_ID");
        this.keySecret = System.getenv("RAZORPAY_KEY_SECRET");

        if (keyId == null || keySecret == null) {
            throw new IllegalStateException("Razorpay environment variables are not set properly!");
        }
    }

    public String createPaymentOrder(String userName, double amount, String receipt) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        JSONObject notes = new JSONObject();
        notes.put("userName", userName); // ✅ Attach userName to notes

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);  // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1);
        orderRequest.put("notes", notes); // ✅ Add notes here

        Order order = razorpay.orders.create(orderRequest);
        return order.toString();
    }
    
    public void addFunds(String userName, double amount, String paymentId) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userName);
        DocumentSnapshot snapshot = userRef.get().get();

        if (!snapshot.exists()) {
            throw new IllegalArgumentException("User not found");
        }

        Long currentBalance = snapshot.getLong("walletAmount");
        userRef.update("walletAmount", currentBalance + (long) amount);

        // Create transaction
        Transaction txn = new Transaction("credit", amount, System.currentTimeMillis(), "Funds added via Razorpay", paymentId);

        // Add to Firestore as array
        Map<String, Object> txnMap = new HashMap<>();
        txnMap.put("type", txn.getType());
        txnMap.put("amount", txn.getAmount());
        txnMap.put("timestamp", txn.getTimestamp());
        txnMap.put("description", txn.getDescription());
        txnMap.put("paymentId", txn.getPaymentId());

        userRef.update("transactions", FieldValue.arrayUnion(txnMap));
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

        // Update balances
        userRef.update("walletAmount", userBalance - (long) amount);
        counsellorRef.update("walletAmount", counsellorBalance + (long) amount);

        // Create transactions
        Transaction debitTxn = new Transaction("debit", amount, System.currentTimeMillis(), "Transferred to " + counsellorName, null);
        Transaction creditTxn = new Transaction("credit", amount, System.currentTimeMillis(), "Received from " + userName, null);

        // Map for Firestore
        Map<String, Object> debitTxnMap = new HashMap<>();
        debitTxnMap.put("type", debitTxn.getType());
        debitTxnMap.put("amount", debitTxn.getAmount());
        debitTxnMap.put("timestamp", debitTxn.getTimestamp());
        debitTxnMap.put("description", debitTxn.getDescription());

        Map<String, Object> creditTxnMap = new HashMap<>();
        creditTxnMap.put("type", creditTxn.getType());
        creditTxnMap.put("amount", creditTxn.getAmount());
        creditTxnMap.put("timestamp", creditTxn.getTimestamp());
        creditTxnMap.put("description", creditTxn.getDescription());

        // Update transactions array
        userRef.update("transactions", FieldValue.arrayUnion(debitTxnMap));
        counsellorRef.update("transactions", FieldValue.arrayUnion(creditTxnMap));
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
        payoutRequest.put("account_number", "Your_Razorpay_Account_Number"); // Replace with actual account number:TODO
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
}
