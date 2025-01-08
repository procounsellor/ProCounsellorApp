package com.catalyst.ProCounsellor.config;

import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class JwtKeyProvider {

    // Base64-encoded custom secret key (32 bytes minimum for HS256)
    private static final String CUSTOM_KEY = "zA1b2C3d4E5f6G7h8I9j0K1l2M3n4O5p6Q7r8S9t0U1v2W3x4Y5z6A7B8C9D0E1=";

    // Static field to hold the signing key
    private static final Key SIGNING_KEY;

    // Static block to initialize the signing key
    static {
        byte[] decodedKey = Base64.getDecoder().decode(CUSTOM_KEY);
        SIGNING_KEY = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());
    }

    // Getter method to provide access to the key
    public static Key getSigningKey() {
        return SIGNING_KEY;
    }
}
