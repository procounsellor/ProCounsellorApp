package com.catalyst.ProCounsellor.model;

public class Transaction {
    private String type; // "credit" or "debit"
    private double amount;
    private long timestamp;
    private String description;
    private String paymentId; // optional, for Razorpay tracking

    // Constructors
    public Transaction() {}

    public Transaction(String type, double amount, long timestamp, String description, String paymentId) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
        this.paymentId = paymentId;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
}
