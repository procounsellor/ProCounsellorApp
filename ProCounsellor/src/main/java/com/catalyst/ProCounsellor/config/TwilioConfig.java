package com.catalyst.ProCounsellor.config;

import com.twilio.Twilio;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class TwilioConfig {

    private final String accountSid;
    private final String authToken;
    private final String verifyServiceSid;

    public TwilioConfig() {
        this.accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        this.authToken = System.getenv("TWILIO_AUTH_TOKEN");
        this.verifyServiceSid = System.getenv("TWILIO_VERIFY_SERVICE_SID");

        if (accountSid == null || authToken == null || verifyServiceSid == null) {
            throw new IllegalStateException("Twilio environment variables are not set properly!");
        }
    }

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    public String getVerifyServiceSid() {
        return verifyServiceSid;
    }
}
