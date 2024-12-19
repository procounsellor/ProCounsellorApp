package com.catalyst.ProCounsellor.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("src/main/resources/procounsellor-71824-firebase-adminsdk-a73ra-0c3dfaf526.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://procounsellor-71824-default-rtdb.firebaseio.com")
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
    
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("src/main/resources/procounsellor-71824-firebase-adminsdk-a73ra-0c3dfaf526.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://procounsellor-71824-default-rtdb.firebaseio.com")
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }

    // Add this method to expose Firestore as a Spring bean
    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
    
    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp);
    }
}
